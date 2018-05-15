package mini.processor

import com.squareup.kotlinpoet.*

var depth = 0

fun ClassName.wildcardType(): ParameterizedTypeName {
    val anyType = WildcardTypeName.subtypeOf(ANY) // <*>
    return ParameterizedTypeName.get(this, anyType) //Store<*>
}

fun ClassName.listType(): ParameterizedTypeName {
    val listClass = ClassName("kotlin.collections", "List")
    return ParameterizedTypeName.get(listClass, this)
}

fun ClassName.arrayListType(): ParameterizedTypeName {
    val listClass = ClassName("kotlin.collections", "ArrayList")
    return ParameterizedTypeName.get(listClass, this)
}

fun mapTypeOf(keyClass: ParameterizedTypeName, valueClass: ParameterizedTypeName): ParameterizedTypeName {
    val kotlinMapType = ClassName("kotlin.collections", "Map") //Map
    //Generated the parameterized constructor
    return ParameterizedTypeName.get(kotlinMapType, keyClass, valueClass)
}

fun mapTypeOf(keyClass: ClassName, valueClass: ClassName): ParameterizedTypeName {
    val kotlinMapType = ClassName("kotlin.collections", "Map") //Map
    //Generated the parameterized constructor
    return ParameterizedTypeName.get(kotlinMapType, keyClass, valueClass)
}

fun indent(): String {
    if (depth == 0) return ""
    return "    ".repeat(depth)
}

inline fun <T> T.nest(func: T.() -> Unit): T {
    depth++
    func()
    depth--
    return this
}

fun FunSpec.Builder.addIndentedStatement(statement: String, vararg args: Any): FunSpec.Builder {
    addStatement(indent() + statement, args)
    return this
}