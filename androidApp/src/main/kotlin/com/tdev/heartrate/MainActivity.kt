package com.tdev.heartrate

import android.os.Bundle
import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import org.koin.dsl.module
import com.tdev.heartrate.shared.domain.model.AppConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                appConfig = AppConfig(
                    demoDataEnabled = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
                ),
                appModule = module {
                    single<android.content.Context> { applicationContext }
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
