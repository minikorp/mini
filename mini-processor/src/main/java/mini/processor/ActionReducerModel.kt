package mini.processor

import com.squareup.kotlinpoet.*
import mini.Action
import org.jetbrains.annotations.TestOnly
import javax.tools.StandardLocation

const val DEBUG_MODE = false

class ActionReducerModel(private val reducerFunctions: List<ReducerFuncModel>) {
    private val actionType = elementUtils.getTypeElement("mini.Action").asType()
    private val stores: List<StoreModel>
    private val tags: List<TagModel>
    private val reducerParameters: List<ReducerFunctionParameterModel>
    private val actionToFunctionMap: Map<ReducerFunctionParameterModel, List<ReducerFuncModel>>

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

        reducerParameters = reducerFunctions.map { it.tag }
            .map { ReducerFunctionParameterModel(it.asElement()) }
            .distinctBy { it.element.qualifiedName() }

        tags = reducerParameters.map { it.tags }
            .flatten()
            .distinctBy { it.typeMirror.qualifiedName() }

        actionToFunctionMap = reducerParameters.map { parameterModel ->
            parameterModel to reducerFunctions
                .filter { it.tag.qualifiedName() in parameterModel.tags.map { it.typeMirror.qualifiedName() } }
                .sortedBy { it.priority }
        }.toMap().toSortedMap(Comparator { a, b ->
            val aType = a.element.asType()
            val bType = b.element.asType()
            //More generic types go lower in the when branch
            when {
                aType isSameType bType -> 0
                aType isSubtypeOf bType -> -1
                else -> 1
            }
        })

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
                .addModifiers(KModifier.PRIVATE)
                .initializer(CodeBlock.of("stores.get(%T::class.java) as %T", typeName, typeName))
                .build()
            )
        }
        return this
    }

    private fun TypeSpec.Builder.addDispatcherFunction(): TypeSpec.Builder {
        val reduceBuilder = with(FunSpec.builder("reduce")) {

            addParameter(ParameterSpec.builder("action", Action::class).build())
            addModifiers(KModifier.OVERRIDE)

            addStatement("when (action) {%>")
            val whenBranches = actionToFunctionMap.filterValues { !it.isEmpty() }
            var index = 0
            whenBranches.forEach { parameterModel, reducers ->
                if (index == whenBranches.size - 1 && parameterModel.element.asType() isSameType actionType) {
                    addStatement("else -> {%>")
                } else {
                    addStatement("is %T -> {%>", parameterModel.element.asType().asTypeName())
                }
                index++

                reducers.forEach { reducer ->
                    val storeFieldName = reducer.storeFieldName

                    val callString = if (reducer.hasStateParameter) {
                        CodeBlock.of("action, $storeFieldName.state")
                    } else {
                        CodeBlock.of("action")
                    }

                    addCode(CodeBlock.builder()
                        .add("$storeFieldName.setStateInternal(")
                        .add("$storeFieldName.${reducer.funcName}($callString)")
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
    fun generateStoreProperties(className: String) = TypeSpec.classBuilder(className).addStoreProperties()

    @TestOnly
    fun generateMainConstructor(className: String) = TypeSpec.classBuilder(className).addMainConstructor()

    @TestOnly
    fun generateReduceFunc(className: String) = TypeSpec.classBuilder(className).addDispatcherFunction()

    @TestOnly
    fun generateActionReducer(className: String, packageName: String) = TypeSpec.classBuilder(className)
        .addSuperinterface(ClassName(packageName, ACTION_REDUCER_INTERFACE))
        .addMainConstructor()
        .addStoreProperties()
        .addDispatcherFunction()
}