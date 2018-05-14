package mini.processor

import javax.lang.model.element.Element

class StoreModel(annotatedClass: Element) {
    val packageName: String = annotatedClass.getPackageName()
    val className: String = annotatedClass.simpleName.toString()
}

class StoreMethod(reducerFunc: ReducerModelFunc) {
    val storeName = reducerFunc.parentClass.simpleName.toString()
    val methodCall: String = "${storeName.toLowerCase()}.${reducerFunc.funcName}"
    val priority: Int = reducerFunc.priority
}
