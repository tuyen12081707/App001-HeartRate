package com.tdev.heartrate.shared.data.mapper

import com.tdev.heartrate.shared.data.database.HeartRateEntity
import com.tdev.heartrate.shared.domain.model.HeartRateRecord

fun HeartRateEntity.toDomainModel(): HeartRateRecord {
    return HeartRateRecord(
        id = this.id,
        bpm = this.bpm.toInt(),
        timestamp = this.timestamp,
        measureType = this.measureType,
        bodyState = this.bodyState,
        note = this.note
    )
}

fun HeartRateRecord.toEntityModel(): HeartRateEntity {
    return HeartRateEntity(
        id = this.id,
        bpm = this.bpm.toLong(),
        timestamp = this.timestamp,
        measureType = this.measureType,
        bodyState = this.bodyState,
        note = this.note
    )
}
