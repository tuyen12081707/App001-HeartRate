package com.tdev.heartrate.shared.presentation.camera

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.camera_completed
import app001heartrate.shared.generated.resources.camera_error
import app001heartrate.shared.generated.resources.camera_initializing
import app001heartrate.shared.generated.resources.camera_instruction
import app001heartrate.shared.generated.resources.camera_measuring
import app001heartrate.shared.generated.resources.camera_no_finger
import app001heartrate.shared.generated.resources.camera_failed
import app001heartrate.shared.generated.resources.camera_title
import org.jetbrains.compose.resources.stringResource
import com.tdev.heartrate.shared.domain.sensor.CameraHeartRateSensor
import com.tdev.heartrate.shared.domain.sensor.CameraMeasurementState
import com.tdev.heartrate.shared.domain.sensor.SensorState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraMeasurementScreen(
    onNavigateBack: () -> Unit,
    onMeasurementCompleted: (Int) -> Unit,
    onMeasurementFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sensor = koinInject<CameraHeartRateSensor>()
    val flow = remember { sensor.startMeasurement() }
    val state by flow.collectAsState(initial = CameraMeasurementState())

    DisposableEffect(Unit) {
        onDispose {
            sensor.stopMeasurement()
        }
    }

    LaunchedEffect(state.state) {
        if (state.state == SensorState.COMPLETED && state.bpm > 0) {
            onMeasurementCompleted(state.bpm)
        } else if (state.state == SensorState.FAILED) {
            onMeasurementFailed()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary // Red background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Check Heart Rate",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFEB3B)
                )
                Text(
                    text = "& Track Health Data",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Main Card
            Card(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top avatar/camera icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                         // Placeholder for finger instruction
                         Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress text
                    val progressPercent = (state.progress * 100).toInt()
                    val statusText = when (state.state) {
                        SensorState.INITIALIZING -> stringResource(Res.string.camera_initializing)
                        SensorState.NO_FINGER -> stringResource(Res.string.camera_no_finger)
                        SensorState.MEASURING -> "Measuring...($progressPercent%)"
                        SensorState.COMPLETED -> stringResource(Res.string.camera_completed)
                        SensorState.FAILED -> stringResource(Res.string.camera_failed)
                        SensorState.ERROR -> stringResource(Res.string.camera_error, state.errorMessage ?: "")
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B3A5A) // Dark blueish gray
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Pulsing Heart
                    PulsingHeart(bpm = state.bpm, isMeasuring = state.state == SensorState.MEASURING)
                }
            }
        }
    }
}

@Composable
fun PulsingHeart(bpm: Int, isMeasuring: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isMeasuring) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isMeasuring) 0f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart_alpha1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isMeasuring) 0f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "heart_alpha2"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(250.dp)
    ) {
        // Outer pulse rings
        if (isMeasuring) {
            Box(modifier = Modifier.fillMaxSize().scale(2f - alpha2).clip(CircleShape).background(Color(0xFFFFCDD2).copy(alpha = alpha2)))
            Box(modifier = Modifier.fillMaxSize().scale(1.5f - alpha1).clip(CircleShape).background(Color(0xFFFFCDD2).copy(alpha = alpha1)))
        }

        // Main Heart
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Heart",
            tint = Color(0xFFFF4081),
            modifier = Modifier.fillMaxSize().scale(scale)
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (bpm > 0) bpm.toString() else "--",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "bpm",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
