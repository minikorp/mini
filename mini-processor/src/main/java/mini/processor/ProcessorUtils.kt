package mini.processor

import mini.processor.ProcessorUtils.env
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror

object ProcessorUtils {
    lateinit var env: ProcessingEnvironment
}

val Element.isMethod: Boolean get() = this.kind == ElementKind.METHOD
val Element.isClass: Boolean get() = this.kind == ElementKind.CLASS
val Element.isConstructor: Boolean get() = this.kind == ElementKind.CONSTRUCTOR
val Element.isInterface: Boolean get() = this.kind == ElementKind.INTERFACE
val Element.isAbstract: Boolean get() = this.modifiers.contains(Modifier.ABSTRACT)
val ExecutableElement.isVoid: Boolean get() = this.returnType.kind == TypeKind.VOID

fun TypeMirror.asElement(): Element = asElementOrNull()!!

fun TypeMirror.asElementOrNull(): Element? = env.typeUtils.asElement(this)

infix fun TypeMirror.assignableTo(base: TypeMirror): Boolean {
    return env.typeUtils.isAssignable(base, this)
}

infix fun TypeMirror.isSubtypeOf(base: TypeMirror): Boolean {
    return env.typeUtils.isSubtype(this, base)
}

fun <T : Annotation> Element.hasAnnotation(type: Class<T>): Boolean {
    return this.annotationMirrors
            .firstOrNull { it.annotationType == type } != null
}

fun Element.getPackageName(): String {
    //xxx.xxx.simpleName
    //xxx.xxx
    val startIndex = toString().count().minus(simpleName.toString().count().plus(1))
    val endIndex = toString().count()
    //simpleName
    return toString().removeRange(startIndex, endIndex)
}