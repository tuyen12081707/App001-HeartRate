package com.tdev.heartrate.shared.presentation.camera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.camera_enter_manually
import app001heartrate.shared.generated.resources.camera_permission_denied_description
import app001heartrate.shared.generated.resources.camera_permission_denied_title
import app001heartrate.shared.generated.resources.camera_permission_retry
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPermissionDeniedScreen(
    onTryAgain: () -> Unit,
    onEnterManually: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.camera_permission_denied_title)) }) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(Res.string.camera_permission_denied_description))
            Button(onClick = onTryAgain, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.camera_permission_retry))
            }
            OutlinedButton(onClick = onEnterManually, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.camera_enter_manually))
            }
        }
    }
}
