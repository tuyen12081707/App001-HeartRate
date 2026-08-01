package com.tdev.heartrate.shared.presentation.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.bpm_unit
import app001heartrate.shared.generated.resources.result_add_another
import app001heartrate.shared.generated.resources.result_back_dashboard
import app001heartrate.shared.generated.resources.result_error
import app001heartrate.shared.generated.resources.result_title
import app001heartrate.shared.generated.resources.action_back
import com.tdev.heartrate.shared.presentation.DataState
import com.tdev.heartrate.shared.domain.utils.formatTimestamp
import com.tdev.heartrate.shared.presentation.components.FeatureErrorState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onDashboard: () -> Unit,
    onAddAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.result_title)) }, navigationIcon = { IconButton(onClick = onDashboard) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.action_back)) } }) },
        modifier = modifier
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when (val state = uiState.data) {
                DataState.Idle, DataState.Loading -> CircularProgressIndicator()
                is DataState.Error -> FeatureErrorState(state.message.ifBlank { stringResource(Res.string.result_error) }, stringResource(Res.string.result_back_dashboard), onDashboard)
                is DataState.Success -> {
                    val record = state.data
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(record.bpm.toString(), style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(stringResource(Res.string.bpm_unit), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Text(record.bodyState.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                        Text(formatTimestamp(record.timestamp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        record.note?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onDashboard, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Text(stringResource(Res.string.result_back_dashboard)) }
                        androidx.compose.material3.OutlinedButton(onClick = onAddAnother, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Text(stringResource(Res.string.result_add_another)) }
                    }
                }
            }
        }
    }
}
