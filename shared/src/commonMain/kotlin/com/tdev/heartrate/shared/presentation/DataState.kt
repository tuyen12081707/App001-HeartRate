package com.tdev.heartrate.shared.presentation

sealed interface DataState<out T> {
    data object Idle : DataState<Nothing>
    data object Loading : DataState<Nothing>
    data class Success<out T>(val data: T) : DataState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : DataState<Nothing>
}
