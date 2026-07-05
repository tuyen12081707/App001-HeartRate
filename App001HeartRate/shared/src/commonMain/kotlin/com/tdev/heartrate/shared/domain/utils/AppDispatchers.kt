package com.tdev.heartrate.shared.domain.utils

import kotlinx.coroutines.CoroutineDispatcher

interface AppDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

expect fun provideAppDispatchers(): AppDispatchers
