package com.example.mini_processor


import com.example.mini_commons.Reducer
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("org.kotlin.annotationProcessor.reducer")
@SupportedOptions(DispatcherProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class DispatcherProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun init(env: ProcessingEnvironment) {
        ProcessorUtils.env = env
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Reducer::class.java)
                .map { it.canonicalName }
                .toMutableSet()
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(Reducer::class.java)
        if (annotatedElements.isEmpty()) return false

        val actionMethods =  annotatedElements
                .filter { it.isMethod }
                .map { ReducerFunc(it as ExecutableElement) }

        val dispatcherFile = Dispatcher(actionMethods, processingEnv)

        return true
    }
}