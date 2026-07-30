package com.tdev.heartrate.shared.data.mapper

import com.tdev.heartrate.shared.data.database.BloodPressureEntity
import com.tdev.heartrate.shared.domain.model.BloodPressureRecord

fun BloodPressureEntity.toDomainModel(): BloodPressureRecord =
    BloodPressureRecord(
        id = id,
        systolic = systolic.toInt(),
        diastolic = diastolic.toInt(),
        pulse = pulse.toInt(),
        timestamp = timestamp,
        note = note
    )
