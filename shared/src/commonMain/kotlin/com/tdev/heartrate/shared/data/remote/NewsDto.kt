package com.tdev.heartrate.shared.data.remote

import com.tdev.heartrate.shared.domain.model.News
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponseDto(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsDto>
)

@Serializable
data class NewsDto(
    val title: String? = null,
    val description: String? = null,
    val urlToImage: String? = null,
    val url: String? = null,
    val publishedAt: String? = null
)

fun NewsDto.toDomain(): News {
    return News(
        title = title.orEmpty(),
        description = description.orEmpty(),
        urlToImage = urlToImage,
        url = url.orEmpty(),
        publishedAt = publishedAt.orEmpty()
    )
}

@Serializable
data class NewsDetailDto(
    val url: String? = null,
    val content: String? = null
)

fun NewsDetailDto.toDomain(): com.tdev.heartrate.shared.domain.model.NewsDetail {
    return com.tdev.heartrate.shared.domain.model.NewsDetail(
        url = url.orEmpty(),
        content = content.orEmpty()
    )
}
