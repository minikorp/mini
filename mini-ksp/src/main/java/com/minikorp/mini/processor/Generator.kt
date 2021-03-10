package com.minikorp.mini.processor

interface Generator {
    val id: String
    fun initialize()
    fun emit()
}