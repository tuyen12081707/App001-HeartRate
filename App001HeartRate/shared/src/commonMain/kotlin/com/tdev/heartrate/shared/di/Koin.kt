package com.tdev.heartrate.shared.di

import app.cash.sqldelight.EnumColumnAdapter
import com.tdev.heartrate.shared.data.database.HeartRateDatabase
import com.tdev.heartrate.shared.data.database.HeartRateEntity
import com.tdev.heartrate.shared.data.repository.HeartRateRepositoryImpl
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.usecase.AddHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.DeleteHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateHistoryUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateStatsUseCase
import com.tdev.heartrate.shared.domain.utils.provideAppDispatchers
import com.tdev.heartrate.shared.presentation.add.AddRecordViewModel
import com.tdev.heartrate.shared.presentation.dashboard.DashboardViewModel
import com.tdev.heartrate.shared.presentation.history.HistoryViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val domainModule = module {
    factory { AddHeartRateRecordUseCase(get()) }
    factory { GetHeartRateHistoryUseCase(get()) }
    factory { DeleteHeartRateRecordUseCase(get()) }
    factory { GetHeartRateStatsUseCase(get()) }
    single { provideAppDispatchers() }
}

val dataModule = module {
    single { 
        HeartRateDatabase(
            driver = get(),
            HeartRateEntityAdapter = HeartRateEntity.Adapter(
                measureTypeAdapter = EnumColumnAdapter(),
                bodyStateAdapter = EnumColumnAdapter()
            )
        ) 
    }
    single<HeartRateRepository> { HeartRateRepositoryImpl(get(), get()) }
}

val presentationModule = module {
    factory { HistoryViewModel(get(), get()) }
    factory { AddRecordViewModel(get()) }
    factory { DashboardViewModel(get()) }
}

expect val platformModule: Module


