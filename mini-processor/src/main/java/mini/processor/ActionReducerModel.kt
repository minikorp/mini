package mini.processor

import com.squareup.kotlinpoet.*
import mini.Action
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Types
import javax.tools.StandardLocation

class ActionReducerModel(actionElements: List<ReducerFuncModel>) {
    private val reducersMaps = mutableMapOf<String, MutableList<ReducerFuncModel>>()
    private val actions = actionElements.map { it.action }
    private val stores = mutableListOf<StoreModel>()

    companion object {
        const val MINI_COMMON_PACKAGE_NAME = "mini"
        const val MINI_PROCESSOR_PACKAGE_NAME = "mini.processor"
        const val STORE_CLASS_NAME = "Store"
        const val ACTION_REDUCER_CLASS_NAME = "MiniActionReducer"
        const val ACTION_REDUCER_INTERFACE = "ActionReducer"
    }

    init {
        actionElements
            .filter { it.parentClass.isClass } //Check if superclass is store type
            .forEach { reducersMaps.getOrPut(it.action.actionName) { mutableListOf() }.add(it) }
        stores.addAll(reducersMaps.values.flatten().distinctBy { it.parentClass.toString() }.map { StoreModel(it.parentClass) })
    }

    fun generateDispatcherFile() {
        //Generate FileSpec
        val builder = FileSpec.builder(MINI_COMMON_PACKAGE_NAME, ACTION_REDUCER_CLASS_NAME)
        //Add Store imports
        stores.forEach { builder.addStaticImport(it.packageName, it.className) }
        //Add Action imports
        actions.forEach { builder.addStaticImport(it.packageName, it.actionName) }
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
            val storeClass = ClassName("", storeModel.className)
            addProperty(PropertySpec.builder(storeModel.className.toLowerCase(), storeClass)
                .initializer("stores.get(${storeClass.canonicalName}::class.java) as ${storeClass.canonicalName}")
                .build()
            )
        }
        return this
    }

    private fun TypeSpec.Builder.addDispatcherFunction(): TypeSpec.Builder {
        val reduceBuilder = with(FunSpec.builder("reduce")) {
            addParameters(listOf("action" to Action::class).map { ParameterSpec.builder(it.first, it.second).build() })
            addModifiers(KModifier.OVERRIDE)
            addIndentedStatement("action.tags.forEach { tag ->")
            nestedBlock("when (tag)") {
                reducersMaps
                    .map { ReduceBlockModel(it.key, it.value) }
                    .forEach { reduceBlock ->
                        nestedBlock("${reduceBlock.actionName}::class.java ->") {
                            indent {
                                addIndentedStatement("action as ${reduceBlock.actionName}")
                                reduceBlock.methodCalls.forEach {
                                    addIndentedStatement(it.methodCall)
                                }
                            }
                        }
                    }
            }

            addIndentedStatement("}") //For loop
        }
        return addFunction(reduceBuilder.build())
    }

    private fun getStoreMapType(): ParameterizedTypeName {
        val anyStoreType = ClassName(MINI_COMMON_PACKAGE_NAME, STORE_CLASS_NAME).wildcardType() //Store<*>
        val anyClassType = ClassName("java.lang", "Class").wildcardType() //Class<*>
        return mapTypeOf(anyClassType, anyStoreType)
    }
}