package mini.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import mini.Action
import mini.Reducer
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

        val className = "MiniGen"
        val file = FileSpec.builder("mini", className)
        val container = TypeSpec.objectBuilder(className)
            .addKdoc("Automatically generated, do not edit.\n")

        //Get non-abstract actions
        try {
            ActionTypesGenerator.generate(container, roundActions)
            ReducersGenerator.generate(container, roundReducers)
        } catch (ex: Throwable) {
            if (ex !is CompilerException) {
                //Compiler crashed
                logError("Mini compiler crashed! Report issue to mini: ${ex.message}")
                ex.printStackTrace()
                throw ex
            }
        }

        file.addType(container.build())
        file.build().writeToFile(sourceElements = *((roundActions + roundReducers).toTypedArray()))

        return true
    }
}
