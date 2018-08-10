package mini.processor

import mini.ActionType
import mini.Reducer
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(MiniProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class MiniProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun init(environment: ProcessingEnvironment) {
        env = environment
        typeUtils = env.typeUtils
        elementUtils = env.elementUtils
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Reducer::class.java)
                .map { it.canonicalName }
                .toMutableSet()
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotatedFunctions = roundEnv.getElementsAnnotatedWith(Reducer::class.java)
        val annotatedClasses = roundEnv.getElementsAnnotatedWith(ActionType::class.java)
        if (annotatedFunctions.isEmpty()) return false

        val reducerFunctions = annotatedFunctions
                .filterNotNull()
                .filter { it.isMethod }
                .map { ReducerFuncModel(it as ExecutableElement) }

        val actionTypes = annotatedClasses.map { it.asType() }

        val reducerModel = ActionReducerModel(reducerFunctions, actionTypes)
        reducerModel.generateDispatcherFile()

        return true
    }
}