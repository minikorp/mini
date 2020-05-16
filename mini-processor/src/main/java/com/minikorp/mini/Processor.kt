package com.minikorp.mini

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion

class Processor {

    val supportedAnnotationTypes: MutableSet<String> = mutableSetOf(
            Reducer::class.java, Action::class.java)
            .map { it.canonicalName }.toMutableSet()
    val supportedSourceVersion: SourceVersion = SourceVersion.RELEASE_8

    fun init(environment: ProcessingEnvironment) {
        env = environment
        typeUtils = env.typeUtils
        elementUtils = env.elementUtils
    }

    fun process(roundEnv: RoundEnvironment): Boolean {

        val roundActions = roundEnv.getElementsAnnotatedWith(Action::class.java)
        val roundReducers = roundEnv.getElementsAnnotatedWith(Reducer::class.java)

        if (roundActions.isEmpty()) return false

        val className = ClassName.bestGuess(AUTO_STATIC_DISPATCHER)
        val file = FileSpec.builder(className.packageName, className.simpleName)
        val container = TypeSpec.objectBuilder(className)
                .addKdoc("Automatically generated, do not edit.\n")
                .addSuperinterface(AutoDispatcher::class)

        //Get non-abstract actions
        try {
            ActionTypesGenerator.generate(container, roundActions)
            ReducersGenerator.generate(container, roundReducers)
        } catch (e: Throwable) {
            if (e !is ProcessorException) {
                logError(
                        "Compiler crashed, open an issue please!\n" +
                                " ${e.stackTraceString()}"
                )
            }
        }

        file.addType(container.build())
        file.build().writeToFile(sourceElements = *((roundActions + roundReducers).toTypedArray()))

        return true
    }
}
