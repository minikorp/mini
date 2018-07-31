package mini.processor

import mini.Reducer
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

data class ReducerFuncModel(private val executableElement: ExecutableElement) {
    val tag: TypeMirror
    val funcName: String = executableElement.simpleName.toString()
    val storeElement: Element = executableElement.enclosingElement
    val storeFieldName = storeElement.simpleName.toString().toLowerCase()
    val priority = executableElement.getAnnotation(Reducer::class.java).priority
    val hasStateParameter: Boolean

    init {
        val paramsSize = executableElement.parameters.size

        compileCheck(message = "Reducer functions must receive an action, and optionally the state of the store",
            check = paramsSize in 1..2,
            element = executableElement)  //We must receive an action and the state is optional
        compileCheck(message = "Reducer functions must return the state of the store as a parameter",
            check = executableElement.returnType isSubtypeOf storeElement.getSuperClassTypeParameter(0).asType(),
            element = executableElement)
        compileCheck(message = "Reducer functions must be public",
            check = isPublicMethod(executableElement),
            element = executableElement)

        hasStateParameter = paramsSize == 2
        if (hasStateParameter) {
            compileCheck(message = "Reducer functions second parameter must be the store state",
                check = isStoreState(executableElement.parameters[1]),
                element = executableElement
            )
        }

        tag = executableElement.parameters[0].asType()
    }

    private fun isAnAction(element: VariableElement): Boolean {
        return element.asType().getSupertypes().any { it.asElement().qualifiedName() == "mini.Action" }
    }

    private fun isStoreState(element: VariableElement): Boolean {
        return storeElement.getSuperClassTypeParameter(0).asType() == element.asType()
    }

    private fun isPublicMethod(element: ExecutableElement): Boolean {
        return element.modifiers.any { it == Modifier.PUBLIC }
    }

}