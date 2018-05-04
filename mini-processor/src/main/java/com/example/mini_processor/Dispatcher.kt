package com.example.mini_processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment

class Dispatcher(actionElements: List<ReducerFunc>, val processingEnv: ProcessingEnvironment) {
    val storeMap = mutableMapOf<String, Store>()

    init {
        actionElements
                .filter { it.parentClass.isClass && true } //Check if superclass is store type
                .forEach {
                    val store = Store(it.parentClass)
                    if (storeMap.contains(store.className)) {
                        storeMap[store.className]!!.subscribedMethods.add(it)
                    } else {
                        storeMap[store.className] = store
                    }
                }
    }

    fun generateDispatcherFile() {
        val dispatcherClass = ClassName("", "Dispatcher")
        val file = FileSpec.builder("", "Dispatcher")
                .addType(
                        TypeSpec.classBuilder("Greeter").add
    }

}