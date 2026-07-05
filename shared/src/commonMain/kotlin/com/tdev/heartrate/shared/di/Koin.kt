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
    factory { AddHeartRateRecordUseCase(get()) }
    factory { GetHeartRateHistoryUseCase(get()) }
    factory { DeleteHeartRateRecordUseCase(get()) }
    factory { GetHeartRateStatsUseCase(get()) }
    factory { GetNewsUseCase(get()) }
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
    single<NewsRepository> { NewsRepositoryImpl(get()) }
}

val presentationModule = module {
    factory { HistoryViewModel(get(), get()) }
    factory { AddRecordViewModel(get()) }
    factory { DashboardViewModel(get()) }
    factory { HomeViewModel(get(), get()) }
}

expect val platformModule: Module


