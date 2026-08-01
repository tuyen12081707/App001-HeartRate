package com.tdev.heartrate.shared.di

import app.cash.sqldelight.EnumColumnAdapter
import com.tdev.heartrate.shared.data.database.HeartRateDatabase
import com.tdev.heartrate.shared.data.database.HeartRateEntity
import com.tdev.heartrate.shared.data.repository.HeartRateRepositoryImpl
import com.tdev.heartrate.shared.data.repository.AppMetadataRepositoryImpl
import com.tdev.heartrate.shared.data.repository.BloodPressureRepositoryImpl
import com.tdev.heartrate.shared.domain.repository.AppMetadataRepository
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.repository.BloodPressureRepository
import com.tdev.heartrate.shared.domain.usecase.AddBloodPressureRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.AddHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.DeleteHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateHistoryUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateStatsUseCase
import com.tdev.heartrate.shared.domain.usecase.GetDashboardDataUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.SeedDemoHeartRateUseCase
import com.tdev.heartrate.shared.domain.utils.Clock
import com.tdev.heartrate.shared.domain.utils.SystemClock
import com.tdev.heartrate.shared.domain.utils.provideAppDispatchers
import com.tdev.heartrate.shared.presentation.add.AddRecordViewModel
import com.tdev.heartrate.shared.presentation.bloodpressure.BloodPressureViewModel
import com.tdev.heartrate.shared.presentation.dashboard.DashboardViewModel
import com.tdev.heartrate.shared.presentation.history.HistoryViewModel
import com.tdev.heartrate.shared.presentation.home.HomeViewModel
import com.tdev.heartrate.shared.domain.usecase.GetNewsUseCase
import com.tdev.heartrate.shared.domain.repository.NewsRepository
import com.tdev.heartrate.shared.data.repository.NewsRepositoryImpl
import com.tdev.heartrate.shared.data.remote.NewsApiClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }
    single { NewsApiClient(get()) }
}

val domainModule = module {
    factory { AddBloodPressureRecordUseCase(get()) }
    factory { AddHeartRateRecordUseCase(get(), get()) }
    factory { GetHeartRateHistoryUseCase(get()) }
    factory { GetHeartRateRecordUseCase(get()) }
    factory { DeleteHeartRateRecordUseCase(get()) }
    factory { GetHeartRateStatsUseCase(get()) }
    factory { GetDashboardDataUseCase(get(), get()) }
    factory { SeedDemoHeartRateUseCase(get(), get(), get()) }
    factory { GetNewsUseCase(get()) }
    factory { com.tdev.heartrate.shared.domain.usecase.GetNewsDetailUseCase(get()) }
    single<Clock> { SystemClock }
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
    single<AppMetadataRepository> { AppMetadataRepositoryImpl(get()) }
    single<BloodPressureRepository> { BloodPressureRepositoryImpl(get(), get()) }
    single<NewsRepository> { NewsRepositoryImpl(get()) }
}

val presentationModule = module {
    factory { BloodPressureViewModel(get()) }
    factory { HistoryViewModel(get(), get()) }
    factory { AddRecordViewModel(get()) }
    factory { DashboardViewModel(get()) }
    factory { HomeViewModel(get(), get()) }
    factory { com.tdev.heartrate.shared.presentation.newsdetail.NewsDetailViewModel(get()) }
}

expect val platformModule: Module
