package com.tdev.heartrate.shared.presentation.navigation

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AppNavigatorTest {
    @Test
    fun navigateAndBackRestoreTypedRoutes() = runBlocking {
        val navigator = AppNavigator(AppRoute.Main(MainTab.Dashboard))
        navigator.navigate(AppRoute.AddHeartRate)
        assertEquals(AppRoute.AddHeartRate, navigator.route.value)
        navigator.navigate(AppRoute.Result(recordId = 42L))
        assertEquals(AppRoute.Result(42L), navigator.route.value)
        navigator.back()
        assertEquals(AppRoute.AddHeartRate, navigator.route.value)
    }

    @Test
    fun resultRoutesHaveDistinctViewModelKeys() {
        assertEquals(false, AppRoute.Result(1L).resultViewModelKey() == AppRoute.Result(2L).resultViewModelKey())
    }
}
