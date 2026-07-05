package com.tdev.heartrate.shared.domain.model

data class HeartRateStats(
    val averageBpm: Int = 0,
    val maxBpm: Int = 0,
    val minBpm: Int = 0,
    val totalRecords: Int = 0
)
