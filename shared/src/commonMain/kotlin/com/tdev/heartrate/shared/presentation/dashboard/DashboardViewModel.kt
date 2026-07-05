package com.tdev.heartrate.shared.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdev.heartrate.shared.domain.model.HeartRateStats
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateStatsUseCase
import com.tdev.heartrate.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class DashboardUiState(
    val stats: HeartRateStats = HeartRateStats(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    getHeartRateStatsUseCase: GetHeartRateStatsUseCase
) : BaseViewModel<DashboardUiState, Unit, Unit>(DashboardUiState()) {
    
    init {
        viewModelScope.launch {
            getHeartRateStatsUseCase()
                .map { stats ->
                    DashboardUiState(
                        stats = stats,
                        isLoading = false
                    )
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    override fun onIntent(intent: Unit) {
        // No intents for dashboard yet
    }
}
