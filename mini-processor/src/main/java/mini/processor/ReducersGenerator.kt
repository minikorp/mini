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
            .build()

        val storeTypeName = Store::class.asTypeName().parameterizedBy(STAR)
        val listOfStoresTypeName = List::class.asTypeName().parameterizedBy(storeTypeName)

        val registerListFn = FunSpec.builder("register")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("dispatcher", Dispatcher::class)
            .addParameter("stores", listOfStoresTypeName)
            .beginControlFlow("stores.forEach { store ->")
            .addStatement("register(dispatcher, store)")
            .endControlFlow()
            .build()

        val registerOneFn = FunSpec.builder("register")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("dispatcher", Dispatcher::class)
            .addParameter("store", storeTypeName)
            .addCode(whenBlock)
            .build()

        val initDispatcherFn = FunSpec.builder("initialize")
            .addParameter("dispatcher", Dispatcher::class)
            .addParameter("stores", listOfStoresTypeName)
            .addCode(CodeBlock.builder()
                .addStatement("dispatcher.actionTypes = actionTypes")
                .addStatement("register(dispatcher, stores)")
                .build())
            .build()

        container.addFunction(registerOneFn)
        container.addFunction(registerListFn)
        container.addFunction(initDispatcherFn)

    }
}

class ReducerModel(val element: Element) {
    val priority = element.getAnnotation(Reducer::class.java).priority
    val function = element as ExecutableElement
    val store = element.enclosingElement.asType()
    val storeName = store.asTypeName()
}