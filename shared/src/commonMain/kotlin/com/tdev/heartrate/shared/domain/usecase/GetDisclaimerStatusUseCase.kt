package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.repository.AppMetadataRepository

class GetDisclaimerStatusUseCase(
    private val metadataRepository: AppMetadataRepository
) {
    suspend operator fun invoke(): Boolean =
        metadataRepository.get(DISCLAIMER_ACCEPTED_KEY) == ACCEPTED_VALUE

    private companion object {
        const val DISCLAIMER_ACCEPTED_KEY = "disclaimer_accepted"
        const val ACCEPTED_VALUE = "true"
    }
}
