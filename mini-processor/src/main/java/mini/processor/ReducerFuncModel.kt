package mini.processor

import mini.Reducer
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class ReducerFuncModel(executableElement: ExecutableElement) {
    val action: ActionModel
    val actionParamPosition: Int
    val state: StateModel
    val stateParamPosition: Int
    val funcName: String = executableElement.simpleName.toString()
    val parentClass = executableElement.enclosingElement
    val priority = executableElement.getAnnotation(Reducer::class.java).priority

    init {
        check(executableElement.parameters.size == 2) //We just receive an action and a state
        check(executableElement.returnType isSubtypeOf parentClass.getSuperClassTypeParameter(0).asType())
        check(isPublicMethod(executableElement))

        actionParamPosition = executableElement.parameters.indexOfFirst { isAnAction(it) }
        stateParamPosition = executableElement.parameters.indexOfFirst { isStoreState(it) }
        action = ActionModel(executableElement.parameters[actionParamPosition].asType().asElement())
        state = StateModel(executableElement.parameters[stateParamPosition].asType().asElement())
    }

    private fun isAnAction(element: VariableElement): Boolean {
        return element.asType().getSupertypes().any { it.asElement().toString() == "mini.Action" }
    }

    private fun isStoreState(element: VariableElement): Boolean {
        return parentClass.getSuperClassTypeParameter(0).asType() == element.asType()
    }

    private fun isPublicMethod(element: ExecutableElement): Boolean {
        return element.modifiers.any { it == Modifier.PUBLIC }
    }
}