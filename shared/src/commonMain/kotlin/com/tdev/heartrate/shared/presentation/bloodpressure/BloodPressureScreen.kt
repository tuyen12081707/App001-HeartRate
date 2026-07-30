package com.tdev.heartrate.shared.presentation.bloodpressure

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tdev.heartrate.shared.domain.model.BloodPressureLevel
import com.tdev.heartrate.shared.domain.model.level
import com.tdev.heartrate.shared.presentation.components.AnimatedPrimaryButton
import com.tdev.heartrate.shared.presentation.theme.BloodPressureCrisis
import com.tdev.heartrate.shared.presentation.theme.BloodPressureHypotension
import com.tdev.heartrate.shared.presentation.theme.BloodPressureNormal
import com.tdev.heartrate.shared.presentation.theme.BloodPressureStage1
import com.tdev.heartrate.shared.presentation.theme.BloodPressureStage2
import com.tdev.heartrate.shared.presentation.theme.BackgroundWhite
import com.tdev.heartrate.shared.presentation.theme.DisabledGray
import com.tdev.heartrate.shared.presentation.theme.PrimaryRed
import com.tdev.heartrate.shared.presentation.theme.SurfaceWhite
import com.tdev.heartrate.shared.presentation.theme.TextDarkCharcoal
import com.tdev.heartrate.shared.presentation.theme.TextGray
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BloodPressureScreen(
    viewModel: BloodPressureViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.sideEffect) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                BloodPressureSideEffect.NavigateBack -> onNavigateBack()
                is BloodPressureSideEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = BackgroundWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SaveBar(
                enabled = !uiState.isLoading,
                onSave = { viewModel.onIntent(BloodPressureIntent.SaveRecord) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(onNavigateBack = onNavigateBack)
            Spacer(modifier = Modifier.height(24.dp))

            MeasurementCard(
                systolic = uiState.systolic,
                diastolic = uiState.diastolic,
                pulse = uiState.pulse,
                onSystolicChange = { viewModel.onIntent(BloodPressureIntent.UpdateSystolic(it)) },
                onDiastolicChange = { viewModel.onIntent(BloodPressureIntent.UpdateDiastolic(it)) },
                onPulseChange = { viewModel.onIntent(BloodPressureIntent.UpdatePulse(it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            DateCard(
                timestamp = uiState.timestamp,
                onClick = { viewModel.onIntent(BloodPressureIntent.RefreshTimestamp) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            NoteCard(
                note = uiState.note,
                onNoteChange = { viewModel.onIntent(BloodPressureIntent.UpdateNote(it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            BloodPressureInfoCard(
                level = com.tdev.heartrate.shared.domain.model.BloodPressureRecord(
                    systolic = uiState.systolic,
                    diastolic = uiState.diastolic,
                    pulse = uiState.pulse,
                    timestamp = uiState.timestamp
                ).level()
            )
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun Header(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextDarkCharcoal
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "New Record",
            color = TextDarkCharcoal,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MeasurementCard(
    systolic: Int,
    diastolic: Int,
    pulse: Int,
    onSystolicChange: (Int) -> Unit,
    onDiastolicChange: (Int) -> Unit,
    onPulseChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MeasurementLabel(title = "Systolic", unit = "mmHg")
                MeasurementLabel(title = "Diastolic", unit = "mmHg")
                MeasurementLabel(title = "Pulse", unit = "bpm")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NumberWheelPicker(
                    selectedValue = systolic,
                    range = 40..250,
                    onValueChange = onSystolicChange,
                    modifier = Modifier.width(60.dp)
                )
                NumberWheelPicker(
                    selectedValue = diastolic,
                    range = 20..150,
                    onValueChange = onDiastolicChange,
                    modifier = Modifier.width(60.dp)
                )
                NumberWheelPicker(
                    selectedValue = pulse,
                    range = 30..250,
                    onValueChange = onPulseChange,
                    modifier = Modifier.width(60.dp)
                )
            }
        }
    }
}

@Composable
private fun MeasurementLabel(title: String, unit: String) {
    Column(
        modifier = Modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = TextDarkCharcoal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = unit,
            color = TextGray,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NumberWheelPicker(
    selectedValue: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val values = remember(range) { range.toList() }
    val initialIndex = remember(selectedValue, values) {
        (selectedValue - range.first).coerceIn(0, values.lastIndex)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val centeredIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, values.lastIndex) }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            values.getOrNull(centeredIndex)?.let { value ->
                if (value != selectedValue) onValueChange(value)
            }
        }
    }
    LaunchedEffect(selectedValue) {
        val targetIndex = values.indexOf(selectedValue)
        if (targetIndex >= 0 && listState.firstVisibleItemIndex != targetIndex) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp),
        modifier = modifier.height(128.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(values) { value ->
            val isSelected = value == values[centeredIndex]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        color = if (isSelected) Color.Transparent else Color.Transparent,
                        shape = RoundedCornerShape(0.dp)
                    )
                    .then(
                        if (isSelected) {
                            Modifier.drawBehind {
                                drawLine(
                                    color = PrimaryRed,
                                    start = androidx.compose.ui.geometry.Offset.Zero,
                                    end = androidx.compose.ui.geometry.Offset(size.width, 0f)
                                )
                                drawLine(
                                    color = PrimaryRed,
                                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                                    end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                                )
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    color = if (isSelected) PrimaryRed else DisabledGray,
                    fontSize = if (isSelected) 28.sp else 22.sp,
                    lineHeight = if (isSelected) 34.sp else 28.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DateCard(timestamp: Long, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextDarkCharcoal,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Date",
                    color = TextDarkCharcoal,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = DisabledGray,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = formatBloodPressureDate(timestamp),
                    color = TextGray,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun NoteCard(note: String, onNoteChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Note,
                    contentDescription = null,
                    tint = TextDarkCharcoal,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Note",
                    color = TextDarkCharcoal,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            BasicTextField(
                value = note,
                onValueChange = onNoteChange,
                textStyle = TextStyle(
                    color = TextDarkCharcoal,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 8.dp),
                decorationBox = { innerTextField ->
                    if (note.isBlank()) {
                        Text(
                            text = "Type a note",
                            color = DisabledGray,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun BloodPressureInfoCard(level: BloodPressureLevel) {
    val rows = listOf(
        Triple(BloodPressureLevel.HYPOTENSION, "Hypotension", "SYS<90 OR DIA <60"),
        Triple(BloodPressureLevel.NORMAL, "Normal", "SYS 90-119 & DIA 60-79"),
        Triple(BloodPressureLevel.HYPERTENSION_STAGE_1, "Hypertension Stage 1", "SYS 130-139 & DIA 80-89"),
        Triple(BloodPressureLevel.HYPERTENSION_STAGE_2, "Hypertension Stage 2", "SYS 140-180 & DIA 90-120"),
        Triple(BloodPressureLevel.HYPERTENSIVE_CRISIS, "Hypertensive Crisis", "SYS >180 OR DIA >120")
    )
    val colors = mapOf(
        BloodPressureLevel.HYPOTENSION to BloodPressureHypotension,
        BloodPressureLevel.NORMAL to BloodPressureNormal,
        BloodPressureLevel.HYPERTENSION_STAGE_1 to BloodPressureStage1,
        BloodPressureLevel.HYPERTENSION_STAGE_2 to BloodPressureStage2,
        BloodPressureLevel.HYPERTENSIVE_CRISIS to BloodPressureCrisis
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = rows.first { it.first == level }.second,
                color = TextDarkCharcoal,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextDarkCharcoal,
                modifier = Modifier.size(16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.values.forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(11.dp)
                            .clip(RoundedCornerShape(90.dp))
                            .background(color)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            rows.forEach { (rowLevel, label, condition) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(90.dp))
                            .background(colors.getValue(rowLevel))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = condition,
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveBar(enabled: Boolean, onSave: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceWhite)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        AnimatedPrimaryButton(
            onClick = onSave,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            containerColor = PrimaryRed,
            contentColor = SurfaceWhite,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            Text(
                text = "Save",
                color = SurfaceWhite,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatBloodPressureDate(timestamp: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dateTime.month.name.lowercase().take(3).replaceFirstChar { it.uppercase() }
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$month $day,${dateTime.year} | $hour:$minute"
}
