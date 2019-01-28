package mini.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import mini.Action
import mini.Dispatcher.Companion.ACTION_TYPES_GEN_CLASS
import mini.Dispatcher.Companion.ACTION_TYPES_GEN_PACKAGE
import mini.Reducer
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.tools.StandardLocation

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated")
class MiniProcessor : AbstractProcessor() {

    override fun init(environment: ProcessingEnvironment) {
        env = environment
        typeUtils = env.typeUtils
        elementUtils = env.elementUtils
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Reducer::class.java, Action::class.java)
            .map { it.canonicalName }
            .toMutableSet()
    }

    override fun process(set: MutableSet<out TypeElement>,
                         roundEnv: RoundEnvironment): Boolean {

        val roundActions = roundEnv.getElementsAnnotatedWith(Action::class.java)
        if (roundActions.isEmpty()) return false

        //Get non-abstract actions
        val actionModels = roundActions.filter { Modifier.ABSTRACT !in it.modifiers }
            .map { ActionModel(it) }

        val file = FileSpec.builder(ACTION_TYPES_GEN_PACKAGE, ACTION_TYPES_GEN_CLASS)
            .addComment("Automatically generated, do not edit\n")
            .apply {
                actionModels.forEach {
                    addComment("${it.element.simpleName}\n")
                    addComment("----- ${it.superTypes.map { it.element.simpleName }}\n")
                }
            }
            .addType(TypeSpec.classBuilder(ACTION_TYPES_GEN_CLASS)
                .addType(TypeSpec.companionObjectBuilder().apply {
                    val anyClassTypeName = Class::class.asTypeName().parameterizedBy(STAR)
                    val mapType = Map::class.asClassName().parameterizedBy(
                        anyClassTypeName,
                        List::class.asTypeName().parameterizedBy(anyClassTypeName)
                    )
                    val prop = PropertySpec.builder("actionTypes", mapType)
                        .addAnnotation(JvmField::class)
                        //⇤⇥«»
                        .initializer(CodeBlock.builder()
                            .add("mapOf(\n⇥")
                            .apply {
                                actionModels.forEach { actionModel ->
                                    val comma = if (actionModel != actionModels.last()) "," else ""
                                    add("«")
                                    add("%T::class.java to ", actionModel.typeName)
                                    add(actionModel.listOfSupertypesCodeBlock())
                                    add(comma)
                                    add("\n»")
                                }
                            }
                            .add("⇤)")
                            .build())
                    addProperty(prop.build())
                }.build()).build()).build()

        val kotlinFileObject = env.filer.createResource(StandardLocation.SOURCE_OUTPUT,
            file.packageName, "${file.name}.kt")
        val openWriter = kotlinFileObject.openWriter()
        file.writeTo(openWriter)
        openWriter.close()

        return true
    }

    class ActionModel(val element: Element) {
        val type = element.asType()
        val typeName = type.asTypeName()
        val superTypes = collectTypes(type)
            .sortedBy { it.depth }
            //Ignore base types
            .filter { it.mirror.qualifiedName() != "java.lang.Object" }
            .filter { it.mirror.qualifiedName() != "mini.BaseAction" }

        fun listOfSupertypesCodeBlock(): CodeBlock {
            val format = superTypes.joinToString(", ") { "%T::class.java" }
            val args = superTypes.map { it.mirror.asTypeName() }.toTypedArray()
            return CodeBlock.of("listOf($format)", *args)
        }

        private fun collectTypes(mirror: TypeMirror, depth: Int = 0): Set<ActionSuperType> {
            //We want to add by depth
            val superTypes = typeUtils.directSupertypes(mirror).toSet()
                .map { collectTypes(it, depth + 1) }
                .flatten()
            return setOf(ActionSuperType(mirror, depth)) + superTypes
        }

        class ActionSuperType(val mirror: TypeMirror, val depth: Int) {
            val element = mirror.asElement()
            val qualifiedName = element.qualifiedName()

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as ActionSuperType
                if (qualifiedName != other.qualifiedName) return false
                return true
            }

            override fun hashCode(): Int {
                return qualifiedName.hashCode()
            }
        }
    }
}
