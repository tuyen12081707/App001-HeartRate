package com.tdev.heartrate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import app001heartrate.shared.generated.resources.Res
import app001heartrate.shared.generated.resources.tab_dashboard
import app001heartrate.shared.generated.resources.tab_history
import app001heartrate.shared.generated.resources.tab_news
import app001heartrate.shared.generated.resources.tab_settings
import com.tdev.heartrate.shared.di.appConfigModule
import com.tdev.heartrate.shared.di.dataModule
import com.tdev.heartrate.shared.di.domainModule
import com.tdev.heartrate.shared.di.networkModule
import com.tdev.heartrate.shared.di.platformModule
import com.tdev.heartrate.shared.di.presentationModule
import com.tdev.heartrate.shared.domain.model.AppConfig
import com.tdev.heartrate.shared.domain.model.MeasureType
import com.tdev.heartrate.shared.domain.model.StartupData
import com.tdev.heartrate.shared.domain.usecase.AcceptDisclaimerUseCase
import com.tdev.heartrate.shared.presentation.AppStartupCoordinator
import com.tdev.heartrate.shared.presentation.DataState
import com.tdev.heartrate.shared.presentation.add.AddRecordIntent
import com.tdev.heartrate.shared.presentation.add.AddRecordScreen
import com.tdev.heartrate.shared.presentation.add.AddRecordViewModel
import com.tdev.heartrate.shared.presentation.bloodpressure.BloodPressureScreen
import com.tdev.heartrate.shared.presentation.bloodpressure.BloodPressureViewModel
import com.tdev.heartrate.shared.presentation.camera.CameraMeasurementScreen
import com.tdev.heartrate.shared.presentation.camera.CameraPermissionDeniedScreen
import com.tdev.heartrate.shared.presentation.camera.FailedScanScreen
import com.tdev.heartrate.shared.presentation.components.BottomBarItem
import com.tdev.heartrate.shared.presentation.components.CustomBottomBar
import com.tdev.heartrate.shared.presentation.dashboard.DashboardScreen
import com.tdev.heartrate.shared.presentation.dashboard.DashboardViewModel
import com.tdev.heartrate.shared.presentation.disclaimer.DisclaimerScreen
import com.tdev.heartrate.shared.presentation.history.HistoryScreen
import com.tdev.heartrate.shared.presentation.history.HistoryViewModel
import com.tdev.heartrate.shared.presentation.home.HomeScreen
import com.tdev.heartrate.shared.presentation.navigation.AppNavigator
import com.tdev.heartrate.shared.presentation.navigation.AppRoute
import com.tdev.heartrate.shared.presentation.navigation.MainTab
import com.tdev.heartrate.shared.presentation.navigation.resultViewModelKey
import com.tdev.heartrate.shared.presentation.profile.ProfileScreen
import com.tdev.heartrate.shared.presentation.result.ResultViewModel
import com.tdev.heartrate.shared.presentation.result.ResultScreen
import com.tdev.heartrate.shared.presentation.theme.AppTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

typealias CameraPermissionRequester = ((onGranted: () -> Unit, onDenied: () -> Unit) -> Unit)

