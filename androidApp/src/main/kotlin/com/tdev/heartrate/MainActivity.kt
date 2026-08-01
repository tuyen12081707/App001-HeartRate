package com.tdev.heartrate

import android.Manifest
import android.os.Bundle
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat

import org.koin.dsl.module
import com.tdev.heartrate.shared.domain.model.AppConfig

private data class CameraPermissionCallbacks(
    val onGranted: () -> Unit,
    val onDenied: () -> Unit
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            var pendingCameraPermission by remember { mutableStateOf<CameraPermissionCallbacks?>(null) }
            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                val callbacks = pendingCameraPermission
                pendingCameraPermission = null
                if (granted) callbacks?.onGranted?.invoke() else callbacks?.onDenied?.invoke()
            }

            App(
                appConfig = AppConfig(
                    demoDataEnabled = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
                ),
                appModule = module {
                    single<android.content.Context> { applicationContext }
                },
                cameraPermissionRequester = { onGranted, onDenied ->
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        onGranted()
                    } else {
                        pendingCameraPermission = CameraPermissionCallbacks(onGranted, onDenied)
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
