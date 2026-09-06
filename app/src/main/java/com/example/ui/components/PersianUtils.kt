package com.example.ui.components

import java.util.Locale

/**
 * Utility functions for Persian numbers, formatting, and Quranic styling.
 */
fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val sb = StringBuilder()
    for (ch in this) {
        if (ch in '0'..'9') {
            sb.append(persianDigits[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun Int.toPersianDigits(): String {
    return this.toString().toPersianDigits()
}

fun Long.toPersianDigits(): String {
    return this.toString().toPersianDigits()
}

/**
 * Formats milliseconds to MM:SS with Persian numerals.
 */
fun formatDurationToPersian(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val formatted = String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    return formatted.toPersianDigits()
}

/**
 * Formats file size in bytes to Persian readable string (e.g. ۱.۲ مگابایت)
 */
fun formatFileSizeToPersian(bytes: Long): String {
    if (bytes < 1024) return "${bytes.toPersianDigits()} بایت"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${String.format(Locale.ROOT, "%.1f", kb).toPersianDigits()} کیلوبایت"
    val mb = kb / 1024.0
    return "${String.format(Locale.ROOT, "%.1f", mb).toPersianDigits()} مگابایت"
}
