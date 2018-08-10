package mini.processor

import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

class ReducerFunctionParameterModel(val element: Element) {
    private val actionName = element.simpleName.toString()
    val parameterTypes: List<ParameterTypeModel> = recursiveActionTags(element.asType(), 0)
        .distinctBy { it.typeMirror.qualifiedName() }

    override fun toString(): String {
        return "ActionModel(actionName='$actionName')"
    }

    private fun recursiveActionTags(mirror: TypeMirror, depth: Int): List<ParameterTypeModel> {
        return (typeUtils.directSupertypes(mirror) ?: emptyList())
            .map { recursiveActionTags(it, depth + 1) }
            .flatten()
            .plus(ParameterTypeModel(mirror, depth))
    }
}

class ParameterTypeModel(val typeMirror: TypeMirror, val depth: Int) {
    override fun toString(): String {
        return "Tag($typeMirror)"
    }
}