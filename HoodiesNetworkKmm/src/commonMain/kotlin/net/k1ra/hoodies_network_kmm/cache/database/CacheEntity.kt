package net.k1ra.hoodies_network_kmm.cache.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class CacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val bodyHash: Long,
    val cachedAt: Long,
    val data: ByteArray,
    val iv: ByteArray?
)