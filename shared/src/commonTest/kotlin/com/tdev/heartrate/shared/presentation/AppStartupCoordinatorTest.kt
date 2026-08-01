package com.tdev.heartrate.shared.presentation

import com.tdev.heartrate.shared.domain.model.AppConfig
import com.tdev.heartrate.shared.domain.model.HeartRateRecord
import com.tdev.heartrate.shared.domain.model.StartupData
import com.tdev.heartrate.shared.domain.repository.AppMetadataRepository
import com.tdev.heartrate.shared.domain.repository.DemoSeedRepository
import com.tdev.heartrate.shared.domain.usecase.AcceptDisclaimerUseCase
import com.tdev.heartrate.shared.domain.usecase.GetDisclaimerStatusUseCase
import com.tdev.heartrate.shared.domain.usecase.SeedDemoHeartRateUseCase
import com.tdev.heartrate.shared.domain.utils.Clock
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AppStartupCoordinatorTest {

    @Test
    fun disclaimerDefaultsToFalseAndPersistsAfterAcceptance() = runBlocking {
        val metadata = InMemoryMetadataRepository()
        val status = GetDisclaimerStatusUseCase(metadata)
        val accept = AcceptDisclaimerUseCase(metadata)

        assertEquals(false, status())
        accept()
        assertEquals(true, status())
    }

    @Test
    fun startupDoesNotSeedWhenDemoDataIsDisabled() = runBlocking {
        val metadata = InMemoryMetadataRepository()
        val seed = RecordingDemoSeedRepository()
        val coordinator = coordinator(
            appConfig = AppConfig(demoDataEnabled = false),
            metadata = metadata,
            seedRepository = seed
        )

        val states = coordinator.start().toList()

        assertEquals(
            listOf(
                DataState.Loading,
                DataState.Success(StartupData(consentAccepted = false))
            ),
            states
        )
        assertEquals(0, seed.calls)
    }

    @Test
    fun startupSeedsWhenDemoDataIsEnabled() = runBlocking {
        val metadata = InMemoryMetadataRepository()
        val seed = RecordingDemoSeedRepository()
        val coordinator = coordinator(
            appConfig = AppConfig(demoDataEnabled = true),
            metadata = metadata,
            seedRepository = seed
        )

        val states = coordinator.start().toList()

        assertEquals(DataState.Loading, states.first())
        assertEquals(DataState.Success(StartupData(consentAccepted = false)), states.last())
        assertEquals(1, seed.calls)
    }

    @Test
    fun startupEmitsErrorWhenMetadataFails() = runBlocking {
        val failure = IllegalStateException("database unavailable")
        val metadata = FailingMetadataRepository(failure)
        val coordinator = AppStartupCoordinator(
            appConfig = AppConfig(demoDataEnabled = false),
            getDisclaimerStatus = GetDisclaimerStatusUseCase(metadata),
            seedDemoHeartRate = SeedDemoHeartRateUseCase(
                demoSeedRepository = RecordingDemoSeedRepository(),
                clock = Clock { 0L }
            )
        )

        val states = coordinator.start().toList()

        assertEquals(DataState.Loading, states.first())
        val error = states.last() as DataState.Error
        assertEquals(failure, error.throwable)
        assertEquals(failure.message, error.message)
    }

    private fun coordinator(
        appConfig: AppConfig,
        metadata: AppMetadataRepository,
        seedRepository: RecordingDemoSeedRepository
    ): AppStartupCoordinator = AppStartupCoordinator(
        appConfig = appConfig,
        getDisclaimerStatus = GetDisclaimerStatusUseCase(metadata),
        seedDemoHeartRate = SeedDemoHeartRateUseCase(
            demoSeedRepository = seedRepository,
            clock = Clock { 0L }
        )
    )
}

private class InMemoryMetadataRepository : AppMetadataRepository {
    private val values = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? = values[key]

    override suspend fun put(key: String, value: String) {
        values[key] = value
    }
}

private class FailingMetadataRepository(
    private val failure: Throwable
) : AppMetadataRepository {
    override suspend fun get(key: String): String? = throw failure

    override suspend fun put(key: String, value: String) = throw failure
}

private class RecordingDemoSeedRepository : DemoSeedRepository {
    var calls: Int = 0

    override suspend fun seedIfAbsent(
        markerKey: String,
        markerValue: String,
        records: List<HeartRateRecord>
    ): Boolean {
        calls += 1
        return true
    }
}
