package com.minivac.mini.misc

import android.annotation.SuppressLint

/**
 * Transform bytes into a human readable string without localization.

 * @param bytes   The number of bytes.
 * *
 * @param shorter Include decimal values (1.3MB or 1.34MB)
 */
@SuppressLint("DefaultLocale")
fun humanFileSize(bytes: Long, shorter: Boolean): String {
    if (bytes < 0) return "??B"

    var result = bytes.toFloat()
    var suffix = "B"
    if (result > 900) {
        suffix = "K"
        result = result / 1024
    }
    if (result > 900) {
        suffix = "M"
        result = result / 1024
    }
    if (result > 900) {
        suffix = "G"
        result = result / 1024
    }
    val value: String
    if (result < 1) {
        value = String.format("%.2f", result)
    } else if (result < 10) {
        if (shorter) {
            value = String.format("%.1f", result)
        } else {
            value = String.format("%.2f", result)
        }
    } else if (result < 100) {
        if (shorter) {
            value = String.format("%.0f", result)
        } else {
            value = String.format("%.2f", result)
        }
    } else {
        value = String.format("%.0f", result)
    }
    return value + suffix
}