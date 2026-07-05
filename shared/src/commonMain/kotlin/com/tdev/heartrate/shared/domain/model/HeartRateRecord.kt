package com.tdev.heartrate.shared.domain.model

data class HeartRateRecord(
    val id: Long = 0,
    val bpm: Int,
    val timestamp: Long,
    val measureType: MeasureType,
    val bodyState: BodyState,
    val note: String? = null
)
