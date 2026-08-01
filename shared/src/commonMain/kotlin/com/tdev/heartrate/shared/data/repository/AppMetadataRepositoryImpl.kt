package com.tdev.heartrate.shared.data.repository

import com.tdev.heartrate.shared.data.database.HeartRateDatabase
import com.tdev.heartrate.shared.domain.repository.AppMetadataRepository

class AppMetadataRepositoryImpl(
    database: HeartRateDatabase
) : AppMetadataRepository {

    private val queries = database.heartRateDatabaseQueries

    override suspend fun get(key: String): String? =
        queries.getMetadata(key).executeAsOneOrNull()

    override suspend fun put(key: String, value: String) {
        queries.upsertMetadata(key, value)
    }
}
