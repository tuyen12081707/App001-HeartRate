package com.tdev.heartrate.shared.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.tdev.heartrate.shared.data.database.HeartRateDatabase
import com.tdev.heartrate.shared.domain.sensor.CameraHeartRateSensor
import com.tdev.heartrate.shared.domain.sensor.CameraHeartRateSensorImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CameraHeartRateSensor> { CameraHeartRateSensorImpl(get()) }
    single<SqlDriver> { AndroidSqliteDriver(HeartRateDatabase.Schema, get(), "heartrate.db") }
}
