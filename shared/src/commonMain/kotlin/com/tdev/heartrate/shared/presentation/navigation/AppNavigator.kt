package com.tdev.heartrate.shared.presentation.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppNavigator(initialRoute: AppRoute) {
    private val backStack = mutableListOf(initialRoute)
    private val _route = MutableStateFlow(initialRoute)
    val route: StateFlow<AppRoute> = _route.asStateFlow()

    fun navigate(route: AppRoute) {
        backStack += route
        _route.value = route
    }

    fun back() {
        if (backStack.size <= 1) return
        backStack.removeAt(backStack.lastIndex)
        _route.value = backStack.last()
    }
}

fun AppRoute.Result.resultViewModelKey(): String = "result-$recordId"
