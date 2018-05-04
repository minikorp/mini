package com.example.mini_processor

import com.example.mini_commons.Reducer
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement


class Store(annotatedClass: Element) {
    val className: String = annotatedClass.simpleName.toString()
    val subscribedMethods: MutableList<ReducerFunc> = annotatedClass.enclosedElements
            .filter { it.isMethod }
            .filter { it.hasAnnotation(Reducer::class.java) }
            .map { ReducerFunc(it as ExecutableElement) }
            .toMutableList()
}