package mini.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import mini.ReducerFun
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

fun generateReducer(vararg elements : TypeElement): ActionReducerModel {
    val reducersList = elements
            .map { it.enclosedElements }
            .flatten()
        .filter { it.isMethod && it.getAnnotation(ReducerFun::class.java) != null }
            .map { ReducerFuncModel(it as ExecutableElement) }

    return ActionReducerModel(reducersList, actionTypes)
}


fun getElement(`class`: Class<*>, elements: Elements): TypeElement {
    return elements.getTypeElement(`class`.canonicalName)
}

fun toString(typeSpec: TypeSpec, packageName : String): String {
    return FileSpec.get(packageName, typeSpec).toString()
}