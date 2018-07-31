package mini.processor

import com.squareup.kotlinpoet.*

fun ClassName.wildcardType(): ParameterizedTypeName {
    val anyType = WildcardTypeName.subtypeOf(ANY) // <*>
    return ParameterizedTypeName.get(this, anyType) //Store<*>
}

fun <T : TypeName> T.listTypeName(): ParameterizedTypeName {
    val listClass = ClassName("kotlin.collections", "List")
    return ParameterizedTypeName.get(listClass, this)
}

fun <T : TypeName> T.arrayListTypeName(): ParameterizedTypeName {
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

fun setTypeOf(valueClass: ParameterizedTypeName): ParameterizedTypeName {
    val kotlinMapType = ClassName("kotlin.collections", "Set") //Map
    //Generated the parameterized constructor
    return ParameterizedTypeName.get(kotlinMapType, valueClass)
}

fun linesOfCode(vararg linesOfCode: String) = CodeBlock.of(linesOfCode.joinToString(separator = "\n"))