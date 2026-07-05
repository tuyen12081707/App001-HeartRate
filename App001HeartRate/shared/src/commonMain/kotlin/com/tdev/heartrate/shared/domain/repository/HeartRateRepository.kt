package com.tdev.heartrate.shared.domain.repository

import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import kotlinx.coroutines.flow.Flow

interface HeartRateRepository {
    suspend fun insertRecord(record: HeartRateRecord)
    suspend fun deleteRecord(id: Long)
    fun getAllRecords(): Flow<List<HeartRateRecord>>
    suspend fun getAverageBpm(): Double
}
