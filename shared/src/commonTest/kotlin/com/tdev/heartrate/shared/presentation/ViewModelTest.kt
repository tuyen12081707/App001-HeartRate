package com.tdev.heartrate.shared.presentation

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.usecase.AddHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.DeleteHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.usecase.GetDashboardDataUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateHistoryUseCase
import com.tdev.heartrate.shared.domain.usecase.GetHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.utils.Clock
import com.tdev.heartrate.shared.presentation.add.AddRecordIntent
import com.tdev.heartrate.shared.presentation.add.AddRecordViewModel
import com.tdev.heartrate.shared.presentation.dashboard.DashboardViewModel
import com.tdev.heartrate.shared.presentation.history.HistoryIntent
import com.tdev.heartrate.shared.presentation.history.HistoryViewModel
import com.tdev.heartrate.shared.presentation.result.ResultViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dashboardMapsRecordFlowToDataStateSuccess() = runTest {
        val repository = FakeHeartRateRepository(records = listOf(record(id = 1L, bpm = 72)))
        val viewModel = DashboardViewModel(
            GetDashboardDataUseCase(repository, Clock { 1_000L }, TimeZone.UTC)
        )

        advanceUntilIdle()

        val data = (viewModel.uiState.value.data as DataState.Success).data
        assertEquals(1, data.totalRecords)
        assertEquals(72, data.averageBpm)
    }

    @Test
    fun dashboardRetryReexecutesFailedFlow() = runTest {
        var attempts = 0
        val repository = FakeHeartRateRepository(
            records = listOf(record(id = 1L, bpm = 72)),
            getAllRecordsOverride = {
                flow {
                    if (attempts++ == 0) throw IllegalStateException("temporary")
                    emit(listOf(record(id = 1L, bpm = 72)))
                }
            }
        )
        val viewModel = DashboardViewModel(GetDashboardDataUseCase(repository, Clock { 1_000L }, TimeZone.UTC))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.data is DataState.Error)
        viewModel.retry()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.data is DataState.Success)
    }

    @Test
    fun historyMapsRecordFlowToDataStateSuccess() = runTest {
        val record = record(id = 1L, bpm = 72)
        val repository = FakeHeartRateRepository(records = listOf(record))
        val viewModel = HistoryViewModel(
            GetHeartRateHistoryUseCase(repository),
            DeleteHeartRateRecordUseCase(repository)
        )

        advanceUntilIdle()

        assertEquals(listOf(record), (viewModel.uiState.value.data as DataState.Success).data)
    }

    @Test
    fun historyRetryReexecutesFailedFlow() = runTest {
        var attempts = 0
        val record = record(id = 1L, bpm = 72)
        val repository = FakeHeartRateRepository(
            getAllRecordsOverride = {
                flow {
                    if (attempts++ == 0) throw IllegalStateException("temporary")
                    emit(listOf(record))
                }
            }
        )
        val viewModel = HistoryViewModel(GetHeartRateHistoryUseCase(repository), DeleteHeartRateRecordUseCase(repository))
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.data is DataState.Error)
        viewModel.retry()
        advanceUntilIdle()
        assertEquals(listOf(record), (viewModel.uiState.value.data as DataState.Success).data)
    }

    @Test
    fun failedDeleteRetainsTheVisibleHistoryRow() = runTest {
        val record = record(id = 1L, bpm = 72)
        val repository = FakeHeartRateRepository(records = listOf(record), deleteFailure = IllegalStateException("delete failed"))
        val viewModel = HistoryViewModel(
            GetHeartRateHistoryUseCase(repository),
            DeleteHeartRateRecordUseCase(repository)
        )
        advanceUntilIdle()

        viewModel.onIntent(HistoryIntent.DeleteRecord(record.id))
        advanceUntilIdle()

        assertEquals(listOf(record), (viewModel.uiState.value.data as DataState.Success).data)
        assertTrue(viewModel.uiState.value.deleteState is DataState.Error)
    }

    @Test
    fun failedDeleteCanBeRetriedForTheSameRecord() = runTest {
        val record = record(id = 1L, bpm = 72)
        val repository = FakeHeartRateRepository(records = listOf(record), deleteFailuresRemaining = 1)
        val viewModel = HistoryViewModel(GetHeartRateHistoryUseCase(repository), DeleteHeartRateRecordUseCase(repository))
        advanceUntilIdle()
        viewModel.onIntent(HistoryIntent.DeleteRecord(record.id))
        advanceUntilIdle()
        assertEquals(record.id, viewModel.uiState.value.deleteErrorRecordId)
        viewModel.onIntent(HistoryIntent.DeleteRecord(record.id))
        advanceUntilIdle()
        assertEquals(DataState.Success(record.id), viewModel.uiState.value.deleteState)
        assertTrue((viewModel.uiState.value.data as DataState.Success).data.isEmpty())
    }

    @Test
    fun addValidationKeepsFormFieldsAndReportsFieldErrors() {
        val viewModel = AddRecordViewModel(AddHeartRateRecordUseCase(FakeHeartRateRepository(), Clock { 1L }))
        viewModel.onIntent(AddRecordIntent.UpdateBpm("bad"))
        viewModel.onIntent(AddRecordIntent.UpdateBodyState(BodyState.RESTING))
        viewModel.onIntent(AddRecordIntent.UpdateNote("keep me"))
        viewModel.onIntent(AddRecordIntent.SaveRecord)

        assertEquals("bad", viewModel.uiState.value.bpm)
        assertEquals(BodyState.RESTING, viewModel.uiState.value.bodyState)
        assertEquals("keep me", viewModel.uiState.value.note)
        assertTrue(viewModel.uiState.value.fieldErrors.containsKey("bpm"))
    }

    @Test
    fun addSaveIgnoresSecondIntentDuringLoadingAndAfterSuccess() = runTest {
        val completion = CompletableDeferred<Long>()
        val repository = FakeHeartRateRepository(insertResult = { completion.await() })
        val viewModel = AddRecordViewModel(AddHeartRateRecordUseCase(repository, Clock { 1L }))
        viewModel.onIntent(AddRecordIntent.UpdateBpm("72"))
        viewModel.onIntent(AddRecordIntent.UpdateBodyState(BodyState.RESTING))

        viewModel.onIntent(AddRecordIntent.SaveRecord)
        viewModel.onIntent(AddRecordIntent.SaveRecord)
        runCurrent()
        assertEquals(1, repository.insertCalls)

        completion.complete(9L)
        advanceUntilIdle()
        viewModel.onIntent(AddRecordIntent.SaveRecord)

        assertEquals(1, repository.insertCalls)
        assertEquals(DataState.Success(9L), viewModel.uiState.value.saveState)
    }

    @Test
    fun addNewEntryResetsCompletedForm() {
        val viewModel = AddRecordViewModel(AddHeartRateRecordUseCase(FakeHeartRateRepository(), Clock { 1L }))
        viewModel.onIntent(AddRecordIntent.UpdateBpm("72"))
        viewModel.onIntent(AddRecordIntent.UpdateBodyState(BodyState.RESTING))
        viewModel.onIntent(AddRecordIntent.UpdateNote("morning"))

        viewModel.onIntent(AddRecordIntent.ResetForNewEntry)

        assertEquals("", viewModel.uiState.value.bpm)
        assertEquals(null, viewModel.uiState.value.bodyState)
        assertEquals("", viewModel.uiState.value.note)
        assertEquals(DataState.Idle, viewModel.uiState.value.saveState)
    }

    @Test
    fun resultLoadsItsRequestedRecordAndMissingIdIsAnError() = runTest {
        val first = record(id = 1L, bpm = 61)
        val second = record(id = 2L, bpm = 92)
        val repository = FakeHeartRateRepository(records = listOf(first, second))

        val firstViewModel = ResultViewModel(GetHeartRateRecordUseCase(repository), first.id)
        advanceUntilIdle()
        assertEquals(first, (firstViewModel.uiState.value.data as DataState.Success).data)

        val secondViewModel = ResultViewModel(GetHeartRateRecordUseCase(repository), second.id)
        advanceUntilIdle()
        assertEquals(second, (secondViewModel.uiState.value.data as DataState.Success).data)

        val missingViewModel = ResultViewModel(GetHeartRateRecordUseCase(repository), 404L)
        advanceUntilIdle()
        assertTrue(missingViewModel.uiState.value.data is DataState.Error)
    }
}

