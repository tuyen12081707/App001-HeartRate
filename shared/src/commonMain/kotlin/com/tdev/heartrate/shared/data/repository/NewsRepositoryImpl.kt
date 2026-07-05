package com.tdev.heartrate.shared.data.repository

import com.tdev.heartrate.shared.data.remote.NewsApiClient
import com.tdev.heartrate.shared.data.remote.toDomain
import com.tdev.heartrate.shared.domain.model.News
import com.tdev.heartrate.shared.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class NewsRepositoryImpl(
    private val apiClient: NewsApiClient
) : NewsRepository {
    override fun getHealthNews(): Flow<Result<List<News>>> = flow {
        val response = apiClient.getHealthNews()
        if (response.status == "ok") {
            val news = response.articles.map { it.toDomain() }.filter { it.title.isNotBlank() }
            emit(Result.success(news))
        } else {
            emit(Result.failure(Exception("Failed to fetch news. Status: ${response.status}")))
        }
    }.catch { e ->
        emit(Result.failure(e))
    }

    override fun getNewsDetail(url: String): Flow<Result<com.tdev.heartrate.shared.domain.model.NewsDetail>> = flow {
        val response = apiClient.getNewsDetail(url)
        emit(Result.success(response.toDomain()))
    }.catch { e ->
        emit(Result.failure(e))
    }
}
