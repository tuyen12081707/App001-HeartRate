package com.tdev.heartrate.shared.domain.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class AndroidAppDispatchers : AppDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}

actual fun provideAppDispatchers(): AppDispatchers = AndroidAppDispatchers()
