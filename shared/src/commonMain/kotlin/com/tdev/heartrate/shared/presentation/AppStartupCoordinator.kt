package com.tdev.heartrate.shared.presentation

import com.tdev.heartrate.shared.domain.model.AppConfig
import com.tdev.heartrate.shared.domain.model.StartupData
import com.tdev.heartrate.shared.domain.usecase.GetDisclaimerStatusUseCase
import com.tdev.heartrate.shared.domain.usecase.SeedDemoHeartRateUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AppStartupCoordinator(
    private val appConfig: AppConfig,
    private val getDisclaimerStatus: GetDisclaimerStatusUseCase,
    private val seedDemoHeartRate: SeedDemoHeartRateUseCase
) {
    fun start(): Flow<DataState<StartupData>> = flow {
        emit(DataState.Loading)
        try {
            val consentAccepted = getDisclaimerStatus()
            if (appConfig.demoDataEnabled) {
                seedDemoHeartRate()
            }
            emit(DataState.Success(StartupData(consentAccepted = consentAccepted)))
        } catch (throwable: Exception) {
            emit(
                DataState.Error(
                    message = throwable.message ?: throwable.toString(),
                    throwable = throwable
                )
            )
        }
    }
}
