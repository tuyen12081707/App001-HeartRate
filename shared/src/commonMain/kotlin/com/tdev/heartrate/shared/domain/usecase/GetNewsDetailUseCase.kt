package com.tdev.heartrate.shared.domain.usecase

import com.tdev.heartrate.shared.domain.model.NewsDetail
import com.tdev.heartrate.shared.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class GetNewsDetailUseCase(private val repository: NewsRepository) {
    operator fun invoke(url: String): Flow<Result<NewsDetail>> {
        return repository.getNewsDetail(url)
    }
}
