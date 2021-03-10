package com.minikorp.mini.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*

private lateinit var _generatorOptions: Map<String, String>
private lateinit var _codeGenerator: CodeGenerator
private lateinit var _logger: KSPLogger
private lateinit var _resolver: Resolver

val generatorOptions get() = _generatorOptions
val codeGenerator get() = _codeGenerator
val logger get() = _logger
val resolver: Resolver get() = _resolver


class Processor : SymbolProcessor {
    override fun init(
            options: Map<String, String>,
            kotlinVersion: KotlinVersion,
            codeGenerator: CodeGenerator,
            logger: KSPLogger
    ) {
        _generatorOptions = options
        _codeGenerator = codeGenerator
        _logger = logger
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        _resolver = resolver
        try {
            val out = processStateClasses() + processTypedReducerClasses()
            out.forEach { it.initialize() }
            out.forEach { it.emit() }
        } catch (compilationException: CompilationException) {
            logger.error(compilationException.message, compilationException.node)
        }

        flushDebugPrint()
        return emptyList()
    }

    override fun finish() {
    }
}