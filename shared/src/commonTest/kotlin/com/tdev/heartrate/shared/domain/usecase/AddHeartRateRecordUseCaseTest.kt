package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.utils.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AddHeartRateRecordUseCaseTest {

    @Test
    fun omittedTimestampUsesInjectedClockAndReturnsPersistedId() = runBlocking {
        val repository = RecordingHeartRateRepository(insertedId = 41L)
        val useCase = AddHeartRateRecordUseCase(
            repository = repository,
            clock = Clock { 1_722_468_000_000L }
        )

        val id = useCase(
            bpm = 72,
            measureType = MeasureType.MANUAL,
            bodyState = BodyState.RESTING,
            note = "Morning"
        )

        assertEquals(41L, id)
        assertEquals(
            HeartRateRecord(
                bpm = 72,
                timestamp = 1_722_468_000_000L,
                measureType = MeasureType.MANUAL,
                bodyState = BodyState.RESTING,
                note = "Morning"
            ),
            repository.insertedRecord
        )
    }

    @Test
    fun explicitTimestampOverridesInjectedClock() = runBlocking {
        val repository = RecordingHeartRateRepository(insertedId = 7L)
        val useCase = AddHeartRateRecordUseCase(
            repository = repository,
            clock = Clock { 9_999L }
        )

        useCase(
            bpm = 84,
            measureType = MeasureType.CAMERA_SENSOR,
            bodyState = BodyState.EXERCISING,
            note = null,
            timestamp = 1_234L
        )

        assertEquals(1_234L, repository.insertedRecord?.timestamp)
    }
}

private class RecordingHeartRateRepository(
    private val insertedId: Long
) : HeartRateRepository {
    var insertedRecord: HeartRateRecord? = null

    override suspend fun insertRecord(record: HeartRateRecord): Long {
        insertedRecord = record
        return insertedId
    }

    override suspend fun getRecordById(id: Long): HeartRateRecord? = null

    override suspend fun deleteRecord(id: Long) = Unit

    override fun getAllRecords(): Flow<List<HeartRateRecord>> = flowOf(emptyList())

    override suspend fun getAverageBpm(): Double = 0.0
}
