package com.minikorp.mini.processor

import com.google.devtools.ksp.symbol.KSNode

class CompilationException(val node: KSNode, override val message: String, cause: Throwable? = null) : Throwable(message, cause)