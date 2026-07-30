package com.tdev.heartrate.shared.domain.model

data class BloodPressureRecord(
    val id: Long = 0,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val timestamp: Long,
    val note: String? = null
)

enum class BloodPressureLevel {
    HYPOTENSION,
    NORMAL,
    HYPERTENSION_STAGE_1,
    HYPERTENSION_STAGE_2,
    HYPERTENSIVE_CRISIS
}

fun BloodPressureRecord.level(): BloodPressureLevel =
    when {
        systolic < 90 || diastolic < 60 -> BloodPressureLevel.HYPOTENSION
        systolic > 180 || diastolic > 120 -> BloodPressureLevel.HYPERTENSIVE_CRISIS
        systolic >= 140 || diastolic >= 90 -> BloodPressureLevel.HYPERTENSION_STAGE_2
        systolic >= 130 || diastolic >= 80 -> BloodPressureLevel.HYPERTENSION_STAGE_1
        else -> BloodPressureLevel.NORMAL
    }
