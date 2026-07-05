package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.News
import com.tdev.heartrate.shared.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class GetNewsUseCase(
    private val repository: NewsRepository
) {
    operator fun invoke(): Flow<Result<List<News>>> {
        return repository.getHealthNews()
    }
}
