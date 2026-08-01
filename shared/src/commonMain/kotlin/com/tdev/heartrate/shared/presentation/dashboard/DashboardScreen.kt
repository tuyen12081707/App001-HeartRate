package com.tdev.heartrate.shared.presentation.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.bpm_unit
import app001heartrate.shared.generated.resources.dashboard_add_reading
import app001heartrate.shared.generated.resources.dashboard_average
import app001heartrate.shared.generated.resources.dashboard_count
import app001heartrate.shared.generated.resources.dashboard_empty
import app001heartrate.shared.generated.resources.dashboard_error
import app001heartrate.shared.generated.resources.dashboard_highest
import app001heartrate.shared.generated.resources.dashboard_latest
import app001heartrate.shared.generated.resources.dashboard_lowest
import app001heartrate.shared.generated.resources.dashboard_retry
import app001heartrate.shared.generated.resources.dashboard_title
import com.tdev.heartrate.shared.domain.model.DashboardData
import com.tdev.heartrate.shared.presentation.DataState
import com.tdev.heartrate.shared.presentation.components.EmptyState
import com.tdev.heartrate.shared.presentation.components.FeatureErrorState
import com.tdev.heartrate.shared.presentation.components.HeartRateChart
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAdd: () -> Unit,
    onTabSelected: (com.tdev.heartrate.shared.presentation.navigation.MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text(stringResource(Res.string.dashboard_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        when (val state = uiState.data) {
            DataState.Idle, DataState.Loading -> Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is DataState.Error -> FeatureErrorState(
                message = state.message.ifBlank { stringResource(Res.string.dashboard_error) },
                actionLabel = stringResource(Res.string.dashboard_retry),
                onAction = viewModel::retry
            )
            is DataState.Success -> DashboardContent(state.data, onAdd)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DashboardContent(data: DashboardData, onAdd: () -> Unit) {
    val latest = data.latest
    if (latest == null && data.totalRecords == 0) {
        EmptyState(stringResource(Res.string.dashboard_empty), stringResource(Res.string.dashboard_add_reading), onAdd)
        return
    }
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.dashboard_latest), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(latest?.bpm?.toString() ?: "—", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(stringResource(Res.string.bpm_unit), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(stringResource(Res.string.dashboard_average), data.averageBpm, Modifier.weight(1f))
        StatCard(stringResource(Res.string.dashboard_count), data.totalRecords, Modifier.weight(1f))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard(stringResource(Res.string.dashboard_highest), data.maxBpm, Modifier.weight(1f))
        StatCard(stringResource(Res.string.dashboard_lowest), data.minBpm, Modifier.weight(1f))
    }
    HeartRateChart(data.points)
    Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(stringResource(Res.string.dashboard_add_reading))
    }
}

@Composable
private fun StatCard(title: String, value: Int, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
