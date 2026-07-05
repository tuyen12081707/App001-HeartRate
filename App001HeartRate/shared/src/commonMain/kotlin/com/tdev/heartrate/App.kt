package com.tdev.heartrate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.tdev.heartrate.shared.presentation.theme.AppTheme
import com.tdev.heartrate.shared.presentation.components.CustomBottomBar
import com.tdev.heartrate.shared.presentation.components.BottomBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.tab_dashboard
import app001heartrate.shared.generated.resources.tab_history
import org.jetbrains.compose.resources.stringResource
import com.tdev.heartrate.shared.presentation.add.AddRecordScreen
import com.tdev.heartrate.shared.presentation.add.AddRecordViewModel
import com.tdev.heartrate.shared.presentation.dashboard.DashboardScreen
import com.tdev.heartrate.shared.presentation.dashboard.DashboardViewModel
import com.tdev.heartrate.shared.presentation.history.HistoryScreen
import com.tdev.heartrate.shared.presentation.history.HistoryViewModel
import com.tdev.heartrate.shared.presentation.disclaimer.DisclaimerScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.KoinApplication
import com.tdev.heartrate.shared.di.dataModule
import com.tdev.heartrate.shared.di.domainModule
import com.tdev.heartrate.shared.di.platformModule
import com.tdev.heartrate.shared.di.presentationModule

import com.tdev.heartrate.shared.presentation.camera.CameraMeasurementScreen
import com.tdev.heartrate.shared.presentation.camera.FailedScanScreen
import org.koin.core.module.Module
import org.koin.dsl.module

import androidx.compose.material.icons.filled.Person
import com.tdev.heartrate.shared.presentation.profile.ProfileScreen

import com.tdev.heartrate.shared.presentation.result.ResultScreen

enum class Screen {
    DISCLAIMER, DASHBOARD, HISTORY, ADD_RECORD, CAMERA_MEASUREMENT, PROFILE, RESULT, FAILED_SCAN
}

@Composable
fun App(appModule: Module = module { }) {
    KoinApplication(application = {
        modules(
            appModule,
            platformModule,
            domainModule,
            dataModule,
            presentationModule
        )
    }) {
        AppTheme {
            var currentScreen by remember { mutableStateOf(Screen.DISCLAIMER) }
            var prefilledBpm by remember { mutableStateOf<String?>(null) }
            var lastSavedBpm by remember { mutableStateOf(0) }
            var lastSavedBodyState by remember { mutableStateOf("") }

            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    bottomBar = {
                        if (currentScreen != Screen.DISCLAIMER && currentScreen != Screen.CAMERA_MEASUREMENT && currentScreen != Screen.ADD_RECORD && currentScreen != Screen.RESULT && currentScreen != Screen.FAILED_SCAN) {
                            CustomBottomBar(
                                items = listOf(
                                    BottomBarItem(
                                        title = stringResource(Res.string.tab_dashboard),
                                        icon = Icons.Default.Home,
                                        isSelected = currentScreen == Screen.DASHBOARD,
                                        onClick = { currentScreen = Screen.DASHBOARD }
                                    ),
                                    BottomBarItem(
                                        title = stringResource(Res.string.tab_history),
                                        icon = Icons.AutoMirrored.Filled.List,
                                        isSelected = currentScreen == Screen.HISTORY,
                                        onClick = { currentScreen = Screen.HISTORY }
                                    ),
                                    BottomBarItem(
                                        title = "Profile",
                                        icon = Icons.Default.Person,
                                        isSelected = currentScreen == Screen.PROFILE,
                                        onClick = { currentScreen = Screen.PROFILE }
                                    )
                                )
                            )
                        }
                    },
                    floatingActionButton = {
                        if (currentScreen == Screen.DASHBOARD || currentScreen == Screen.HISTORY) {
                            FloatingActionButton(
                                onClick = { currentScreen = Screen.ADD_RECORD }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Record")
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.DISCLAIMER -> {
                                DisclaimerScreen(
                                    onAgree = { currentScreen = Screen.DASHBOARD }
                                )
                            }
                            Screen.DASHBOARD -> {
                                val viewModel = koinViewModel<DashboardViewModel>()
                                DashboardScreen(viewModel = viewModel)
                            }
                            Screen.HISTORY -> {
                                val viewModel = koinViewModel<HistoryViewModel>()
                                HistoryScreen(viewModel = viewModel)
                            }
                            Screen.ADD_RECORD -> {
                                val viewModel = koinViewModel<AddRecordViewModel>()
                                // If we have prefilled BPM, update the viewmodel ONCE.
                                androidx.compose.runtime.LaunchedEffect(prefilledBpm) {
                                    prefilledBpm?.let {
                                        viewModel.onIntent(com.tdev.heartrate.shared.presentation.add.AddRecordIntent.UpdateBpm(it))
                                        prefilledBpm = null // consume
                                    }
                                }
                                // Listen to save result side effect
                                androidx.compose.runtime.LaunchedEffect(viewModel.sideEffect) {
                                    viewModel.sideEffect.collect { effect ->
                                        if (effect is com.tdev.heartrate.shared.presentation.add.AddRecordSideEffect.NavigateToResult) {
                                            lastSavedBpm = effect.bpm
                                            lastSavedBodyState = viewModel.uiState.value.bodyState.name
                                                .lowercase()
                                                .replaceFirstChar { it.uppercase() }
                                            currentScreen = Screen.RESULT
                                        }
                                    }
                                }
                                AddRecordScreen(
                                    viewModel = viewModel,
                                    onNavigateBack = { currentScreen = Screen.DASHBOARD },
                                    onOpenCamera = { currentScreen = Screen.CAMERA_MEASUREMENT }
                                )
                            }
                            Screen.CAMERA_MEASUREMENT -> {
                                CameraMeasurementScreen(
                                    onNavigateBack = { currentScreen = Screen.ADD_RECORD },
                                    onMeasurementCompleted = { bpm ->
                                        prefilledBpm = bpm.toString()
                                        currentScreen = Screen.ADD_RECORD
                                    },
                                    onMeasurementFailed = { currentScreen = Screen.FAILED_SCAN }
                                )
                            }
                            Screen.PROFILE -> {
                                ProfileScreen()
                            }
                            Screen.RESULT -> {
                                ResultScreen(
                                    bpm = lastSavedBpm,
                                    bodyState = lastSavedBodyState,
                                    onGoHome = { currentScreen = Screen.DASHBOARD },
                                    onMeasureAgain = { currentScreen = Screen.ADD_RECORD }
                                )
                            }
                            Screen.FAILED_SCAN -> {
                                FailedScanScreen(
                                    onTryAgain = { currentScreen = Screen.CAMERA_MEASUREMENT },
                                    onGoHome = { currentScreen = Screen.DASHBOARD }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}