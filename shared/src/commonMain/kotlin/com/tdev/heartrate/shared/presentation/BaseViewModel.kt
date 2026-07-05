package com.tdev.heartrate.shared.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S, I, E>(initialState: S) : ViewModel() {
    protected val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    protected val _sideEffect = MutableSharedFlow<E>()
    val sideEffect: SharedFlow<E> = _sideEffect.asSharedFlow()

    abstract fun onIntent(intent: I)

    protected fun emitSideEffect(effect: E) {
        viewModelScope.launch {
            _sideEffect.emit(effect)
        }
    }
}
