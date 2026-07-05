package com.tdev.heartrate.shared.domain.model

data class News(
    val title: String,
    val description: String,
    val urlToImage: String?,
    val url: String,
    val publishedAt: String
)
