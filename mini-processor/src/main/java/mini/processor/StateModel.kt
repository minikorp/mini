package mini.processor

import javax.lang.model.element.Element

class StateModel(element: Element) {
    val stateName = element.simpleName.toString()
    val stateType = element.asType()
    val packageName: String = element.getPackageName()
}