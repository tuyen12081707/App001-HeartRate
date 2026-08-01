package com.tdev.heartrate.shared.presentation.dashboard

import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.DashboardData
import com.tdev.heartrate.shared.domain.usecase.GetDashboardDataUseCase
import com.tdev.heartrate.shared.presentation.BaseViewModel
import com.tdev.heartrate.shared.presentation.DataState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class DashboardUiState(
    val data: DataState<DashboardData> = DataState.Idle
) {
    val isLoading: Boolean get() = data is DataState.Loading || data is DataState.Idle
    val dashboard: DashboardData? get() = (data as? DataState.Success)?.data
}

class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : BaseViewModel<DashboardUiState, Unit, Unit>(DashboardUiState()) {

    private var loadJob: Job? = null

    fun retry() {
        load()
    }

    init {
        load()
    }

    private fun load() {
        loadJob?.cancel()
        _uiState.value = DashboardUiState(DataState.Loading)
        loadJob = viewModelScope.launch {
            getDashboardDataUseCase()
                .map<DashboardData, DataState<DashboardData>> { DataState.Success(it) }
                .catch { throwable ->
                    if (throwable is CancellationException) throw throwable
                    emit(DataState.Error(throwable.message ?: "Unable to load dashboard", throwable))
                }
                .collect { state -> _uiState.value = DashboardUiState(state) }
        }
    }

    override fun onIntent(intent: Unit) = Unit
}