@Composable
fun App(
    appConfig: AppConfig = AppConfig(demoDataEnabled = false),
    appModule: Module = module { },
    cameraPermissionRequester: CameraPermissionRequester? = null,
    onStartupState: (DataState<StartupData>) -> Unit = {}
) {
    KoinApplication(application = {
        modules(appModule, platformModule, domainModule, dataModule, presentationModule, networkModule, appConfigModule(appConfig))
    }) {
        val startupCoordinator = koinInject<AppStartupCoordinator>()
        val acceptDisclaimer = koinInject<AcceptDisclaimerUseCase>()
        var startupState by remember { mutableStateOf<DataState<StartupData>>(DataState.Idle) }
        val navigator = remember { AppNavigator(AppRoute.Disclaimer) }
        val scope = rememberCoroutineScope()
        val route by navigator.route.collectAsState()

        LaunchedEffect(startupCoordinator) {
            startupCoordinator.start().collect { state ->
                startupState = state
                onStartupState(state)
                val startup = (state as? DataState.Success)?.data
                if (startup?.consentAccepted == true) navigator.navigate(AppRoute.Main(MainTab.Dashboard))
            }
        }

        AppTheme {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val mainTab = (route as? AppRoute.Main)?.tab
                val hideBottomBar = route !is AppRoute.Main
                Scaffold(
                    bottomBar = {
                        if (!hideBottomBar) {
                            CustomBottomBar(
                                items = listOf(
                                    BottomBarItem(stringResource(Res.string.tab_dashboard), Icons.Default.Home, mainTab == MainTab.Dashboard) { navigator.navigate(AppRoute.Main(MainTab.Dashboard)) },
                                    BottomBarItem(stringResource(Res.string.tab_history), Icons.AutoMirrored.Filled.List, mainTab == MainTab.History) { navigator.navigate(AppRoute.Main(MainTab.History)) },
                                    BottomBarItem(stringResource(Res.string.tab_news), Icons.Default.Info, mainTab == MainTab.News) { navigator.navigate(AppRoute.Main(MainTab.News)) },
                                    BottomBarItem(stringResource(Res.string.tab_settings), Icons.Default.Person, mainTab == MainTab.Profile) { navigator.navigate(AppRoute.Main(MainTab.Profile)) }
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        when (val currentRoute = route) {
                            AppRoute.Disclaimer -> DisclaimerScreen(onAgree = {
                                scope.launch {
                                    acceptDisclaimer()
                                    navigator.navigate(AppRoute.Main(MainTab.Dashboard))
                                }
                            })
                            is AppRoute.Main -> when (currentRoute.tab) {
                                MainTab.Dashboard -> DashboardScreen(koinViewModel<DashboardViewModel>(), { navigator.navigate(AppRoute.AddHeartRate()) }, { navigator.navigate(AppRoute.Main(it)) })
                                MainTab.History -> HistoryScreen(koinViewModel<HistoryViewModel>(), { navigator.navigate(AppRoute.Main(it)) })
                                MainTab.News -> HomeScreen(onNavigateToAddRecord = { navigator.navigate(AppRoute.AddHeartRate()) }, onNavigateToNewsDetail = { navigator.navigate(AppRoute.NewsDetail(it)) })
                                MainTab.Profile -> ProfileScreen()
                            }
                            is AppRoute.AddHeartRate -> {
                                val viewModel = koinViewModel<AddRecordViewModel>()
                                LaunchedEffect(viewModel, currentRoute) {
                                    viewModel.onIntent(
                                        AddRecordIntent.ResetForNewEntry(
                                            prefilledBpm = currentRoute.prefilledBpm,
                                            measureType = currentRoute.measureType
                                        )
                                    )
                                }
                                val openCamera: (() -> Unit)? = cameraPermissionRequester?.let { requester ->
                                    {
                                        requester(
                                            { navigator.navigate(AppRoute.CameraMeasurement) },
                                            { navigator.navigate(AppRoute.CameraPermissionDenied) }
                                        )
                                    }
                                }
                                AddRecordScreen(
                                    viewModel = viewModel,
                                    onBack = { navigator.back() },
                                    onSaved = { recordId -> navigator.navigate(AppRoute.Result(recordId)) },
                                    onOpenCamera = openCamera
                                )
                            }
                            is AppRoute.Result -> {
                                val viewModel = koinViewModel<ResultViewModel>(key = currentRoute.resultViewModelKey(), parameters = { parametersOf(currentRoute.recordId) })
                                ResultScreen(viewModel, { navigator.navigate(AppRoute.Main(MainTab.Dashboard)) }, { navigator.navigate(AppRoute.AddHeartRate()) })
                            }
                            AppRoute.BloodPressure -> BloodPressureScreen(koinViewModel<BloodPressureViewModel>(), { navigator.back() })
                            AppRoute.CameraMeasurement -> CameraMeasurementScreen(
                                onNavigateBack = { navigator.back() },
                                onMeasurementCompleted = { bpm ->
                                    navigator.navigate(AppRoute.AddHeartRate(bpm, MeasureType.CAMERA_SENSOR))
                                },
                                onMeasurementFailed = { navigator.navigate(AppRoute.FailedScan) }
                            )
                            AppRoute.CameraPermissionDenied -> CameraPermissionDeniedScreen(
                                onTryAgain = {
                                    cameraPermissionRequester?.invoke(
                                        { navigator.navigate(AppRoute.CameraMeasurement) },
                                        { /* remain on the denial screen */ }
                                    )
                                },
                                onEnterManually = { navigator.navigate(AppRoute.AddHeartRate()) }
                            )
                            AppRoute.FailedScan -> FailedScanScreen(
                                onTryAgain = {
                                    cameraPermissionRequester?.invoke(
                                        { navigator.navigate(AppRoute.CameraMeasurement) },
                                        { /* remain on the failure screen */ }
                                    )
                                },
                                onEnterManually = { navigator.navigate(AppRoute.AddHeartRate()) }
                            )
                            is AppRoute.NewsDetail -> com.tdev.heartrate.shared.presentation.newsdetail.NewsDetailScreen(currentRoute.url) { navigator.back() }
                        }
                    }
                }
            }
        }
    }
}
