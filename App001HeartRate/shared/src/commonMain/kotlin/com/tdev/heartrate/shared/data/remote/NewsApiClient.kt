package com.tdev.heartrate.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class NewsApiClient(private val httpClient: HttpClient) {
    suspend fun getHealthNews(): NewsResponseDto {
        return httpClient.get("https://saurav.tech/NewsAPI/top-headlines/category/health/us.json").body()
    }
}
