package mini.processor

import javax.lang.model.element.Element

class ActionModel(element: Element) {
    val actionName = element.simpleName.toString()
    val packageName: String = element.getPackageName()
}