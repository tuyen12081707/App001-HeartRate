package com.tdev.heartrate.shared.domain.repository

import com.tdev.heartrate.shared.domain.model.News
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getHealthNews(): Flow<Result<List<News>>>
}
