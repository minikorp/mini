package mini.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import mini.Action
import mini.Reducer
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated")
class MiniProcessor : AbstractProcessor() {

    override fun init(environment: ProcessingEnvironment) {
        env = environment
        typeUtils = env.typeUtils
        elementUtils = env.elementUtils
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Reducer::class.java, Action::class.java)
            .map { it.canonicalName }
            .toMutableSet()
    }

    override fun process(set: MutableSet<out TypeElement>,
                         roundEnv: RoundEnvironment): Boolean {

        val roundActions = roundEnv.getElementsAnnotatedWith(Action::class.java)
        val roundReducers = roundEnv.getElementsAnnotatedWith(Reducer::class.java)

        if (roundActions.isEmpty()) return false

        val className = "MiniGen"
        val file = FileSpec.builder("mini", className)
        val container = TypeSpec.objectBuilder(className)
        container.addKdoc("Automatically generated, do not edit.\n")

        //Get non-abstract actions
        ActionTypesGenerator.generate(container, roundActions)
        ReducersGenerator.generate(container, roundReducers)

        file.addType(container.build())
        file.build().writeToFile()

        return true
    }


}
