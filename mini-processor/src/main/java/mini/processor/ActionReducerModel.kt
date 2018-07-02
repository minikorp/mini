package mini.processor

import com.squareup.kotlinpoet.*
import mini.Action
import javax.tools.Diagnostic
import javax.tools.StandardLocation

const val DEBUG_MODE = false

class ActionReducerModel(reducerFunctions: List<ReducerFuncModel>) {
    private val reducersMaps = mutableMapOf<String, MutableList<ReducerFuncModel>>()
    private val stores: List<StoreModel>

    companion object {
        const val MINI_COMMON_PACKAGE_NAME = "mini"
        const val MINI_PROCESSOR_PACKAGE_NAME = "mini.processor"
        const val STORE_CLASS_NAME = "Store"
        const val ACTION_REDUCER_CLASS_NAME = "MiniActionReducer"
        const val ACTION_REDUCER_INTERFACE = "ActionReducer"
    }

    init {
        logMessage(Diagnostic.Kind.NOTE, "Filtering actions")
        //Reverse the map for code-gen
        reducerFunctions
            .forEach { reducersMaps.getOrPut(it.action.actionName) { ArrayList() }.add(it) }
        logMessage(Diagnostic.Kind.NOTE, "${reducerFunctions.size} Actions retrieved. Starting stores mapping")
        stores = reducersMaps.values
            .flatten()
            .distinctBy { it.parentClass.toString() }
            .map { StoreModel(it.parentClass) }
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

        val kotlinFileObject = ProcessorUtils.env.filer.createResource(StandardLocation.SOURCE_OUTPUT,
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
            val storeClass = ClassName(storeModel.packageName, storeModel.className)
            addProperty(PropertySpec.builder(storeModel.className.toLowerCase(), storeClass)
                .initializer(CodeBlock.of("stores.get(%T::class.java) as %T", storeClass, storeClass))
                .build()
            )
        }
        return this
    }

    private fun TypeSpec.Builder.addDispatcherFunction(): TypeSpec.Builder {
        val reduceBuilder = with(FunSpec.builder("reduce")) {
            addParameters(listOf("action" to Action::class).map { ParameterSpec.builder(it.first, it.second).build() })
            addModifiers(KModifier.OVERRIDE)
            addCode(linesOfCode(
                "action.tags.forEach { tag ->",
                "%>when (tag) {%>", ""))
            reducersMaps
                .map { ReduceBlockModel(it.value[0].action, it.value) }
                .forEach { reduceBlock ->
                    val actionClass = ClassName(reduceBlock.action.packageName, reduceBlock.action.actionName)
                    addCode(CodeBlock.of("%T::class.java -> {\n%>action as %T\n", actionClass, actionClass))
                    addCode(reduceBlock.methodCalls.joinToString(separator = "\n") { it.methodCall })
                    addCode(linesOfCode("%<", "}", ""))
                }
            addCode(linesOfCode("%<", "}%<", "}", ""))
            return@with this
        }
        return addFunction(reduceBuilder.build())
    }

    private fun getStoreMapType(): ParameterizedTypeName {
        val anyStoreType = ClassName(MINI_COMMON_PACKAGE_NAME, STORE_CLASS_NAME).wildcardType() //Store<*>
        val anyClassType = ClassName("java.lang", "Class").wildcardType() //Class<*>
        return mapTypeOf(anyClassType, anyStoreType)
    }
}