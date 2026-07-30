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

object BloodPressureThresholds {
    const val HYPOTENSION_SYSTOLIC_MAX = 89
    const val HYPOTENSION_DIASTOLIC_MAX = 59
    const val NORMAL_SYSTOLIC_MIN = 90
    const val NORMAL_SYSTOLIC_MAX = 129
    const val NORMAL_DIASTOLIC_MIN = 60
    const val NORMAL_DIASTOLIC_MAX = 79
    const val STAGE_1_SYSTOLIC_MIN = 130
    const val STAGE_1_SYSTOLIC_MAX = 139
    const val STAGE_1_DIASTOLIC_MIN = 80
    const val STAGE_1_DIASTOLIC_MAX = 89
    const val STAGE_2_SYSTOLIC_MIN = 140
    const val STAGE_2_SYSTOLIC_MAX = 180
    const val STAGE_2_DIASTOLIC_MIN = 90
    const val STAGE_2_DIASTOLIC_MAX = 120
    const val CRISIS_SYSTOLIC_MIN = 181
    const val CRISIS_DIASTOLIC_MIN = 121
}

object BloodPressureInputConstraints {
    val SYSTOLIC_RANGE = 40..250
    val DIASTOLIC_RANGE = 20..150
    val PULSE_RANGE = 30..250
}

fun classifyBloodPressure(systolic: Int, diastolic: Int): BloodPressureLevel =
    when {
        systolic >= BloodPressureThresholds.CRISIS_SYSTOLIC_MIN ||
            diastolic >= BloodPressureThresholds.CRISIS_DIASTOLIC_MIN ->
            BloodPressureLevel.HYPERTENSIVE_CRISIS

        systolic >= BloodPressureThresholds.STAGE_2_SYSTOLIC_MIN ||
            diastolic >= BloodPressureThresholds.STAGE_2_DIASTOLIC_MIN ->
            BloodPressureLevel.HYPERTENSION_STAGE_2

        systolic >= BloodPressureThresholds.STAGE_1_SYSTOLIC_MIN ||
            diastolic >= BloodPressureThresholds.STAGE_1_DIASTOLIC_MIN ->
            BloodPressureLevel.HYPERTENSION_STAGE_1

        systolic <= BloodPressureThresholds.HYPOTENSION_SYSTOLIC_MAX ||
            diastolic <= BloodPressureThresholds.HYPOTENSION_DIASTOLIC_MAX ->
            BloodPressureLevel.HYPOTENSION

        else -> BloodPressureLevel.NORMAL
    }

fun BloodPressureRecord.level(): BloodPressureLevel =
    classifyBloodPressure(systolic = systolic, diastolic = diastolic)
