package mini.processor

import com.squareup.kotlinpoet.*
import mini.Action
import javax.tools.StandardLocation

class ActionReducerModel(actionElements: List<ReducerFuncModel>) {
    private val reducersMaps = mutableMapOf<String, MutableList<ReducerFuncModel>>()
    private val actions = actionElements.map { it.action }
    private val stores = mutableListOf<StoreModel>()

    init {
        actionElements
            .filter { it.parentClass.isClass } //Check if superclass is store type
            .forEach { reducersMaps.getOrPut(it.action.actionName) { mutableListOf() }.add(it) }
        stores.addAll(reducersMaps.values.flatten().distinctBy { it.parentClass.toString() }.map { StoreModel(it.parentClass) })
    }

    fun generateDispatcherFile() {
        //Generate FileSpec
        val builder = FileSpec.builder("mini", "MiniActionReducer")
        //Add Store imports
        stores.forEach { builder.addStaticImport(it.packageName, it.className) }
        //Add Action imports
        actions.forEach { builder.addStaticImport(it.packageName, it.actionName) }
        //Start generating file
        val kotlinFile = builder
            .addType(TypeSpec.classBuilder("MiniActionReducer")
                .addSuperinterface(ClassName("mini", "ActionReducer"))
                .addMainConstructor()
                .addStoreProperties()
                .addDispatcherFunction()
                .build())
            .build()

        val kotlinFileObject = ProcessorUtils.env.filer.createResource(StandardLocation.SOURCE_OUTPUT,
            "mini.processor", "${kotlinFile.name}.kt")
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
                                    addIndentedStatement("${it.methodCall}(action)")
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
        val anyStoreType = ClassName("mini", "Store").wildcardType() //Store<*>
        val anyClassType = ClassName("java.lang", "Class").wildcardType() //Class<*>
        return mapTypeOf(anyClassType, anyStoreType)
    }

    private fun getInterceptorListType(): ParameterizedTypeName {
        return ClassName("", "Interceptor").arrayListType()
    }
}