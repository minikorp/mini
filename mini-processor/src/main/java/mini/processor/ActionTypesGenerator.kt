package mini.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeMirror

object ActionTypesGenerator {
    fun generate(container: TypeSpec.Builder, elements: Set<Element>) {

        val actionModels = elements.filter { Modifier.ABSTRACT !in it.modifiers }
            .map { ActionModel(it) }

        container.apply {
            val anyClassTypeName = Class::class.asTypeName().parameterizedBy(STAR)
            val listTypeName = List::class.asTypeName().parameterizedBy(anyClassTypeName)
            val mapType = Map::class
                .asClassName()
                .parameterizedBy(anyClassTypeName, listTypeName)

            val prop = PropertySpec.builder("actionTypes", mapType)
                .addModifiers(KModifier.PRIVATE)
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
        }.build()
    }
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
        val format = superTypes.joinToString(",\n") { "%T::class.java" }
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