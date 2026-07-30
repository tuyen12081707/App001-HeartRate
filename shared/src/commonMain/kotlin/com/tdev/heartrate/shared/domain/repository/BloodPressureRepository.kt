package com.tdev.heartrate.shared.domain.repository

import com.tdev.heartrate.shared.domain.model.BloodPressureRecord
import kotlinx.coroutines.flow.Flow

interface BloodPressureRepository {
    suspend fun insertRecord(record: BloodPressureRecord)
    fun getAllRecords(): Flow<List<BloodPressureRecord>>
}
