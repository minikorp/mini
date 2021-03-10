package com.minikorp.mini.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import java.io.OutputStream
import kotlin.reflect.KClass
import sun.reflect.generics.visitor.Visitor

//Debug

private const val DEBUG = true
private var lazyDebugFile: OutputStream? = null
fun debugPrint(obj: Any) {
    if (!DEBUG) return
    if (lazyDebugFile == null) {
        lazyDebugFile = codeGenerator.createNewFile(Dependencies.ALL_FILES, "", "Debug")
    }
    val text = "//${obj.toString().replace("\n", "////\n")}"
    lazyDebugFile!!.write("$text\n".toByteArray())
}

fun flushDebugPrint() {
    lazyDebugFile?.flush()
    lazyDebugFile?.close()
    lazyDebugFile = null
}

fun KSName.getPackageName(): String = getQualifier().dropLast(getShortName().length)


//Kotlin Poet Utils

fun KSAnnotated.isAnnotatedWith(annotation: KClass<*>): Boolean {
    val visitor = object : KSDefaultVisitor<Unit, Unit>() {
        override fun defaultHandler(node: KSNode, data: Unit) = Unit
    }
    return annotations.find { it.shortName.asString() == annotation.simpleName } != null
}

val KSClassDeclaration.id get() = asClassName().toString()
fun KSClassDeclaration.asClassName() = ClassName(this.packageName.asString(), this.simpleName.asString())

fun KSTypeReference.asTypeName(): TypeName {
    val baseClassName = (resolve().declaration as KSClassDeclaration).asClassName()
    val args = this.element?.typeArguments ?: emptyList()

    if (args.isNotEmpty()) {
        return baseClassName.parameterizedBy(args.mapNotNull { it.type?.asTypeName() })
    }

    return baseClassName
}

fun CodeGenerator.createNewFile(fileSpec: FileSpec, dependencies: Dependencies) {
    createNewFile(dependencies, fileSpec.packageName, fileSpec.name).use { stream ->
        stream.writer().use {
            fileSpec.writeTo(it)
        }
        stream.flush()
    }
}