package com.minikorp.mini.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.minikorp.mini.TypedReducer
import com.minikorp.mini.TypedReducerRoot
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName

private val reducerClasses = HashMap<String, ReducerGenerator>()

fun processTypedReducerClasses(): List<Generator> {
    val reducerSymbols = resolver.getSymbolsWithAnnotation(TypedReducerRoot::class.qualifiedName.toString())
    if (reducerSymbols.isEmpty()) return emptyList()

    reducerSymbols.forEach { symbol ->
        symbol.accept(object : KSDefaultVisitor<Unit, Unit>() {
            override fun defaultHandler(node: KSNode, data: Unit) = Unit
            override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
                val container = function.parentDeclaration as? KSClassDeclaration
                function.returnType
                if (container == null) {
                    logger.error("@TypedReducer functions must be declared inside a class")
                    return
                }
                val id = ReducerGenerator.id(container)
                if (reducerClasses[id] == null) {
                    reducerClasses[id] = ReducerGenerator(container)
                }
            }
        }, Unit)
    }

    return reducerClasses.values.toList()
}

class ReducerGenerator(val classDeclaration: KSClassDeclaration) : Generator {

    private val containerClassName = classDeclaration.asClassName()
    private val fileSpec = FileSpec.builder(containerClassName.packageName, containerClassName.simpleName + "TypedReducer")
    private val rootFunction = classDeclaration
            .getAllFunctions().filter {
                it.isAnnotatedWith(TypedReducerRoot::class)
            }.let { roots ->
                if (roots.size != 1) {
                    throw CompilationException(
                            classDeclaration,
                            "Expected exactly one function annotated with @${TypedReducerRoot::class.simpleName}, " +
                                    "found ${roots.size}")

                }
                ReducerAnnotatedFunction(roots[0])
            }

    private val functions = classDeclaration.getAllFunctions()
            .filter { it.isAnnotatedWith(TypedReducer::class) }
            .map { ReducerAnnotatedFunction(it) }

    private val extensionFunctionName = "${rootFunction.name}Typed"

    companion object {
        fun id(declaration: KSClassDeclaration): String = declaration.qualifiedName!!.asString()
    }

    override val id: String = id(classDeclaration)

    override fun initialize() {

    }

    override fun emit() {
        val whenCase = CodeBlock.builder()
                .beginControlFlow("return when (action)")
                .indent()
                .indent()
                .apply {
                    functions.forEach { function ->
                        add("is %T -> ", function.actionParam.type)
                        add(function.callCodeBlock)
                    }
                }
                .add("else -> null\n")
                .endControlFlow()
                .build()

        fileSpec.addFunction(FunSpec.builder(extensionFunctionName)
                .receiver(containerClassName)
                .addCode(whenCase)
                .addModifiers(if (rootFunction.suspending) listOf(KModifier.SUSPEND) else emptyList())
                .addParameters(rootFunction.params.map { ParameterSpec.builder(it.name, it.type).build() })
                .returns(rootFunction.returnType.copy(nullable = true))
                .build())

        codeGenerator.createNewFile(fileSpec.build(), Dependencies.ALL_FILES)
    }

    data class ReducerAnnotatedFunction(val functionDeclaration: KSFunctionDeclaration) {

        val name: String = functionDeclaration.simpleName.asString()

        val returnType: TypeName = (functionDeclaration.returnType?.resolve()?.declaration as? KSClassDeclaration)
                ?.asClassName()
                ?: throw CompilationException(functionDeclaration, "Couldn't resolve return type")

        val params = functionDeclaration.parameters.map { Param(it) }

        val actionParam: Param = params.find { it.name == "action" }
                ?: throw CompilationException(functionDeclaration, "Missing action parameter")

        val suspending = functionDeclaration.modifiers.contains(Modifier.SUSPEND)

        val callCodeBlock = CodeBlock.builder()
                .addStatement("$name(${params.joinToString(separator = ", ") { "${it.name} = ${it.name}" }})",
                        *params.map { it.type }.toTypedArray())
                .build()
    }

    data class Param(val declaration: KSValueParameter) {
        val name = declaration.name?.getShortName() ?: "param${hashCode()}"
        val type = declaration.type.asTypeName()
    }
}