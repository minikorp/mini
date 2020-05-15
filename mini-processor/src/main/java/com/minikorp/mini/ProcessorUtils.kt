package com.minikorp.mini

import com.squareup.kotlinpoet.FileSpec
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation

lateinit var env: ProcessingEnvironment
lateinit var elementUtils: Elements
lateinit var typeUtils: Types

fun Throwable.stackTraceString(): String {
    val out = ByteArrayOutputStream()
    printStackTrace(PrintStream(out))
    return out.toString()
}

val Element.isMethod: Boolean get() = this.kind == ElementKind.METHOD
val Element.isClass: Boolean get() = this.kind == ElementKind.CLASS

fun ExecutableElement.isSuspending(): Boolean {
    return parameters.last().asType().toString().startsWith("kotlin.coroutines.Continuation")
}

fun TypeMirror.asElement(): Element = asElementOrNull()!!
fun TypeMirror.asElementOrNull(): Element? = typeUtils.asElement(this)

fun TypeMirror.getSupertypes(): MutableList<out TypeMirror> = typeUtils.directSupertypes(this)
fun TypeMirror.getAllSuperTypes(depth: Int = 0): Set<TypeMirror> {
    //We want to add by depth
    val superTypes = typeUtils.directSupertypes(this).toSet()
            .map { it.getAllSuperTypes(depth + 1) }
            .flatten()
    return setOf(this) + superTypes
}

fun TypeMirror.qualifiedName(): String = toString()
fun Element.qualifiedName(): String = toString()
fun Element.getPackageName(): String {
    //xxx.xxx.simpleName
    //xxx.xxx
    val fullName = toString()
    val simpleNameLength = simpleName.toString().count() + 1 //Remove the last dot
    return fullName.dropLast(simpleNameLength)
}

infix fun TypeMirror.assignableTo(base: TypeMirror): Boolean = typeUtils.isAssignable(base, this)
infix fun TypeMirror.isSubtypeOf(base: TypeMirror): Boolean = typeUtils.isSubtype(this, base)

fun Element.asTypeElement(): TypeElement = asTypeElementOrNull()!!
fun Element.asTypeElementOrNull(): TypeElement? = this as? TypeElement

fun TypeMirror.asTypeElement(): TypeElement = asTypeElementOrNull()!!
fun TypeMirror.asTypeElementOrNull(): TypeElement? = asElementOrNull()?.asTypeElementOrNull()

fun TypeMirror.asDeclaredType(): DeclaredType = asDeclaredTypeOrNull()!!
fun TypeMirror.asDeclaredTypeOrNull(): DeclaredType? = this as? DeclaredType

infix fun TypeMirror.isSameType(other: TypeMirror?): Boolean {
    if (other == null) return false
    return typeUtils.isSameType(this, other)
}

fun Element.getSuperClass() = asTypeElement().superclass.asElement()
fun Element.getSuperClassTypeParameter(position: Int) = asTypeElement()
        .superclass.asDeclaredType().typeArguments[position].asElement()


class ProcessorException : IllegalStateException()

fun compilePrecondition(check: Boolean,
                        message: String,
                        element: Element? = null) {
    if (!check) {
        logError(message, element)
        throw ProcessorException()
    }
}

fun logError(message: String, element: Element? = null) {
    logMessage(Diagnostic.Kind.ERROR, message, element)
}

fun logWarning(message: String, element: Element? = null) {
    logMessage(Diagnostic.Kind.MANDATORY_WARNING, message, element)
}

fun logMessage(kind: Diagnostic.Kind, message: String, element: Element? = null) {
    env.messager.printMessage(kind, "\n" + message, element)
}

//KotlinPoet utils

fun FileSpec.writeToFile(vararg sourceElements: Element) {
    val kotlinFileObject = env.filer
            .createResource(StandardLocation.SOURCE_OUTPUT, packageName, "$name.kt", *sourceElements)
    val openWriter = kotlinFileObject.openWriter()
    writeTo(openWriter)
    openWriter.close()
}
