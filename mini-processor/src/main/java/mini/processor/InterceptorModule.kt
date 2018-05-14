package mini.processor

import com.squareup.kotlinpoet.*
import mini.Action
import javax.tools.StandardLocation

class InterceptorModule(actionElements: List<ReducerModelFunc>) {
    //private val storeMap = mutableMapOf<String, StoreModel>()
    private val reducersMaps = mutableMapOf<String, MutableList<ReducerModelFunc>>()
    private val actions = actionElements.map { it.action }
    private val stores = mutableListOf<StoreModel>()
    var depth = 0

    init {
        actionElements
                .filter { it.parentClass.isClass } //Check if superclass is store type
                .forEach { reducersMaps.getOrPut(it.action.actionName) { mutableListOf() }.add(it) }
        stores.addAll(reducersMaps.values.flatten().distinctBy { it.parentClass.toString() }.map { StoreModel(it.parentClass) })
        generateInterceptorFile()
    }

    private fun generateInterceptorFile() {
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
                        .mainConstructor()
                        .addStoreValues()
                        .reduceFunction()
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

    private fun TypeSpec.Builder.reduceFunction(): TypeSpec.Builder {
        val reduceBuilder = with(FunSpec.builder("reduce")) {
            addParameters(listOf("action" to Action::class).map { ParameterSpec.builder(it.first, it.second).build() })
            addModifiers(KModifier.OVERRIDE)
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
        }

        return addFunction(reduceBuilder
                .addIndentedStatement("}")
                .build())
    }

    private fun indent(): String {
        if (depth == 0) return ""
        return "    ".repeat(depth)
    }

    private inline fun <T> T.nest(func: T.() -> Unit): T {
        depth++
        func()
        depth--
        return this
    }

    private fun FunSpec.Builder.addIndentedStatement(statement: String, vararg args: Any): FunSpec.Builder {
        addStatement(indent() + statement, args)
        return this
    }

    private fun getStoreMapType(): ParameterizedTypeName {
        val anyType = WildcardTypeName.subtypeOf(ANY) // <*>
        val storeType = ClassName("mini", "Store") //Store
        val anyStoreType = ParameterizedTypeName.get(storeType, anyType) //Store<*>
        val kClassType = ClassName("java.lang", "Class") //Class
        val storeClass = ParameterizedTypeName.get(kClassType, anyType) //Class<*>
        val kotlinMapType = ClassName("kotlin.collections", "Map") //Map
        //Generated the parameterized constructor
        return ParameterizedTypeName.get(kotlinMapType, storeClass, anyStoreType) //Map<Class<*>, Store<*>>
    }
}