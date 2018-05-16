package mini.processor

import mini.Reducer
import javax.lang.model.element.ExecutableElement

class ReducerFuncModel(executableElement: ExecutableElement) {
    val action: ActionModel = ActionModel(executableElement.parameters[0].asType().asElement())
    val funcName : String = executableElement.simpleName.toString()
    val parentClass = executableElement.enclosingElement
    val priority = executableElement.getAnnotation(Reducer::class.java).priority
}