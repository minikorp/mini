package mini

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