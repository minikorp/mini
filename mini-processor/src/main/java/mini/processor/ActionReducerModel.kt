package mini.processor

import com.squareup.kotlinpoet.*
import mini.Action
import org.jetbrains.annotations.TestOnly
import javax.lang.model.element.Modifier
import javax.tools.StandardLocation

const val DEBUG_MODE = false

class ActionReducerModel(private val reducerFunctions: List<ReducerFuncModel>) {
    private val actionType = elementUtils.getTypeElement("mini.Action").asType()
    private val stores: List<StoreModel>
    private val tags: List<TagModel>
    private val actions: List<ActionModel>
    private val actionToFunctionMap: Map<ActionModel, List<ReducerFuncModel>>

    companion object {
        const val MINI_COMMON_PACKAGE_NAME = "mini"
        const val MINI_PROCESSOR_PACKAGE_NAME = "mini.processor"
        const val STORE_CLASS_NAME = "Store"
        const val ACTION_REDUCER_CLASS_NAME = "MiniActionReducer"
        const val ACTION_REDUCER_INTERFACE = "ActionReducer"
    }

    init {
        stores = reducerFunctions
            .distinctBy { it.storeElement.qualifiedName() }
            .map {
                StoreModel(
                    fieldName = it.storeFieldName,
                    element = it.storeElement)
            }

        actions = reducerFunctions.map { it.tag }
            .filter {
                //Take subtypes of action that are not abstract
                it.isSubtypeOf(actionType) && !it.asElement().modifiers.contains(Modifier.ABSTRACT)
            }
            .map { ActionModel(it.asElement()) }
            .distinctBy { it.element.qualifiedName() }

        tags = actions.map { it.tags }
            .flatten()
            .distinctBy { it.typeMirror.qualifiedName() }

        actionToFunctionMap = actions.map { actionModel ->
            actionModel to reducerFunctions
                .filter { it.tag in actionModel.tags.map { it.typeMirror } }
                .sortedBy { it.priority }
        }.toMap()

    }

    fun generateDispatcherFile() {
        //Generate FileSpec
        val builder = FileSpec.builder(MINI_COMMON_PACKAGE_NAME, ACTION_REDUCER_CLASS_NAME)
        //Start generating file
        val kotlinFile = builder
            .addType(TypeSpec.classBuilder(ACTION_REDUCER_CLASS_NAME)
                .addSuperinterface(ClassName(MINI_COMMON_PACKAGE_NAME, ACTION_REDUCER_INTERFACE))
                .addMainConstructor()
                .addStoreProperties()
                .addDispatcherFunction()
                .build())
            .build()

        val kotlinFileObject = env.filer.createResource(StandardLocation.SOURCE_OUTPUT,
            MINI_PROCESSOR_PACKAGE_NAME, "${kotlinFile.name}.kt")
        val openWriter = kotlinFileObject.openWriter()
        kotlinFile.writeTo(openWriter)
        openWriter.close()
    }

    private fun TypeSpec.Builder.addMainConstructor(): TypeSpec.Builder {
        return primaryConstructor(FunSpec.constructorBuilder()
            .addParameter("stores", getStoreMapType())
            .build())
    }

    private fun TypeSpec.Builder.addStoreProperties(): TypeSpec.Builder {
        stores.forEach { storeModel ->
            val typeName = storeModel.element.asType().asTypeName()
            addProperty(PropertySpec.builder(storeModel.fieldName, typeName)
                .initializer(CodeBlock.of("stores.get(%T::class.java) as %T", typeName, typeName))
                .build()
            )
        }
        return this
    }

    private fun TypeSpec.Builder.addDispatcherFunction(): TypeSpec.Builder {
        val reduceBuilder = with(FunSpec.builder("reduce")) {
            addParameters(listOf("action" to Action::class).map { ParameterSpec.builder(it.first, it.second).build() })
            addModifiers(KModifier.OVERRIDE)

            addStatement("when(action) {%>")
            actionToFunctionMap
                .filterValues { !it.isEmpty() }
                .forEach { actionModel, reducers ->
                    addStatement("is %T -> {%>", actionModel.element.asType().asTypeName())
                    reducers.forEach { reducer ->

                        val storeFieldName = reducer.storeFieldName

                        fun callString(): CodeBlock {
                            return if (reducer.hasStateParameter) {
                                CodeBlock.of("action, $storeFieldName.state")
                            } else {
                                CodeBlock.of("action")
                            }
                        }

                        addCode(CodeBlock.builder()
                            .add("$storeFieldName.setStateInternal(")
                            .add("$storeFieldName.${reducer.funcName}(${callString()})")
                            .add(")\n")
                            .build())
                    }
                    addStatement("%<}")
                }
            addStatement("%<}")
            return@with this
        }

        return addFunction(reduceBuilder.build())
    }

    private fun getStoreMapType(): ParameterizedTypeName {
        val anyStoreType = ClassName(MINI_COMMON_PACKAGE_NAME, STORE_CLASS_NAME).wildcardType() //Store<*>
        val anyClassType = ClassName("java.lang", "Class").wildcardType() //Class<*>
        return mapTypeOf(anyClassType, anyStoreType)
    }

    private fun getActionTagMapType(): ParameterizedTypeName {
        val anyClassType = ClassName("kotlin.reflect", "KClass").wildcardType() //Class<*>
        return mapTypeOf(anyClassType, anyClassType.listTypeName())
    }

    @TestOnly
    fun generateStoreProperties(className : String) =  TypeSpec.classBuilder(className).addStoreProperties()

    @TestOnly
    fun generateMainConstructor(className : String) =  TypeSpec.classBuilder(className).addMainConstructor()

    @TestOnly
    fun generateReduceFunc(className : String) =  TypeSpec.classBuilder(className).addDispatcherFunction()

    @TestOnly
    fun generateActionReducer(className : String, packageName : String) =  TypeSpec.classBuilder(className).
            addSuperinterface(ClassName(packageName, ACTION_REDUCER_INTERFACE))
            .addMainConstructor()
            .addStoreProperties()
            .addDispatcherFunction()
}