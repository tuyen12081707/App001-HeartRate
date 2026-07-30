package com.tdev.heartrate.shared.presentation.bloodpressure

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app001heartrate.shared.generated.resources.*
import com.tdev.heartrate.shared.domain.model.BloodPressureInputConstraints
import com.tdev.heartrate.shared.domain.model.BloodPressureLevel
import com.tdev.heartrate.shared.domain.model.BloodPressureThresholds
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

@Composable
fun BloodPressureScreen(
    viewModel: BloodPressureViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDateDialog by remember { mutableStateOf(false) }

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
                onClick = { showDateDialog = true }
            )
            Spacer(modifier = Modifier.height(12.dp))

            NoteCard(
                note = uiState.note,
                onNoteChange = { viewModel.onIntent(BloodPressureIntent.UpdateNote(it)) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            BloodPressureInfoCard(
                level = uiState.level
            )
            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    if (showDateDialog) {
        BloodPressureDateTimeDialog(
            timestamp = uiState.timestamp,
            onDismiss = { showDateDialog = false },
            onConfirm = { timestamp ->
                viewModel.onIntent(BloodPressureIntent.UpdateTimestamp(timestamp))
                showDateDialog = false
            }
        )
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
            text = stringResource(Res.string.blood_pressure_new_record),
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
                MeasurementLabel(
                    title = stringResource(Res.string.blood_pressure_systolic),
                    unit = stringResource(Res.string.blood_pressure_unit_mmhg)
                )
                MeasurementLabel(
                    title = stringResource(Res.string.blood_pressure_diastolic),
                    unit = stringResource(Res.string.blood_pressure_unit_mmhg)
                )
                MeasurementLabel(
                    title = stringResource(Res.string.blood_pressure_pulse),
                    unit = stringResource(Res.string.blood_pressure_unit_bpm)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NumberWheelPicker(
                    selectedValue = systolic,
                    range = BloodPressureInputConstraints.SYSTOLIC_RANGE,
                    onValueChange = onSystolicChange,
                    modifier = Modifier.width(60.dp)
                )
                NumberWheelPicker(
                    selectedValue = diastolic,
                    range = BloodPressureInputConstraints.DIASTOLIC_RANGE,
                    onValueChange = onDiastolicChange,
                    modifier = Modifier.width(60.dp)
                )
                NumberWheelPicker(
                    selectedValue = pulse,
                    range = BloodPressureInputConstraints.PULSE_RANGE,
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
                    text = stringResource(Res.string.blood_pressure_date),
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
                    text = formatBloodPressureDate(
                        timestamp = timestamp,
                        monthLabel = monthLabel(
                            Instant.fromEpochMilliseconds(timestamp)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .monthNumber
                        )
                    ),
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
private fun BloodPressureDateTimeDialog(
    timestamp: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val timeZone = TimeZone.currentSystemDefault()
    val initialDateTime = remember(timestamp) {
        Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone)
    }
    var hour by remember(timestamp) { mutableIntStateOf(initialDateTime.hour) }
    var minute by remember(timestamp) { mutableIntStateOf(initialDateTime.minute) }
    var day by remember(timestamp) { mutableIntStateOf(initialDateTime.dayOfMonth) }
    var month by remember(timestamp) { mutableIntStateOf(initialDateTime.monthNumber) }
    var year by remember(timestamp) { mutableIntStateOf(initialDateTime.year) }
    val availableDays = remember(month, year) { (1..daysInMonth(month, year)).toList() }
    val availableYears = remember(initialDateTime.year) {
        ((initialDateTime.year - DATE_YEAR_RANGE)..(initialDateTime.year + DATE_YEAR_RANGE)).toList()
    }
    val monthLabels = listOf(
        stringResource(Res.string.month_jan),
        stringResource(Res.string.month_feb),
        stringResource(Res.string.month_mar),
        stringResource(Res.string.month_apr),
        stringResource(Res.string.month_may),
        stringResource(Res.string.month_jun),
        stringResource(Res.string.month_jul),
        stringResource(Res.string.month_aug),
        stringResource(Res.string.month_sep),
        stringResource(Res.string.month_oct),
        stringResource(Res.string.month_nov),
        stringResource(Res.string.month_dec)
    )

    LaunchedEffect(availableDays) {
        day = day.coerceAtMost(availableDays.last())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.blood_pressure_date_dialog_title),
                        color = TextDarkCharcoal,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = TextDarkCharcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DialogSectionLabel(
                        icon = Icons.Default.AccessTime,
                        text = stringResource(Res.string.blood_pressure_date_dialog_time),
                        modifier = Modifier.weight(2f)
                    )
                    DialogSectionLabel(
                        icon = Icons.Default.CalendarToday,
                        text = stringResource(Res.string.blood_pressure_date_dialog_title),
                        modifier = Modifier.weight(3f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DialogWheelPicker(
                        values = (0..23).toList(),
                        selectedValue = hour,
                        valueLabel = { it.toString().padStart(2, '0') },
                        onValueChange = { hour = it },
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ":",
                        color = TextDarkCharcoal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    DialogWheelPicker(
                        values = (0..59).toList(),
                        selectedValue = minute,
                        valueLabel = { it.toString().padStart(2, '0') },
                        onValueChange = { minute = it },
                        modifier = Modifier.weight(1f)
                    )
                    DialogWheelPicker(
                        values = availableDays,
                        selectedValue = day,
                        valueLabel = { it.toString() },
                        onValueChange = { day = it },
                        modifier = Modifier.weight(1f)
                    )
                    DialogWheelPicker(
                        values = (1..12).toList(),
                        selectedValue = month,
                        valueLabel = { monthNumber -> monthLabels[monthNumber - 1] },
                        onValueChange = { month = it },
                        modifier = Modifier.weight(1f)
                    )
                    DialogWheelPicker(
                        values = availableYears,
                        selectedValue = year,
                        valueLabel = { it.toString() },
                        onValueChange = { year = it },
                        modifier = Modifier.weight(1.25f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, DisabledGray),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDarkCharcoal)
                    ) {
                        Text(text = stringResource(Res.string.blood_pressure_date_dialog_cancel))
                    }
                    Button(
                        onClick = {
                            val selectedTimestamp = LocalDateTime(
                                year = year,
                                monthNumber = month,
                                dayOfMonth = day,
                                hour = hour,
                                minute = minute
                            ).toInstant(timeZone).toEpochMilliseconds()
                            onConfirm(selectedTimestamp)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = SurfaceWhite
                        )
                    ) {
                        Text(text = stringResource(Res.string.blood_pressure_date_dialog_confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogSectionLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextDarkCharcoal,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = TextDarkCharcoal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> DialogWheelPicker(
    values: List<T>,
    selectedValue: T,
    valueLabel: (T) -> String,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = values.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val centeredIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceIn(0, values.lastIndex) }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            values.getOrNull(centeredIndex)?.let { centeredValue ->
                if (centeredValue != selectedValue) onValueChange(centeredValue)
            }
        }
    }
    LaunchedEffect(selectedValue, values) {
        val targetIndex = values.indexOf(selectedValue)
        if (targetIndex >= 0 && listState.firstVisibleItemIndex != targetIndex) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 35.dp),
        modifier = modifier.height(105.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(values) { value ->
            val isSelected = value == values[centeredIndex]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) BackgroundWhite else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = valueLabel(value),
                    color = if (isSelected) TextDarkCharcoal else DisabledGray,
                    fontSize = if (isSelected) 16.sp else 13.sp,
                    lineHeight = 20.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
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
                    text = stringResource(Res.string.blood_pressure_note),
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
                            text = stringResource(Res.string.blood_pressure_note_placeholder),
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
        BloodPressureLevelRow(
            level = BloodPressureLevel.HYPOTENSION,
            label = stringResource(Res.string.blood_pressure_level_hypotension),
            condition = stringResource(
                Res.string.blood_pressure_condition_hypotension,
                BloodPressureThresholds.HYPOTENSION_SYSTOLIC_MAX,
                BloodPressureThresholds.HYPOTENSION_DIASTOLIC_MAX
            )
        ),
        BloodPressureLevelRow(
            level = BloodPressureLevel.NORMAL,
            label = stringResource(Res.string.blood_pressure_level_normal),
            condition = stringResource(
                Res.string.blood_pressure_condition_normal,
                BloodPressureThresholds.NORMAL_SYSTOLIC_MIN,
                BloodPressureThresholds.NORMAL_SYSTOLIC_MAX,
                BloodPressureThresholds.NORMAL_DIASTOLIC_MIN,
                BloodPressureThresholds.NORMAL_DIASTOLIC_MAX
            )
        ),
        BloodPressureLevelRow(
            level = BloodPressureLevel.HYPERTENSION_STAGE_1,
            label = stringResource(Res.string.blood_pressure_level_stage_1),
            condition = stringResource(
                Res.string.blood_pressure_condition_stage_1,
                BloodPressureThresholds.STAGE_1_SYSTOLIC_MIN,
                BloodPressureThresholds.STAGE_1_SYSTOLIC_MAX,
                BloodPressureThresholds.STAGE_1_DIASTOLIC_MIN,
                BloodPressureThresholds.STAGE_1_DIASTOLIC_MAX
            )
        ),
        BloodPressureLevelRow(
            level = BloodPressureLevel.HYPERTENSION_STAGE_2,
            label = stringResource(Res.string.blood_pressure_level_stage_2),
            condition = stringResource(
                Res.string.blood_pressure_condition_stage_2,
                BloodPressureThresholds.STAGE_2_SYSTOLIC_MIN,
                BloodPressureThresholds.STAGE_2_SYSTOLIC_MAX,
                BloodPressureThresholds.STAGE_2_DIASTOLIC_MIN,
                BloodPressureThresholds.STAGE_2_DIASTOLIC_MAX
            )
        ),
        BloodPressureLevelRow(
            level = BloodPressureLevel.HYPERTENSIVE_CRISIS,
            label = stringResource(Res.string.blood_pressure_level_crisis),
            condition = stringResource(
                Res.string.blood_pressure_condition_crisis,
                BloodPressureThresholds.CRISIS_SYSTOLIC_MIN,
                BloodPressureThresholds.CRISIS_DIASTOLIC_MIN
            )
        )
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
                text = rows.first { it.level == level }.label,
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
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(90.dp))
                            .background(colors.getValue(row.level))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = row.label,
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = row.condition,
                        color = TextGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

private data class BloodPressureLevelRow(
    val level: BloodPressureLevel,
    val label: String,
    val condition: String
)

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
                text = stringResource(Res.string.blood_pressure_save),
                color = SurfaceWhite,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun monthLabel(monthNumber: Int): String =
    stringResource(
        when (monthNumber) {
            1 -> Res.string.month_jan
            2 -> Res.string.month_feb
            3 -> Res.string.month_mar
            4 -> Res.string.month_apr
            5 -> Res.string.month_may
            6 -> Res.string.month_jun
            7 -> Res.string.month_jul
            8 -> Res.string.month_aug
            9 -> Res.string.month_sep
            10 -> Res.string.month_oct
            11 -> Res.string.month_nov
            else -> Res.string.month_dec
        }
    )

private fun daysInMonth(month: Int, year: Int): Int =
    when (month) {
        2 -> if (isLeapYear(year)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

private fun isLeapYear(year: Int): Boolean =
    year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)

private fun formatBloodPressureDate(timestamp: Long, monthLabel: String): String {
    val dateTime = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$monthLabel $day,${dateTime.year} | $hour:$minute"
}

private const val DATE_YEAR_RANGE = 5
