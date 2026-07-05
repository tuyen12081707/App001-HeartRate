package com.tdev.heartrate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform