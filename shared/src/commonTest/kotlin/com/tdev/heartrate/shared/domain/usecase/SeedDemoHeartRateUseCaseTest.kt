package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.DemoSeedRepository
import com.tdev.heartrate.shared.domain.utils.Clock
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SeedDemoHeartRateUseCaseTest {

    @Test
    fun missingMarkerInsertsSevenFixedManualRecordsAndSecondCallIsIdempotent() = runBlocking {
        val repository = RecordingDemoSeedRepository()
        val useCase = SeedDemoHeartRateUseCase(
            demoSeedRepository = repository,
            clock = Clock { NOW_MILLIS }
        )

        assertTrue(useCase())
        assertEquals(7, repository.insertedRecords.size)
        assertEquals(false, useCase())
        assertEquals(7, repository.insertedRecords.size)
        assertEquals(
            listOf(
                68 to NOW_MILLIS - 6 * DAY_MILLIS,
                72 to NOW_MILLIS - 5 * DAY_MILLIS,
                70 to NOW_MILLIS - 4 * DAY_MILLIS,
                75 to NOW_MILLIS - 3 * DAY_MILLIS,
                73 to NOW_MILLIS - 2 * DAY_MILLIS,
                71 to NOW_MILLIS - DAY_MILLIS,
                74 to NOW_MILLIS
            ),
            repository.insertedRecords.map { it.bpm to it.timestamp }
        )
        assertTrue(repository.insertedRecords.all { it.measureType == MeasureType.MANUAL })
        assertTrue(repository.insertedRecords.all { it.bodyState == BodyState.RESTING })
    }

    @Test
    fun existingMarkerReturnsFalseWithoutInsertingRecords() = runBlocking {
        val repository = RecordingDemoSeedRepository(seeded = true)
        val useCase = SeedDemoHeartRateUseCase(
            demoSeedRepository = repository,
            clock = Clock { NOW_MILLIS }
        )

        assertEquals(false, useCase())
        assertEquals(emptyList(), repository.insertedRecords)
    }

    @Test
    fun failedAtomicSeedDoesNotExposePartialRecords() = runBlocking {
        val repository = RecordingDemoSeedRepository(fail = true)
        val useCase = SeedDemoHeartRateUseCase(
            demoSeedRepository = repository,
            clock = Clock { NOW_MILLIS }
        )

        assertFailsWith<IllegalStateException> { useCase() }

        assertEquals(emptyList(), repository.insertedRecords)
        assertEquals(false, repository.seeded)
    }

    private companion object {
        const val DAY_MILLIS = 86_400_000L
        const val NOW_MILLIS = 20 * DAY_MILLIS + 8 * 60 * 60 * 1_000L
    }
}

private class RecordingDemoSeedRepository(
    var seeded: Boolean = false,
    private val fail: Boolean = false
) : DemoSeedRepository {
    val insertedRecords = mutableListOf<HeartRateRecord>()

    override suspend fun seedIfAbsent(
        markerKey: String,
        markerValue: String,
        records: List<HeartRateRecord>
    ): Boolean {
        if (fail) throw IllegalStateException("Seed transaction failed")
        if (seeded) return false
        insertedRecords += records
        seeded = true
        return true
    }
}
