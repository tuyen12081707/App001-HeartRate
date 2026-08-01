package com.tdev.heartrate.shared.presentation

import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.repository.HeartRateRepository
import com.tdev.heartrate.shared.domain.usecase.AddHeartRateRecordUseCase
import com.tdev.heartrate.shared.domain.utils.Clock
import com.tdev.heartrate.shared.presentation.add.AddRecordIntent
import com.tdev.heartrate.shared.presentation.add.AddRecordViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViewModelTest {
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

}

private class FakeHeartRateRepository : HeartRateRepository {
    private val records = MutableStateFlow<List<HeartRateRecord>>(emptyList())

    override suspend fun insertRecord(record: HeartRateRecord): Long = 1L
    override suspend fun getRecordById(id: Long): HeartRateRecord? = records.value.firstOrNull { it.id == id }
    override suspend fun deleteRecord(id: Long) = Unit
    override fun getAllRecords(): Flow<List<HeartRateRecord>> = records
    override suspend fun getAverageBpm(): Double = 0.0
}
