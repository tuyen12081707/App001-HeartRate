package com.tdev.heartrate.shared.domain.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}

actual fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
