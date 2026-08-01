package com.tdev.heartrate.shared.domain.utils

fun interface Clock {
    fun nowMillis(): Long
}

object SystemClock : Clock {
    override fun nowMillis(): Long = getCurrentTimeMillis()
}
