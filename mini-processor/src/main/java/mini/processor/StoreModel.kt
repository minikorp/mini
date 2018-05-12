package mini.processor

import mini.Reducer
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

class StoreModel(annotatedClass: Element) {
    val packageName: String = annotatedClass.getPackageName()
    val className: String = annotatedClass.simpleName.toString()
    val subscribedMethods: MutableList<ReducerFunc> = annotatedClass.enclosedElements
        .filter { it.isMethod }
        .filter { it.hasAnnotation(Reducer::class.java) }
        .map { ReducerFunc(it as ExecutableElement) }
        .toMutableList()

    fun generateFunctionCall(func: ReducerFunc): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("${className.toLowerCase()}.${func.funcName}(action)")
        return stringBuilder.toString()
    }
}