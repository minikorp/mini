package mini.processor

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import mini.CompositeCloseable
import mini.Dispatcher
import mini.Reducer
import java.io.Closeable
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier

object ReducersGenerator {

    fun generate(container: TypeSpec.Builder, elements: Set<Element>) {
        val reducers = elements.map { ReducerModel(it) }
            .groupBy { it.containerName }

        val reducerContainerType = Any::class.asTypeName()
        val reducerContainerListType = List::class.asTypeName()
            .parameterizedBy(reducerContainerType)

        val whenBlock = CodeBlock.builder()
            .addStatement("val c = %T()", CompositeCloseable::class)
            .addStatement("when (container) {").indent()
            .apply {
                reducers.forEach { (containerName, reducerFunctions) ->
                    addStatement("is %T -> {", containerName).indent()
                    reducerFunctions.forEach { function ->
                        addStatement("c.add(dispatcher.subscribe<%T>(priority=%L) { container.%N(it) })",
                            function.function.parameters[0].asType(), //Action type
                            function.priority, //Priority
                            function.function.simpleName //Function name
                        )
                    }
                    unindent().addStatement("}")
                }
            }
            .addStatement("else -> throw IllegalArgumentException(\"Container \$container has no reducers\")")
            .unindent()
            .addStatement("}") //Close when
            .addStatement("return c")
            .build()

        val registerOneFn = FunSpec.builder("subscribe")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("dispatcher", Dispatcher::class)
            .addParameter("container", reducerContainerType)
            .returns(Closeable::class)
            .addCode(whenBlock)
            .build()

        val registerListFn = FunSpec.builder("subscribe")
            .addParameter("dispatcher", Dispatcher::class)
            .addParameter("containers", reducerContainerListType)
            .returns(Closeable::class)
            .addStatement("val c = %T()", CompositeCloseable::class)
            .beginControlFlow("containers.forEach { container ->")
            .addStatement("c.add(subscribe(dispatcher, container))")
            .endControlFlow()
            .addStatement("return c")
            .build()

        val initDispatcherFn = FunSpec.builder("newDispatcher")
            .returns(Dispatcher::class)
            .addCode(CodeBlock.builder()
                .addStatement("return Dispatcher(actionTypes)")
                .build())
            .build()

        container.addFunction(initDispatcherFn)
        container.addFunction(registerOneFn)
        container.addFunction(registerListFn)

    }
}

class ReducerModel(element: Element) {
    val priority = element.getAnnotation(Reducer::class.java).priority
    val function = element as ExecutableElement
    private val container = element.enclosingElement.asType()
    val containerName = container.asTypeName()

    init {
        compilePrecondition(element, function.parameters.size == 1) {
            "Reducer functions expect exactly one parameter (action), " +
                "found ${function.parameters.size}: [${function.parameters}]"
        }
        compilePrecondition(element, function.modifiers.contains(Modifier.PUBLIC)) {
            "Reducer functions must be public, found: ${function.modifiers}"
        }
    }
}