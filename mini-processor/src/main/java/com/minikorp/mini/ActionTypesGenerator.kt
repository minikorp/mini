package com.minikorp.mini

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

object ActionTypesGenerator {
    fun generate(container: TypeSpec.Builder, elements: Set<Element>) {

        val actionModels = elements.filter { Modifier.ABSTRACT !in it.modifiers }
                .map { ActionModel(it) }

        container.apply {
            val anyClassTypeName = KClass::class.asTypeName().parameterizedBy(STAR)
            val listTypeName = List::class.asTypeName().parameterizedBy(anyClassTypeName)
            val mapType = Map::class
                    .asClassName()
                    .parameterizedBy(anyClassTypeName, listTypeName)

            val prop = PropertySpec.builder(Mini::actionTypes.name, mapType)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(CodeBlock.builder()
                            .add("mapOf(\n⇥")
                            .apply {
                                actionModels.forEach { actionModel ->
                                    val comma = if (actionModel != actionModels.last()) "," else ""
                                    add("«")
                                    add("%T::class to ", actionModel.typeName)
                                    add(actionModel.listOfSupertypesCodeBlock())
                                    add(comma)
                                    add("\n»")
                                }
                            }
                            .add("⇤)")
                            .build())
            addProperty(prop.build())
        }.build()
    }
}

class ActionModel(element: Element) {
    private val type = element.asType()
    private val javaObject = ClassName.bestGuess("java.lang.Object")

    val typeName = type.asTypeName()
    private val superTypes = collectTypes(type)
            .sortedBy { it.depth }
            .filter { it.typeName != javaObject }
            .map { it.typeName }
            .plus(ANY)

    fun listOfSupertypesCodeBlock(): CodeBlock {
        val format = superTypes.joinToString(",\n") { "%T::class" }
        val args = superTypes.toTypedArray()
        return CodeBlock.of("listOf($format)", *args)
    }

    private fun collectTypes(mirror: TypeMirror, depth: Int = 0): Set<ActionSuperType> {
        //We want to add by depth
        val superTypes = typeUtils.directSupertypes(mirror).toSet()
                .map { collectTypes(it, depth + 1) }
                .flatten()
        return setOf(ActionSuperType(mirror.asTypeName(), depth)) + superTypes
    }

    class ActionSuperType(val typeName: TypeName, val depth: Int) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ActionSuperType
            if (typeName != other.typeName) return false
            return true
        }

        override fun hashCode(): Int {
            return typeName.hashCode()
        }
    }
}