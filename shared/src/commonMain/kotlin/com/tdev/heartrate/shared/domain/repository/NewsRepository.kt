package com.tdev.heartrate.shared.domain.repository

import com.tdev.heartrate.shared.domain.model.News
import kotlinx.coroutines.flow.Flow

import com.tdev.heartrate.shared.domain.model.NewsDetail

interface NewsRepository {
    fun getHealthNews(): Flow<Result<List<News>>>
    fun getNewsDetail(url: String): Flow<Result<NewsDetail>>
}
