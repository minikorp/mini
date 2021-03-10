package com.minikorp.mini.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.minikorp.mini.Action
import com.minikorp.mini.IdentityReducer
import com.minikorp.mini.Reducer
import com.minikorp.mini.State
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName

private val stateClasses = HashMap<String, StateGenerator>()

fun processStateClasses(): List<Generator> {
    val stateSymbols = resolver.getSymbolsWithAnnotation(State::class.qualifiedName.toString())
    if (stateSymbols.isEmpty()) return emptyList()

    stateSymbols.forEach { symbol ->
        symbol.accept(object : KSDefaultVisitor<Unit, Unit>() {
            override fun defaultHandler(node: KSNode, data: Unit) = Unit
            override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                val id = StateGenerator.id(classDeclaration)
                if (stateClasses[id] == null) {
                    stateClasses[id] = StateGenerator(classDeclaration)
                }
            }
        }, Unit)
    }

    return stateClasses.values.toList()
}

class StateGenerator(private val classDeclaration: KSClassDeclaration) : Generator {

    companion object {
        fun id(classDeclaration: KSClassDeclaration): String {
            return classDeclaration.qualifiedName!!.asString()
        }
    }

    override val id: String = id(classDeclaration)
    private val stateClassName = classDeclaration.asClassName()
    private val storeClassName = ClassName(stateClassName.packageName, stateClassName.simpleName + "Store")
    private val reducerClassName = ClassName(stateClassName.packageName, stateClassName.simpleName + "Reducer")
    private val reducerSupertypeClassName = Reducer::class.asClassName().parameterizedBy(stateClassName)
    private val fileSpec = FileSpec.builder(storeClassName.packageName, storeClassName.simpleName)
    private val selfSlice = Slice("reducer", "reducer", this)
    private val slices = ArrayList<Slice>()

    data class Slice(val name: String,
                     val reducerArgumentName: String,
                     val ref: StateGenerator)

    override fun initialize() {
        if (classDeclaration.modifiers.contains(Modifier.DATA).not()) {
            throw CompilationException(classDeclaration, "@${State::class.java.simpleName} classes must be data classes")
        }
        //Self reducer
        classDeclaration.getAllProperties().forEach { property ->
            property.type.resolve().declaration.accept(object : KSDefaultVisitor<Unit, Unit>() {
                override fun defaultHandler(node: KSNode, data: Unit) = Unit
                override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                    val slice = stateClasses[classDeclaration.id]
                    if (slice != null) {
                        val name = property.simpleName.asString()
                        slices.add(Slice(
                                name = name,
                                reducerArgumentName = name + "Reducer",
                                ref = slice
                        ))
                    }
                }
            }, Unit)
        }
    }

    private fun emitReducer() {
        val reduceBody = CodeBlock.builder()
                .add("return ${selfSlice.name}.reduce(state, action).copy(\n")
                .indent()
                .apply {
                    slices //skip self
                            .forEach { slice ->
                                add("${slice.name}=${slice.reducerArgumentName}.reduce(state.${slice.name}, action),\n")
                            }
                }
                .add(")")
                .build()

        fileSpec.addType(TypeSpec
                .classBuilder(reducerClassName)
                .addSuperinterface(reducerSupertypeClassName)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(
                                selfSlice.name, selfSlice.ref.reducerSupertypeClassName
                        ).defaultValue("%T()", IdentityReducer::class.asClassName()).build())
                        .apply {
                            slices.forEach { slice ->
                                addParameter(slice.reducerArgumentName, slice.ref.reducerSupertypeClassName)
                            }
                        }
                        .build()
                )
                .apply {
                    slices.plus(selfSlice).forEach { slice ->
                        addProperty(PropertySpec
                                .builder(slice.reducerArgumentName, slice.ref.reducerSupertypeClassName)
                                .addModifiers(KModifier.PRIVATE)
                                .initializer(slice.reducerArgumentName)
                                .build())
                    }
                }
                .addFunction(FunSpec.builder("reduce")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("state", stateClassName)
                        .addParameter("action", Action::class.asClassName())
                        .returns(stateClassName)
                        .addCode(reduceBody)
                        .build())
                .build())
    }

    override fun emit() {
        if (slices.isEmpty()) return //This is a leaf, no need to generate a reducer
        emitReducer()
        codeGenerator.createNewFile(fileSpec.build(), Dependencies(
                aggregating = true,
                classDeclaration.containingFile!!
        ))
    }
}