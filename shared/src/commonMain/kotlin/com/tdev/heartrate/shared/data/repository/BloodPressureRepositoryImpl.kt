package com.tdev.heartrate.shared.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tdev.heartrate.shared.data.database.HeartRateDatabase
import com.tdev.heartrate.shared.data.mapper.toDomainModel
import com.tdev.heartrate.shared.domain.model.BloodPressureRecord
import com.tdev.heartrate.shared.domain.repository.BloodPressureRepository
import com.tdev.heartrate.shared.domain.utils.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BloodPressureRepositoryImpl(
    private val database: HeartRateDatabase,
    private val dispatchers: AppDispatchers
) : BloodPressureRepository {

    private val queries = database.heartRateDatabaseQueries

    override suspend fun insertRecord(record: BloodPressureRecord) {
        queries.insertBloodPressureRecord(
            systolic = record.systolic.toLong(),
            diastolic = record.diastolic.toLong(),
            pulse = record.pulse.toLong(),
            timestamp = record.timestamp,
            note = record.note
        )
    }

    override fun getAllRecords(): Flow<List<BloodPressureRecord>> =
        queries.getAllBloodPressureRecords()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { records -> records.map { it.toDomainModel() } }
}
