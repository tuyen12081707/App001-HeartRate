package com.tdev.heartrate.shared.domain.model

data class DashboardData(
    val latest: HeartRateRecord?,
    val averageBpm: Int,
    val minBpm: Int,
    val maxBpm: Int,
    val totalRecords: Int,
    val points: List<DashboardPoint>
)
