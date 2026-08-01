package com.tdev.heartrate.shared.domain.model

data class DashboardPoint(
    val dayStartMillis: Long,
    val averageBpm: Int,
    val recordCount: Int
)
