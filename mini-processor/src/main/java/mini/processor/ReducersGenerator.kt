package mini.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import mini.Dispatcher
import mini.Reducer
import mini.Store
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

object ReducersGenerator {

    fun generate(container: TypeSpec.Builder, elements: Set<Element>) {
        val reducers = elements.map { ReducerModel(it) }
            .groupBy { it.storeName }

        val whenBlock = CodeBlock.builder()
            //⇤⇥«»
            .beginControlFlow("stores.forEach { store ->")
            .addStatement("when (store) {").indent()
            .apply {
                reducers.forEach { (storeName, reducers) ->
                    addStatement("is %T -> {", storeName).indent()
                    reducers.forEach { reducer ->
                        addStatement("dispatcher.register<%T>(priority=%L) { store.%N(it) }",
                            reducer.function.parameters[0].asType(), //Action type
                            reducer.priority, //Priority
                            reducer.function.simpleName //Function name
                        )
                    }
                    unindent().addStatement("}")
                }
            }
            .unindent().addStatement("}")
            .endControlFlow()
            .build()

        val storeTypeName = Store::class.asTypeName().parameterizedBy(STAR)
        val listOfStoresTypeName = List::class.asTypeName().parameterizedBy(storeTypeName)

        val registerListFunction = FunSpec.builder("register")
            .addAnnotation(JvmStatic::class)
            .addParameter("dispatcher", Dispatcher::class)
            .addParameter("stores", listOfStoresTypeName)
            .addCode(whenBlock)
            .build()

        val registerOneFunction = FunSpec.builder("register")
            .addAnnotation(JvmStatic::class)
            .addParameter("dispatcher", Dispatcher::class)
            .addParameter("store", storeTypeName)
            .addCode(CodeBlock.of(
                "register(dispatcher, listOf(store))"
            )).build()

        container.addFunction(registerOneFunction)
        container.addFunction(registerListFunction)

    }
}

class ReducerModel(val element: Element) {
    val priority = element.getAnnotation(Reducer::class.java).priority
    val function = element as ExecutableElement
    val store = element.enclosingElement.asType()
    val storeName = store.asTypeName()
}