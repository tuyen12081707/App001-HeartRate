package com.tdev.heartrate.shared.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tdev.heartrate.shared.data.database.HeartRateDatabase
import com.tdev.heartrate.shared.data.mapper.toDomainModel
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.utils.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HeartRateRepositoryImpl(
    private val database: HeartRateDatabase,
    private val dispatchers: AppDispatchers
) : HeartRateRepository {

    private val queries = database.heartRateDatabaseQueries

    override suspend fun insertRecord(record: HeartRateRecord) {
        queries.insertRecord(
            bpm = record.bpm.toLong(),
            timestamp = record.timestamp,
            measureType = record.measureType,
            bodyState = record.bodyState,
            note = record.note
        )
    }

    override suspend fun deleteRecord(id: Long) {
        queries.deleteRecord(id)
    }

    override fun getAllRecords(): Flow<List<HeartRateRecord>> {
        return queries.getAllRecords()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getAverageBpm(): Double {
        return queries.getAverageBpm().executeAsOne().AVG ?: 0.0
    }
}
