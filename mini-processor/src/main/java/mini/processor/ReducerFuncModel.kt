package mini.processor

import mini.Reducer
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class ReducerFuncModel(executableElement: ExecutableElement) {
    private val stateParamPosition: Int
    val action: ActionModel
    val actionParamPosition: Int
    val state: StateModel?
    val funcName: String = executableElement.simpleName.toString()
    val parentClass: Element = executableElement.enclosingElement
    val priority = executableElement.getAnnotation(Reducer::class.java).priority

    init {
        val paramsSize = executableElement.parameters.size
        compileCheck(message = "Reducer functions must receive an action, and optionally the state of the store",
                check = paramsSize in 1..2,
                element = executableElement)  //We must receive an action and the state is optional
        compileCheck(message = "Reducer functions must return the state of the store as a parameter",
                check = executableElement.returnType isSubtypeOf parentClass.getSuperClassTypeParameter(0).asType(),
                element = executableElement)
        compileCheck(message = "Reducer functions must be public",
                check = isPublicMethod(executableElement),
                element = executableElement)
        compileCheck(message = "Reducer functions must receive an action type",
                check = executableElement.parameters.any { isAnAction(it) },
                element = executableElement)

        actionParamPosition = executableElement.parameters.indexOfFirst { isAnAction(it) }
        action = ActionModel(executableElement.parameters[actionParamPosition].asType().asElement())
        stateParamPosition = executableElement.parameters.indexOfFirst { isStoreState(it) }
        state = if (stateParamPosition > -1) StateModel(executableElement.parameters[stateParamPosition].asType().asElement()) else null
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