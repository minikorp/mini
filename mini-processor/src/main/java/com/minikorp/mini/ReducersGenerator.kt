package com.minikorp.mini

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.Closeable
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.DeclaredType

object ReducersGenerator {

    fun generate(container: TypeSpec.Builder, elements: Set<Element>) {
        val reducers = elements.map { ReducerModel(it) }
                .groupBy { it.container.typeName }

        val whenBlock = CodeBlock.builder()
                .addStatement("val c = %T()", CompositeCloseable::class)
                .addStatement("when (container) {").indent()
                .apply {
                    reducers.forEach { (containerName, reducerFunctions) ->
                        addStatement("is %T -> {", containerName).indent()
                        reducerFunctions.forEach { function ->
                            add("c.add(dispatcher.subscribe<%T>(priority=%L) { action -> ",
                                    function.actionTypeName,
                                    function.priority
                            )
                            add(function.generateCallBlock("container", "action"))
                            addStatement("})")
                        }
                        unindent().addStatement("}")
                    }
                }
                .unindent()
                .addStatement("}") //Close when
                .addStatement("return c")
                .build()

        val typeParam = TypeVariableName("T")
        val oneParam = StateContainer::class.asTypeName().parameterizedBy(typeParam)

        val registerOneFn = FunSpec.builder("subscribe")
                .addModifiers(KModifier.OVERRIDE)
                .addTypeVariable(typeParam)
                .addParameter("dispatcher", Dispatcher::class)
                .addParameter("container", oneParam)
                .returns(Closeable::class)
                .addCode(whenBlock)
                .build()

        container.addFunction(registerOneFn)
    }
}

class ReducerModel(element: Element) {
    private val function = element as ExecutableElement

    private val isPure: Boolean
    private val isSuspending: Boolean

    val container: ContainerModel
    val priority = element.getAnnotation(Reducer::class.java).priority

    val actionTypeName: TypeName
    val returnTypeName: TypeName

    init {
        compilePrecondition(
                check = function.modifiers.contains(Modifier.PUBLIC),
                message = "Reducer functions must be public.",
                element = element
        )

        isSuspending = function.isSuspending()
        container = ContainerModel(element.enclosingElement)
        val parameters: List<TypeName>

        if (isSuspending) {
            //Hacky check to get return type of a kotlin continuation
            val continuationTypeParameter = (function.parameters.last().asType() as DeclaredType)
                    .typeArguments[0].asTypeName() as WildcardTypeName
            returnTypeName = continuationTypeParameter.inTypes.first()
            parameters = function.parameters.dropLast(1).map { it.asType().asTypeName() }
        } else {
            returnTypeName = function.returnType.asTypeName()
            parameters = function.parameters.map { it.asType().asTypeName() }
        }

        if (returnTypeName == UNIT) {
            isPure = false
            actionTypeName = function.parameters[0].asType().asTypeName()
            compilePrecondition(
                    check = parameters.size == 1,
                    message = "Expected exactly one action parameter",
                    element = element
            )
        } else {
            isPure = true
            compilePrecondition(
                    check = parameters.size == 2,
                    message = "Expected exactly two parameters, ${container.stateTypeName} and action",
                    element = element
            )
            val stateTypeName = parameters[0]
            actionTypeName = parameters[1]

            compilePrecondition(
                    check = stateTypeName == container.stateTypeName,
                    message = "Expected ${container.stateTypeName} as first state parameter",
                    element = element
            )

            compilePrecondition(
                    check = returnTypeName == container.stateTypeName,
                    message = "Expected ${container.stateTypeName} as return value",
                    element = element
            )
        }
    }

    fun generateCallBlock(containerParam: String, actionParam: String): CodeBlock {

        val receiver = if (container.isStatic) {
            CodeBlock.of("%T.${function.simpleName}", container.typeName)
        } else {
            CodeBlock.of("${containerParam}.${function.simpleName}")
        }

        val call = if (isPure) {
            CodeBlock.of("(${containerParam}.state, $actionParam)")
        } else {
            CodeBlock.of("($actionParam)")
        }

        return if (isPure) {
            CodeBlock.builder()
                    .add("${containerParam}.setState(")
                    .add(receiver)
                    .add(call)
                    .add(")")
                    .build()
        } else {
            CodeBlock.builder()
                    .add(receiver)
                    .add(call)
                    .build()
        }
    }
}

class ContainerModel(element: Element) {
    val typeName: TypeName
    val stateTypeName: TypeName
    val isStatic: Boolean

    init {
        compilePrecondition(
                check = element.kind == ElementKind.CLASS,
                message = "Reducers must be declared inside StoreContainer classes",
                element = element
        )

        val mainTypeName = element.asType().asTypeName()
        val parent = element.enclosingElement

        isStatic = if (parent != null && parent.kind == ElementKind.CLASS) {
            val parentTypeName = parent.asType().asTypeName()
            "$parentTypeName.Companion" == mainTypeName.toString()
        } else {
            false
        }

        val realContainer = if (isStatic) element.enclosingElement else element
        typeName = realContainer.asType().asTypeName()

        val superTypes = realContainer.asType().getAllSuperTypes().map { it.asTypeName() }
        val stateContainerType = superTypes
                .find { it is ParameterizedTypeName && it.rawType == StateContainer::class.asTypeName() }
        compilePrecondition(
                check = stateContainerType != null,
                message = "Reducers must be declared in a StateContainer<T>",
                element = element
        )

        stateTypeName = (stateContainerType!! as ParameterizedTypeName).typeArguments[0]
    }

    override fun toString(): String {
        return stateTypeName.toString()
    }
}
