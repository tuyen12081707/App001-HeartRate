package com.tdev.heartrate.shared.domain.repository

import com.tdev.heartrate.shared.domain.model.HeartRateRecord

interface DemoSeedRepository {
    suspend fun seedIfAbsent(
        markerKey: String,
        markerValue: String,
        records: List<HeartRateRecord>
    ): Boolean
}
