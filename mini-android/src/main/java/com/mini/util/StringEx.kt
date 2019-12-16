package com.mini.util

import android.annotation.SuppressLint

private fun Char.isDigitOrUpperCase(): Boolean = isDigit() || isUpperCase()
private fun StringBuilder.toStringAndClear() = toString().also { clear() }

private val BOUNDARIES = listOf(' ', '-', '_', '.')

/**
 * Splits a string to multiple words using [boundaries] to separate them.
 */
fun String.splitToWords(boundaries: Iterable<Char> = BOUNDARIES): List<String> {
    val list = mutableListOf<String>()
    val word = StringBuilder()
    for (index in 0 until length) {
        val char = this[index]
        if (char in boundaries) {
            list.add(word.toStringAndClear())
        } else {
            if (char.isDigitOrUpperCase()) {
                val hasPrev = index > 0
                val hasNext = index < length - 1
                val prevLowerCase = hasPrev && this[index - 1].isLowerCase()
                val prevDigitUpperCase = hasPrev && this[index - 1].isDigitOrUpperCase()
                val nextLowerCase = hasNext && this[index + 1].isLowerCase()
                if (prevLowerCase || (prevDigitUpperCase && nextLowerCase)) {
                    list.add(word.toStringAndClear())
                }
            }
            word.append(char)
        }
    }
    list.add(word.toStringAndClear())
    return list
}

@SuppressLint("DefaultLocale")
fun String.toSnakeCase(): String {
    return splitToWords().joinToString(separator = "_") { it.decapitalize() }
}

@SuppressLint("DefaultLocale")
fun String.toCamelCase(): String {
    return splitToWords().joinToString(separator = "") { it.capitalize() }
}