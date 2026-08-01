package com.tdev.heartrate.shared.presentation.navigation

sealed interface AppRoute {
    data object Disclaimer : AppRoute
    data class Main(val tab: MainTab) : AppRoute
    data object AddHeartRate : AppRoute
    data class Result(val recordId: Long) : AppRoute
    data class NewsDetail(val url: String) : AppRoute
    data object BloodPressure : AppRoute
    data object CameraMeasurement : AppRoute
    data object FailedScan : AppRoute
}
