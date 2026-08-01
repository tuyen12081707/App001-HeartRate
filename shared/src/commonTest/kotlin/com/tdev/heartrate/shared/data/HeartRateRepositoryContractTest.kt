package com.tdev.heartrate.shared.data

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.AppMetadataRepository
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HeartRateRepositoryContractTest {

    @Test
    fun insertRecordReturnsIdThatCanBeUsedForLookup() = runBlocking {
        val repository = FakeHeartRateRepository()
        val record = heartRateRecord()

        val insertedId = repository.insertRecord(record)

        assertEquals(1L, insertedId)
        assertEquals(record.copy(id = insertedId), repository.getRecordById(insertedId))
    }

    @Test
    fun getRecordByIdReturnsNullForMissingRecord() = runBlocking {
        val repository = FakeHeartRateRepository()

        assertNull(repository.getRecordById(404L))
    }

    @Test
    fun metadataPutOverwritesValueForSameKey() = runBlocking {
        val repository = FakeAppMetadataRepository()

        repository.put("disclaimer_accepted", "false")
        repository.put("disclaimer_accepted", "true")

        assertEquals("true", repository.get("disclaimer_accepted"))
    }

    @Test
    fun metadataGetReturnsNullForMissingKey() = runBlocking {
        assertNull(FakeAppMetadataRepository().get("missing"))
    }

    private fun heartRateRecord() = HeartRateRecord(
        bpm = 72,
        timestamp = 1_722_468_000_000L,
        measureType = MeasureType.MANUAL,
        bodyState = BodyState.RESTING,
        note = "Morning"
    )
}

private class FakeHeartRateRepository : HeartRateRepository {
    private val records = mutableMapOf<Long, HeartRateRecord>()
    private var nextId = 1L

    override suspend fun insertRecord(record: HeartRateRecord): Long {
        val id = nextId++
        records[id] = record.copy(id = id)
        return id
    }

    override suspend fun getRecordById(id: Long): HeartRateRecord? = records[id]

    override suspend fun deleteRecord(id: Long) {
        records.remove(id)
    }

    override fun getAllRecords(): Flow<List<HeartRateRecord>> = flowOf(records.values.toList())

    override suspend fun getAverageBpm(): Double =
        records.values.map { it.bpm }.average().takeUnless { it.isNaN() } ?: 0.0
}

private class FakeAppMetadataRepository : AppMetadataRepository {
    private val values = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? = values[key]

    override suspend fun put(key: String, value: String) {
        values[key] = value
    }
}
