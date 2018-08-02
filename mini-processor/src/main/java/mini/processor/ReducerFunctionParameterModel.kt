package mini.processor

import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

class ReducerFunctionParameterModel(val element: Element) {
    private val actionName = element.simpleName.toString()
    val tags: List<TagModel> = recursiveActionTags(element.asType())
        .map { TagModel(it) }
        .distinctBy { it.typeMirror.qualifiedName() }

    override fun toString(): String {
        return "ActionModel(actionName='$actionName')"
    }

    private fun recursiveActionTags(mirror: TypeMirror): List<TypeMirror> {
        return (typeUtils.directSupertypes(mirror) ?: emptyList())
            .map { recursiveActionTags(it) }
            .flatten()
            .plus(mirror)
    }
}

class TagModel(val typeMirror: TypeMirror) {
    override fun toString(): String {
        return "Tag($typeMirror)"
    }
}