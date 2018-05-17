package mini.processor

import com.squareup.kotlinpoet.*

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

var FunSpec.Builder.depth: Int by MutableFieldProperty { 0 }

fun FunSpec.Builder.nestedBlock(statement: String,
                                vararg args: String = emptyArray(),
                                func: FunSpec.Builder.() -> Unit): FunSpec.Builder {
    depth++
    addIndentedStatement("$statement {", args)
    func()
    addIndentedStatement("}")
    depth--
    return this
}

fun FunSpec.Builder.indent(func: FunSpec.Builder.() -> Unit): FunSpec.Builder {
    depth++
    func()
    depth--
    return this
}

fun FunSpec.Builder.addIndentedStatement(statement: String,
                                         vararg args: Any,
                                         indent: String = "    "): FunSpec.Builder {
    val spaces = if (depth == 0) "" else indent.repeat(depth)
    addStatement(spaces + statement, args)
    return this
}