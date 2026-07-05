package com.tdev.heartrate.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class NewsApiClient(private val httpClient: HttpClient) {
    suspend fun getHealthNews(): NewsResponseDto {
        return httpClient.get("https://saurav.tech/NewsAPI/top-headlines/category/health/us.json").body()
    }

    suspend fun getNewsDetail(url: String): NewsDetailDto {
        // Mocking an API call for news detail, wait 1 second
        kotlinx.coroutines.delay(1000)
        return NewsDetailDto(
            url = url,
            content = "This is the full detailed content for the news article fetched from the API. " +
                      "It provides more in-depth information about the health topic discussed. \n\n" +
                      "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                      "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
        )
    }
}
