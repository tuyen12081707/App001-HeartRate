package com.tdev.heartrate.shared.presentation.navigation

import com.tdev.heartrate.shared.domain.model.MeasureType

sealed interface AppRoute {
    data object Disclaimer : AppRoute
    data class Main(val tab: MainTab) : AppRoute
    data class AddHeartRate(
        val prefilledBpm: Int? = null,
        val measureType: MeasureType = MeasureType.MANUAL
    ) : AppRoute
    data class Result(val recordId: Long) : AppRoute
    data class NewsDetail(val url: String) : AppRoute
    data object BloodPressure : AppRoute
    data object CameraMeasurement : AppRoute
    data object CameraPermissionDenied : AppRoute
    data object FailedScan : AppRoute
}
