package com.tdev.heartrate.shared.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.action_add
import app001heartrate.shared.generated.resources.home_blood_pressure
import app001heartrate.shared.generated.resources.home_blood_sugar
import app001heartrate.shared.generated.resources.home_feel_today
import app001heartrate.shared.generated.resources.home_good_day
import app001heartrate.shared.generated.resources.home_heart
import app001heartrate.shared.generated.resources.home_heart_rate
import app001heartrate.shared.generated.resources.home_heart_wave
import com.tdev.heartrate.shared.presentation.theme.BloodPressureBlue
import com.tdev.heartrate.shared.presentation.theme.BloodPressureSurface
import com.tdev.heartrate.shared.presentation.theme.BloodSugarPink
import com.tdev.heartrate.shared.presentation.theme.BloodSugarSurface
import com.tdev.heartrate.shared.presentation.theme.HomeBackgroundTop
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToBloodPressure: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to HomeBackgroundTop,
                    0.4f to MaterialTheme.colorScheme.background
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(Res.string.home_good_day),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(Res.string.home_feel_today),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(22.dp))

        HeartRateFeatureCard(
            isLoading = uiState.isLoading,
            onAdd = onNavigateToAddRecord
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HealthMetricCard(
                title = stringResource(Res.string.home_blood_pressure),
                illustration = Res.drawable.home_blood_pressure,
                surfaceColor = BloodPressureSurface,
                accentColor = BloodPressureBlue,
                modifier = Modifier.weight(1f),
                onAdd = onNavigateToBloodPressure
            )
            HealthMetricCard(
                title = stringResource(Res.string.home_blood_sugar),
                illustration = Res.drawable.home_blood_sugar,
                surfaceColor = BloodSugarSurface,
                accentColor = BloodSugarPink,
                modifier = Modifier.weight(1f),
                onAdd = {}
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HeartRateFeatureCard(
    isLoading: Boolean,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(156.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.84f),
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
        ) {
            Image(
                painter = painterResource(Res.drawable.home_heart_wave),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.48f,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.home_heart_rate),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                MetricAddButton(
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = onAdd
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.28f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.55f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(Res.drawable.home_heart),
                                contentDescription = stringResource(Res.string.home_heart_rate),
                                modifier = Modifier.size(82.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthMetricCard(
    title: String,
    illustration: DrawableResource,
    surfaceColor: Color,
    accentColor: Color,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(206.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(illustration),
                contentDescription = title,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            MetricAddButton(accentColor = accentColor, onClick = onAdd)
        }
    }
}

@Composable
private fun MetricAddButton(
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(Res.string.action_add),
                style = MaterialTheme.typography.labelLarge,
                color = accentColor
            )
        }
    }
}
