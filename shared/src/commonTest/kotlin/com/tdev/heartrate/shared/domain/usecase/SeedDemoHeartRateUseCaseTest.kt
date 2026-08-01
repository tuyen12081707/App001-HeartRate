package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.AppMetadataRepository
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.utils.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeedDemoHeartRateUseCaseTest {

    @Test
    fun missingMarkerInsertsSevenFixedManualRecordsThenStoresMarker() = runBlocking {
        val heartRateRepository = SeedHeartRateRepository()
        val metadataRepository = SeedMetadataRepository()
        val useCase = SeedDemoHeartRateUseCase(
            heartRateRepository = heartRateRepository,
            metadataRepository = metadataRepository,
            clock = Clock { NOW_MILLIS }
        )

        val inserted = useCase()

        assertTrue(inserted)
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
            heartRateRepository.insertedRecords.map { it.bpm to it.timestamp }
        )
        assertTrue(heartRateRepository.insertedRecords.all { it.measureType == MeasureType.MANUAL })
        assertTrue(heartRateRepository.insertedRecords.all { it.bodyState == BodyState.RESTING })
        assertEquals("true", metadataRepository.get("demo_seed_v1"))
    }

    @Test
    fun existingMarkerReturnsFalseWithoutInsertingRecords() = runBlocking {
        val heartRateRepository = SeedHeartRateRepository()
        val metadataRepository = SeedMetadataRepository(
            initialValues = mutableMapOf("demo_seed_v1" to "true")
        )
        val useCase = SeedDemoHeartRateUseCase(
            heartRateRepository = heartRateRepository,
            metadataRepository = metadataRepository,
            clock = Clock { NOW_MILLIS }
        )

        val inserted = useCase()

        assertEquals(false, inserted)
        assertEquals(emptyList(), heartRateRepository.insertedRecords)
    }

    @Test
    fun failedInsertDoesNotStoreMarker() = runBlocking {
        val heartRateRepository = SeedHeartRateRepository(failAtInsert = 4)
        val metadataRepository = SeedMetadataRepository()
        val useCase = SeedDemoHeartRateUseCase(
            heartRateRepository = heartRateRepository,
            metadataRepository = metadataRepository,
            clock = Clock { NOW_MILLIS }
        )

        assertFailsWith<IllegalStateException> { useCase() }

        assertNull(metadataRepository.get("demo_seed_v1"))
        assertEquals(3, heartRateRepository.insertedRecords.size)
    }

    private companion object {
        const val DAY_MILLIS = 86_400_000L
        const val NOW_MILLIS = 20 * DAY_MILLIS + 8 * 60 * 60 * 1_000L
    }
}

private class SeedHeartRateRepository(
    private val failAtInsert: Int? = null
) : HeartRateRepository {
    val insertedRecords = mutableListOf<HeartRateRecord>()
    private var attempts = 0

    override suspend fun insertRecord(record: HeartRateRecord): Long {
        attempts += 1
        if (attempts == failAtInsert) throw IllegalStateException("Seed insert failed")
        insertedRecords += record
        return attempts.toLong()
    }

    override suspend fun getRecordById(id: Long): HeartRateRecord? = null

    override suspend fun deleteRecord(id: Long) = Unit

    override fun getAllRecords(): Flow<List<HeartRateRecord>> = flowOf(insertedRecords)

    override suspend fun getAverageBpm(): Double = 0.0
}

private class SeedMetadataRepository(
    private val initialValues: MutableMap<String, String> = mutableMapOf()
) : AppMetadataRepository {
    override suspend fun get(key: String): String? = initialValues[key]

    override suspend fun put(key: String, value: String) {
        initialValues[key] = value
    }
}
