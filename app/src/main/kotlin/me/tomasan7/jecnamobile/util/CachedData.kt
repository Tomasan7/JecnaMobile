package me.tomasan7.jecnamobile.util

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class CachedData<T>(
    val data: T,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant = Instant.now()
)