private class FakeHeartRateRepository(
    records: List<HeartRateRecord> = emptyList(),
    private val insertResult: suspend () -> Long = { 1L },
    private val deleteFailure: Throwable? = null,
    private val getAllRecordsOverride: (() -> Flow<List<HeartRateRecord>>)? = null,
    var deleteFailuresRemaining: Int = 0
) : HeartRateRepository {
    private val recordFlow = MutableStateFlow(records)
    var insertCalls = 0
        private set
    private var deleteCalls = 0

    override suspend fun insertRecord(record: HeartRateRecord): Long {
        insertCalls += 1
        return insertResult()
    }

    override suspend fun getRecordById(id: Long): HeartRateRecord? = recordFlow.value.firstOrNull { it.id == id }

    override suspend fun deleteRecord(id: Long) {
        deleteCalls += 1
        if (deleteFailuresRemaining > 0) {
            deleteFailuresRemaining -= 1
            throw IllegalStateException("delete failed")
        }
        deleteFailure?.let { throw it }
        recordFlow.value = recordFlow.value.filterNot { it.id == id }
    }

    override fun getAllRecords(): Flow<List<HeartRateRecord>> = getAllRecordsOverride?.invoke() ?: recordFlow

    override suspend fun getAverageBpm(): Double = 0.0
}

private fun record(id: Long, bpm: Int) = HeartRateRecord(
    id = id,
    bpm = bpm,
    timestamp = 1_000L,
    measureType = MeasureType.MANUAL,
    bodyState = BodyState.RESTING
)
