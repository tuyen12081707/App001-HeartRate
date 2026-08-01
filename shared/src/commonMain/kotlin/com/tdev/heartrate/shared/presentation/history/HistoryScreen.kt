package com.tdev.heartrate.shared.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.history_delete
import app001heartrate.shared.generated.resources.history_delete_cancel
import app001heartrate.shared.generated.resources.history_delete_confirm
import app001heartrate.shared.generated.resources.history_delete_message
import app001heartrate.shared.generated.resources.history_empty
import app001heartrate.shared.generated.resources.history_retry
import app001heartrate.shared.generated.resources.history_retry_delete
import app001heartrate.shared.generated.resources.history_bpm_format
import app001heartrate.shared.generated.resources.history_title
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.utils.formatTimestamp
import com.tdev.heartrate.shared.presentation.DataState
import com.tdev.heartrate.shared.presentation.components.EmptyState
import com.tdev.heartrate.shared.presentation.components.FeatureErrorState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onTabSelected: (com.tdev.heartrate.shared.presentation.navigation.MainTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<HeartRateRecord?>(null) }
    pendingDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(Res.string.history_delete)) },
            text = { Text(stringResource(Res.string.history_delete_message)) },
            confirmButton = { TextButton(onClick = { viewModel.onIntent(HistoryIntent.DeleteRecord(record.id)); pendingDelete = null }) { Text(stringResource(Res.string.history_delete_confirm)) } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(stringResource(Res.string.history_delete_cancel)) } }
        )
    }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(Res.string.history_title)) }) }, modifier = modifier) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState.data) {
                DataState.Idle, DataState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DataState.Error -> FeatureErrorState(state.message, stringResource(Res.string.history_retry), onAction = viewModel::retry)
                is DataState.Success -> if (state.data.isEmpty()) {
                    EmptyState(stringResource(Res.string.history_empty), stringResource(Res.string.history_retry), onAction = viewModel::retry)
                } else {
                    val groups = state.data.groupBy { formatTimestamp(it.timestamp).substringAfter(' ', "Unknown day") }
                    Column(Modifier.fillMaxSize()) {
                        val deleteError = uiState.deleteState as? DataState.Error
                        val failedId = uiState.deleteErrorRecordId
                        if (deleteError != null && failedId != null) {
                            FeatureErrorState(
                                message = deleteError.message,
                                actionLabel = stringResource(Res.string.history_retry_delete),
                                onAction = { viewModel.onIntent(HistoryIntent.DeleteRecord(failedId)) }
                            )
                        }
                        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            groups.forEach { (day, records) ->
                                item(key = "header-$day") { Text(day, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                                items(records, key = { it.id }) { record ->
                                    HistoryRecordRow(record) { pendingDelete = record }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordRow(record: HeartRateRecord, onDelete: () -> Unit) {
    androidx.compose.material3.Card(Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.history_bpm_format, record.bpm), style = MaterialTheme.typography.titleLarge)
                Text(record.bodyState.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTimestamp(record.timestamp).substringBefore(' '), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onDelete) { Text(stringResource(Res.string.history_delete)) }
        }
    }
}
