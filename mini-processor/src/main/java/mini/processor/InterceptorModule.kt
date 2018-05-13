package mini.processor

import com.squareup.kotlinpoet.*
import com.sun.xml.internal.ws.policy.sourcemodel.ModelNode
import mini.Action
import mini.Chain
import mini.Store
import sun.reflect.generics.tree.TypeArgument
import java.lang.reflect.Type
import javax.tools.StandardLocation
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.types.TypeHolderArgument

class InterceptorModule(val actionElements: List<ReducerFunc>) {
    val storeMap = mutableMapOf<String, StoreModel>()

    init {
        actionElements
            .filter { it.parentClass.isClass } //Check if superclass is store type
            .forEach {
                val store = StoreModel(it.parentClass)
                if (storeMap.contains(store.className)) {
                    storeMap[store.className]!!.subscribedMethods.add(it)
                } else {
                    store.subscribedMethods.add(it)
                    storeMap[store.className] = store
                }
            }
        generateInterceptorFile()
    }

    fun generateInterceptorFile() {

        //Generate FileSpec
        val builder = FileSpec.builder("mini", "DispatcherInterceptor")
        //Add Store imports
        storeMap.forEach { builder.addStaticImport(it.value.packageName, it.value.className) }
        //Add Action imports
        actionElements.forEach { builder.addStaticImport(it.action.packageName, it.action.actionName) }
        //Start generating file
        val kotlinFile = builder
            .addType(
                TypeSpec.classBuilder("ActionDispatcher")
                    .addSuperinterface(ClassName("mini", "Interceptor"))
                    .mainConstructor()
                    .addStoreValues()
                    .invoqueFunction()
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
        storeMap.forEach { s, storeModel ->
            val storeClass = ClassName("", storeModel.className)
            addProperty(
                PropertySpec.builder(s.toLowerCase(), storeClass)
                    .initializer("stores.get(${storeClass.canonicalName}::class.java) as ${storeClass.canonicalName}")
                    .build()
            )
        }
        return this
    }

    private fun TypeSpec.Builder.invoqueFunction(): TypeSpec.Builder {
        val invoqueBuilder = FunSpec.builder("invoke")
            .addParameters(
                listOf("action" to Action::class, "chain" to Chain::class).map { it.toSpec().build() })
            .returns(Action::class)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("when(action){")

        actionElements
            .distinctBy { it.action.actionName }
            .forEach { parentFunc ->
                invoqueBuilder.addStatement(parentFunc.toWhenClause())
                storeMap
                    .filter { it.value.subscribedMethods.map { it.action.actionName }.contains(parentFunc.action.actionName) }
                    .forEach { _, storeModel ->
                        invoqueBuilder.addStatement(storeModel.generateFunctionCall(parentFunc))
                    }
                invoqueBuilder.addStatement("}")
            }

        return addFunction(
            invoqueBuilder
                .addStatement("}")
                .addStatement("return action")
                .build())
    }
}

fun getStoreMapType(): ParameterizedTypeName {
    val anyType = WildcardTypeName.subtypeOf(ANY) // <*>
    val storeType = ClassName("mini", "Store") //Store
    val anyStoreType = ParameterizedTypeName.get(storeType, anyType) //Store<*>
    val kClassType = ClassName("java.lang", "Class") //Class //TODO change to KClass?
    val storeClass = ParameterizedTypeName.get(kClassType, anyType) //Class<*>
    val kotlinMapType = ClassName("kotlin.collections", "Map") //Map
    //Generated the parameterized constructor
    return ParameterizedTypeName.get(kotlinMapType, storeClass, anyStoreType) //Map<Class<*>, Store<*>>
}

typealias PoetFunc = Pair<String, KClass<*>>
fun PoetFunc.toSpec() = ParameterSpec.builder(first, second)