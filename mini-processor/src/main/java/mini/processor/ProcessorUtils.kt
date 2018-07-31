package mini.processor

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

lateinit var env: ProcessingEnvironment
lateinit var elementUtils: Elements
lateinit var typeUtils: Types

val Element.isMethod: Boolean get() = this.kind == ElementKind.METHOD

val Element.isClass: Boolean get() = this.kind == ElementKind.CLASS

fun TypeMirror.asElement(): Element = asElementOrNull()!!

fun TypeMirror.asElementOrNull(): Element? = typeUtils.asElement(this)

fun TypeMirror.getSupertypes(): MutableList<out TypeMirror> = typeUtils.directSupertypes(this)

fun TypeMirror.qualifiedName(): String {
    //toString returns the full name
    return toString()
}

fun Element.qualifiedName(): String {
    //toString returns the full name
    return toString()
}

fun Element.getPackageName(): String {
    //xxx.xxx.simpleName
    //xxx.xxx
    val fullName = toString()
    val simpleNameLength = simpleName.toString().count() + 1 //Remove the last dot
    return fullName.dropLast(simpleNameLength)
}

infix fun TypeMirror.assignableTo(base: TypeMirror): Boolean {
    return typeUtils.isAssignable(base, this)
}

infix fun TypeMirror.isSubtypeOf(base: TypeMirror): Boolean {
    return typeUtils.isSubtype(this, base)
}

fun Element.asTypeElement(): TypeElement = asTypeElementOrNull()!!
fun Element.asTypeElementOrNull(): TypeElement? = this as? TypeElement

fun TypeMirror.asTypeElement(): TypeElement = asTypeElementOrNull()!!
fun TypeMirror.asTypeElementOrNull(): TypeElement? = asElementOrNull()?.asTypeElementOrNull()

fun TypeMirror.asDeclaredType(): DeclaredType = asDeclaredTypeOrNull()!!
fun TypeMirror.asDeclaredTypeOrNull(): DeclaredType? = this as? DeclaredType

infix fun TypeMirror.sameType(other: TypeMirror?): Boolean {
    if (other == null) return false
    return typeUtils.isSameType(this, other)
}

fun Element.getSuperClass() = asTypeElement().superclass.asElement()
fun Element.getSuperClassTypeParameter(position: Int) = asTypeElement().superclass.asDeclaredType().typeArguments[position].asElement()

fun compileCheck(message: String = "Compilation error", check: Boolean, element: Element? = null) {
    if (check) return
    else logError(message, element)
}

fun logError(message: String, element: Element? = null) {
    logMessage(Diagnostic.Kind.ERROR, message, element)
    error("Compilation aborted")
}

fun logWarning(message: String, element: Element? = null) {
    logMessage(Diagnostic.Kind.MANDATORY_WARNING, message, element)
}

fun logMessage(kind: Diagnostic.Kind, message: String, element: Element? = null) {
    if (DEBUG_MODE) env.messager.printMessage(kind, message, element)
}

