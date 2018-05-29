package mini.processor

import mini.processor.ProcessorUtils.env
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

object ProcessorUtils {
    lateinit var env: ProcessingEnvironment
}

val Element.isMethod: Boolean get() = this.kind == ElementKind.METHOD

val Element.isClass: Boolean get() = this.kind == ElementKind.CLASS

fun TypeMirror.asElement(): Element = asElementOrNull()!!

fun TypeMirror.asElementOrNull(): Element? = env.typeUtils.asElement(this)

fun TypeMirror.getSupertypes(): MutableList<out TypeMirror> = env.typeUtils.directSupertypes(this)

fun Element.getPackageName(): String {
    //xxx.xxx.simpleName
    //xxx.xxx
    val fullName = toString()
    val simpleNameLength = simpleName.toString().count() + 1 //Remove the last dot
    return fullName.dropLast(simpleNameLength)
}

infix fun TypeMirror.assignableTo(base: TypeMirror): Boolean {
    return env.typeUtils.isAssignable(base, this)
}


infix fun TypeMirror.isSubtypeOf(base: TypeMirror): Boolean {
    return env.typeUtils.isSubtype(this, base)
}

fun Element.asTypeElement(): TypeElement = asTypeElementOrNull()!!
fun Element.asTypeElementOrNull(): TypeElement? = this as? TypeElement

fun TypeMirror.asTypeElement(): TypeElement = asTypeElementOrNull()!!
fun TypeMirror.asTypeElementOrNull(): TypeElement? = asElementOrNull()?.asTypeElementOrNull()

fun TypeMirror.asDeclaredType(): DeclaredType = asDeclaredTypeOrNull()!!
fun TypeMirror.asDeclaredTypeOrNull(): DeclaredType? = this as? DeclaredType

fun Element.getSuperClass() = asTypeElement().superclass.asElement()
fun Element.getSuperClassTypeParameter(position : Int) = asTypeElement().superclass.asDeclaredType().typeArguments[position].asElement()

