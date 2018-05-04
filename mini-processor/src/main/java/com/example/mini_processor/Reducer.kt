package com.example.mini_processor

import com.example.mini_commons.Action
import javax.lang.model.element.ExecutableElement

class ReducerFunc(executableElement: ExecutableElement) {
    val action: Action = executableElement.parameters[0]!!.constantValue as Action
    val parentClass = executableElement.enclosingElement
}