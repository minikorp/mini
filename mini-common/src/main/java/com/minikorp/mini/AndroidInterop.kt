package com.minikorp.mini

/**
 * Check if running on android device / emulator or jvm
 */
internal val isAndroid by lazy {
    try {
        android.os.Build.VERSION.SDK_INT != 0
    } catch (e: Throwable) {
        false
    }
}

fun requireAndroid() {
    if (!isAndroid) {
        throw UnsupportedOperationException("This method can only be called from android environment")
    }
}