package com.tdev.heartrate.shared.data.repository

import com.tdev.heartrate.shared.data.database.HeartRateDatabase
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.repository.DemoSeedRepository

class DemoSeedRepositoryImpl(
    private val database: HeartRateDatabase
) : DemoSeedRepository {
    override suspend fun seedIfAbsent(
        markerKey: String,
        markerValue: String,
        records: List<HeartRateRecord>
    ): Boolean = database.transactionWithResult {
        val queries = database.heartRateDatabaseQueries
        if (queries.getMetadata(markerKey).executeAsOneOrNull() != null) {
            false
        } else {
            records.forEach { record ->
                queries.insertRecord(
                    bpm = record.bpm.toLong(),
                    timestamp = record.timestamp,
                    measureType = record.measureType,
                    bodyState = record.bodyState,
                    note = record.note
                )
            }
            queries.upsertMetadata(markerKey, markerValue)
            true
        }
    }
}
