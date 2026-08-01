package com.tdev.heartrate.shared.presentation.add

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.add_record_body_state
import app001heartrate.shared.generated.resources.add_record_bpm_label
import app001heartrate.shared.generated.resources.add_record_camera_action
import app001heartrate.shared.generated.resources.add_record_error_invalid
import app001heartrate.shared.generated.resources.add_record_error_range
import app001heartrate.shared.generated.resources.add_record_note_label
import app001heartrate.shared.generated.resources.add_record_save_button
import app001heartrate.shared.generated.resources.add_record_title
import app001heartrate.shared.generated.resources.action_back
import app001heartrate.shared.generated.resources.body_state_after_waking
import app001heartrate.shared.generated.resources.body_state_before_bed
import app001heartrate.shared.generated.resources.body_state_exercising
import app001heartrate.shared.generated.resources.body_state_resting
import app001heartrate.shared.generated.resources.body_state_sleeping
import com.tdev.heartrate.shared.domain.model.BodyState
import com.tdev.heartrate.shared.presentation.DataState
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    viewModel: AddRecordViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    onOpenCamera: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collectLatest { effect ->
            if (effect is AddRecordSideEffect.NavigateToResult) onSaved(effect.recordId)
        }
    }
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.add_record_title)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.action_back)) } }) },
        modifier = modifier
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(Res.string.add_record_bpm_label), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            WheelNumberPicker(
                selectedValue = uiState.bpm.toIntOrNull() ?: 80,
                onValueChange = { viewModel.onIntent(AddRecordIntent.UpdateBpm(it.toString())) }
            )
            onOpenCamera?.let { openCamera ->
                OutlinedButton(
                    onClick = openCamera,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.add_record_camera_action))
                }
            }
            if (uiState.fieldErrors.containsKey("bpm")) {
                Text(
                    text = if (uiState.bpm.toIntOrNull() == null) stringResource(Res.string.add_record_error_invalid) else stringResource(Res.string.add_record_error_range),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(stringResource(Res.string.add_record_body_state), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BodyState.entries.forEach { state ->
                    BodyStateTile(state, bodyStateLabel(state), state == uiState.bodyState) { viewModel.onIntent(AddRecordIntent.UpdateBodyState(state)) }
                }
            }
            if (uiState.fieldErrors.containsKey("bodyState")) Text(uiState.fieldErrors.getValue("bodyState"), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.onIntent(AddRecordIntent.UpdateNote(it)) },
                label = { Text(stringResource(Res.string.add_record_note_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(16.dp)
            )
            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Button(
                onClick = { viewModel.onIntent(AddRecordIntent.SaveRecord) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text(stringResource(Res.string.add_record_save_button))
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun bodyStateLabel(state: BodyState): String = when (state) {
    BodyState.RESTING -> stringResource(Res.string.body_state_resting)
    BodyState.EXERCISING -> stringResource(Res.string.body_state_exercising)
    BodyState.SLEEPING -> stringResource(Res.string.body_state_sleeping)
    BodyState.AFTER_WAKING_UP -> stringResource(Res.string.body_state_after_waking)
    BodyState.BEFORE_BED -> stringResource(Res.string.body_state_before_bed)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelNumberPicker(selectedValue: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier, range: IntRange = 30..250) {
    val values = remember(range) { range.toList() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedValue - range.first).coerceIn(0, values.lastIndex))
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val centeredIndex by remember { derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, values.lastIndex) } }
    LaunchedEffect(listState.isScrollInProgress) { if (!listState.isScrollInProgress) onValueChange(values[centeredIndex]) }
    LaunchedEffect(selectedValue) {
        val index = values.indexOf(selectedValue)
        if (index >= 0 && index != listState.firstVisibleItemIndex) listState.scrollToItem(index)
    }
    Box(modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth(.58f).height(58.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)))
        LazyColumn(state = listState, flingBehavior = flingBehavior, contentPadding = PaddingValues(vertical = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            items(values.size) { index ->
                val selected = index == centeredIndex
                Text(values[index].toString(), modifier = Modifier.height(60.dp).fillMaxWidth().scale(if (selected) 1.45f else .9f), color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f), fontSize = 24.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
fun BodyStateTile(state: BodyState, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(92.dp).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(8.dp), contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
