package mini.processor

import mini.Reducer
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

data class ReducerFuncModel(private val executableElement: ExecutableElement) {
    companion object {
        const val PARAMS_SIZE_ERROR = "Reducer functions must receive an action, and optionally the state of the store"
        const val PARAMS_ORDER_ERROR = "Reducer functions second parameter must be the store state"
        const val RETURN_STATE_ERROR = "Reducer functions must return the state of the store as a parameter"
        const val PUBLIC_FUN_ERROR = "Reducer functions must be public"
    }

    val parameterType: ReducerFunctionParameterModel
    val funcName: String = executableElement.simpleName.toString()
    val storeElement: Element = executableElement.enclosingElement
    val storeFieldName = storeElement.simpleName.toString().toLowerCase()
    val priority = executableElement.getAnnotation(Reducer::class.java).priority
    val hasStateParameter: Boolean

    init {
        val paramsSize = executableElement.parameters.size

        compileCheck(message = PARAMS_SIZE_ERROR,
                check = paramsSize in 1..2,
                element = executableElement)  //We must receive an action and the state is optional
        compileCheck(message = RETURN_STATE_ERROR,
                check = executableElement.returnType isSubtypeOf storeElement.getSuperClassTypeParameter(0).asType(),
                element = executableElement)
        compileCheck(message = PUBLIC_FUN_ERROR,
                check = isPublicMethod(executableElement),
                element = executableElement)

        hasStateParameter = paramsSize == 2
        if (hasStateParameter) {
            compileCheck(message = PARAMS_ORDER_ERROR,
                    check = isStoreState(executableElement.parameters[1]),
                    element = executableElement
            )
        }

        parameterType = ReducerFunctionParameterModel(executableElement.parameters[0])
    }

    private fun isStoreState(element: VariableElement): Boolean {
        return storeElement.getSuperClassTypeParameter(0).asType().qualifiedName() == element.asType().qualifiedName()
    }

    private fun isPublicMethod(element: ExecutableElement): Boolean {
        return element.modifiers.any { it == Modifier.PUBLIC }
    }

}