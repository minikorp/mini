package mini.processor

import javax.lang.model.element.ExecutableElement

class ReducerFunc(executableElement: ExecutableElement) {
    val action: ActionModel = ActionModel(executableElement.parameters[0].asType().asElement())
    val funcName : String = executableElement.simpleName.toString()
    val parentClass = executableElement.enclosingElement

    fun toWhenClause() : String{
        return "is ${action.actionName} -> {"
    }
}