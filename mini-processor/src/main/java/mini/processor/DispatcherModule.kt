package mini.processor

import com.squareup.kotlinpoet.*
import mini.Action
import mini.Chain
import javax.tools.StandardLocation

class DispatcherModule(actionElements: List<ReducerModelFunc>) {
    private val reducersMaps = mutableMapOf<String, MutableList<ReducerModelFunc>>()
    private val actions = actionElements.map { it.action }
    private val stores = mutableListOf<StoreModel>()

    init {
        actionElements
            .filter { it.parentClass.isClass } //Check if superclass is store type
            .forEach { reducersMaps.getOrPut(it.action.actionName) { mutableListOf() }.add(it) }
        stores.addAll(reducersMaps.values.flatten().distinctBy { it.parentClass.toString() }.map { StoreModel(it.parentClass) })
        generateInterceptorFile()
    }

    private fun generateInterceptorFile() {
        //Generate FileSpec
        val builder = FileSpec.builder("mini", "MiniDispatcher")
        //Add Store imports
        stores.forEach { builder.addStaticImport(it.packageName, it.className) }
        //Add Action imports
        actions.forEach { builder.addStaticImport(it.packageName, it.actionName) }
        //Start generating file
        val kotlinFile = builder
            .addType(TypeSpec.classBuilder("MiniDispatcher")
                .addSuperinterface(ClassName("mini", "FluxDispatcher"))
                .mainConstructor()
                .addStoreValues()
                .overrideDispatcherValues()
                .dispatcherFunction()
                .build())
            .build()

        val kotlinFileObject = ProcessorUtils.env.filer.createResource(StandardLocation.SOURCE_OUTPUT,
            "mini.processor", "${kotlinFile.name}.kt")
        val openWriter = kotlinFileObject.openWriter()
        kotlinFile.writeTo(openWriter)
        openWriter.close()
    }

    private fun TypeSpec.Builder.mainConstructor(): TypeSpec.Builder {
        return primaryConstructor(FunSpec.constructorBuilder()
            .addParameter("stores", getStoreMapType())
            .build())
    }

    private fun TypeSpec.Builder.addStoreValues(): TypeSpec.Builder {
        stores.forEach { storeModel ->
            val storeClass = ClassName("", storeModel.className)
            addProperty(
                PropertySpec.builder(storeModel.className.toLowerCase(), storeClass)
                    .initializer("stores.get(${storeClass.canonicalName}::class.java) as ${storeClass.canonicalName}")
                    .build()
            )
        }
        return this
    }

    private fun TypeSpec.Builder.overrideDispatcherValues(): TypeSpec.Builder {
        return addProperty(
            PropertySpec.builder("interceptors", getInterceptorListType(), KModifier.OVERRIDE)
                .initializer("ArrayList<Interceptor>()")
                .build())
            .addProperty(
                PropertySpec.builder("rootChain", Chain::class, KModifier.OVERRIDE)
                    .initializer("object : Chain {\n" +
                        "        override fun proceed(action: Action): Action {\n" +
                        "            interceptors.forEach { it.invoke(action, this) }; return action\n" +
                        "        }\n" +
                        "    }")
                    .build())
            .addProperty(
                PropertySpec.varBuilder("chain", Chain::class, KModifier.OVERRIDE)
                    .initializer("rootChain")
                    .build())
            .addProperty(
                PropertySpec.varBuilder("dispatching", Boolean::class, KModifier.OVERRIDE)
                    .initializer("false")
                    .build())
    }

    private fun TypeSpec.Builder.dispatcherFunction(): TypeSpec.Builder {
        val reduceBuilder = with(FunSpec.builder("dispatch")) {
            addParameters(listOf("action" to Action::class).map { ParameterSpec.builder(it.first, it.second).build() })
            addModifiers(KModifier.OVERRIDE)
            addIndentedStatement("assertOnUiThread()")
            addIndentedStatement("synchronized(this) {")
            nest {
                addIndentedStatement("try {")
                nest {
                    addIndentedStatement("if (dispatching) error(\"Can't dispatch actions while reducing state!\")")
                    addIndentedStatement("dispatching = true")
                    addIndentedStatement("action.tags.forEach { tag ->")
                    nest {
                        addIndentedStatement("when (tag) {")

                        reducersMaps
                            .map { ReduceBlock(it.key, it.value) }
                            .forEach { reduceBlock ->
                                nest {
                                    addIndentedStatement("${reduceBlock.actionName}::class.java -> {")
                                    nest {
                                        addIndentedStatement("action as ${reduceBlock.actionName}")
                                        reduceBlock.methodCalls.forEach {
                                            addIndentedStatement("${it.methodCall}(action)")
                                        }
                                    }
                                    addIndentedStatement("}")
                                }
                            }
                        addIndentedStatement("}")
                    }
                    addIndentedStatement("}")
                }
                addIndentedStatement("} finally {")
                nest {
                    addIndentedStatement("dispatching = false")
                }
                addIndentedStatement("}")
            }
        }

        return addFunction(reduceBuilder
            .addIndentedStatement("}")
            .build())
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