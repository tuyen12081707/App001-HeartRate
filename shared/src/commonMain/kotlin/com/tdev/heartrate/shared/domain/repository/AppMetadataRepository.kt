package com.tdev.heartrate.shared.domain.repository

interface AppMetadataRepository {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
}
