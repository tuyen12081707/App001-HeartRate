package com.tdev.heartrate.shared.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.tdev.heartrate.shared.data.database.HeartRateDatabase
import com.tdev.heartrate.shared.domain.sensor.CameraHeartRateSensor
import com.tdev.heartrate.shared.domain.sensor.CameraHeartRateSensorImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CameraHeartRateSensor> { CameraHeartRateSensorImpl() }
    single<SqlDriver> { NativeSqliteDriver(HeartRateDatabase.Schema, "heartrate.db") }
}
