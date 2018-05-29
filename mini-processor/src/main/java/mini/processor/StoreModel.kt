package mini.processor

import javax.lang.model.element.Element

class StoreModel(annotatedClass: Element) {
    val packageName: String = annotatedClass.getPackageName()
    val className: String = annotatedClass.simpleName.toString()
}

class StoreMethod(reducerFunc: ReducerFuncModel) {
    val storeName = reducerFunc.parentClass.simpleName.toString()
    val stateGetter = "${storeName.toLowerCase()}.state"
    val methodCall: String
    val priority: Int = reducerFunc.priority

    init {
        val constructor = if (reducerFunc.actionParamPosition == 0) "action,$stateGetter" else "$stateGetter, action"
        methodCall = "${storeName.toLowerCase()}.setStateInternal(${storeName.toLowerCase()}.${reducerFunc.funcName}($constructor))"
    }
}
