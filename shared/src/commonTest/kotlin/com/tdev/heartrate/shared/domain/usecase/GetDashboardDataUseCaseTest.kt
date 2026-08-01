package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.DashboardData
import com.tdev.heartrate.shared.domain.model.DashboardPoint
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.utils.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class GetDashboardDataUseCaseTest {

    @Test
    fun emptyRepositoryProducesEmptyDashboard() = runBlocking {
        val useCase = GetDashboardDataUseCase(
            repository = DashboardHeartRateRepository(emptyList()),
            clock = Clock { NOW_MILLIS }
        )

        assertEquals(
            DashboardData(
                latest = null,
                averageBpm = 0,
                minBpm = 0,
                maxBpm = 0,
                totalRecords = 0,
                points = emptyList()
            ),
            useCase().first()
        )
    }

    @Test
    fun aggregatesOnlyLatestSevenDayBucketsAndOrdersPointsOldestFirst() = runBlocking {
        val currentDayStart = 10 * DAY_MILLIS
        val earliestDayStart = currentDayStart - 6 * DAY_MILLIS
        val records = listOf(
            record(bpm = 80, timestamp = NOW_MILLIS - 1_000L, id = 4L),
            record(bpm = 90, timestamp = currentDayStart - DAY_MILLIS + 2_000L, id = 3L),
            record(bpm = 60, timestamp = currentDayStart - DAY_MILLIS + 1_000L, id = 2L),
            record(bpm = 70, timestamp = earliestDayStart, id = 1L),
            record(bpm = 200, timestamp = earliestDayStart - 1L, id = 5L),
            record(bpm = 10, timestamp = currentDayStart + DAY_MILLIS, id = 6L)
        )
        val useCase = GetDashboardDataUseCase(
            repository = DashboardHeartRateRepository(records),
            clock = Clock { NOW_MILLIS }
        )

        val result = useCase().first()

        assertEquals(record(bpm = 80, timestamp = NOW_MILLIS - 1_000L, id = 4L), result.latest)
        assertEquals(75, result.averageBpm)
        assertEquals(60, result.minBpm)
        assertEquals(90, result.maxBpm)
        assertEquals(4, result.totalRecords)
        assertEquals(
            listOf(
                DashboardPoint(earliestDayStart, averageBpm = 70, recordCount = 1),
                DashboardPoint(currentDayStart - DAY_MILLIS, averageBpm = 75, recordCount = 2),
                DashboardPoint(currentDayStart, averageBpm = 80, recordCount = 1)
            ),
            result.points
        )
    }

    private fun record(bpm: Int, timestamp: Long, id: Long) = HeartRateRecord(
        id = id,
        bpm = bpm,
        timestamp = timestamp,
        measureType = MeasureType.MANUAL,
        bodyState = BodyState.RESTING
    )

    private companion object {
        const val DAY_MILLIS = 86_400_000L
        const val NOW_MILLIS = 10 * DAY_MILLIS + 12 * 60 * 60 * 1_000L
    }
}

private class DashboardHeartRateRepository(
    private val records: List<HeartRateRecord>
) : HeartRateRepository {
    override suspend fun insertRecord(record: HeartRateRecord): Long = error("Not used")

    override suspend fun getRecordById(id: Long): HeartRateRecord? = records.firstOrNull { it.id == id }

    override suspend fun deleteRecord(id: Long) = error("Not used")

    override fun getAllRecords(): Flow<List<HeartRateRecord>> = flowOf(records)

    override suspend fun getAverageBpm(): Double = records.map { it.bpm }.average()
}
