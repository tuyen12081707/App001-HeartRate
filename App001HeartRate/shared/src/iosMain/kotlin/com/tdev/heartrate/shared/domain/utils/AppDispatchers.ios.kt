package com.tdev.heartrate.shared.domain.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class IosAppDispatchers : AppDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Main
    // Dispatchers.IO is available in newer kotlinx.coroutines for native, but we map it explicitly
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}

actual fun provideAppDispatchers(): AppDispatchers = IosAppDispatchers()
