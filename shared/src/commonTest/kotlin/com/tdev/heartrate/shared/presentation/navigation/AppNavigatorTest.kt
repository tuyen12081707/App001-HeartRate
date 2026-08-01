package com.tdev.heartrate.shared.presentation.navigation

import com.tdev.heartrate.shared.domain.model.MeasureType
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigatorTest {
    @Test
    fun cameraAddRoutePreservesPrefilledBpmAndSource() {
        val route = AppRoute.AddHeartRate(82, MeasureType.CAMERA_SENSOR)
        assertEquals(82, route.prefilledBpm)
        assertEquals(MeasureType.CAMERA_SENSOR, route.measureType)
    }

    @Test
    fun navigateAndBackRestoreTypedRoutes() = runBlocking {
        val navigator = AppNavigator(AppRoute.Main(MainTab.Dashboard))
        navigator.navigate(AppRoute.AddHeartRate())
        assertEquals(AppRoute.AddHeartRate(), navigator.route.value)
        navigator.navigate(AppRoute.Result(recordId = 42L))
        assertEquals(AppRoute.Result(42L), navigator.route.value)
        navigator.back()
        assertEquals(AppRoute.AddHeartRate(), navigator.route.value)
    }

    @Test
    fun resultRoutesHaveDistinctViewModelKeys() {
        assertEquals(false, AppRoute.Result(1L).resultViewModelKey() == AppRoute.Result(2L).resultViewModelKey())
    }
}
