package net.k1ra.hoodies_network_kmm.cache.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface CacheDao {
    @Insert
    suspend fun insert(item: CacheEntity)

    @Query("SELECT count(*) FROM CacheEntity WHERE (iv = :iv)")
    suspend fun getByIv(iv: ByteArray?): Int

    @Query("SELECT * FROM CacheEntity WHERE (url = :url AND bodyHash = :bodyHash)")
    suspend fun get(url: String, bodyHash: Long): CacheEntity?

    @Query("DELETE FROM CacheEntity WHERE (url = :url AND bodyHash = :bodyHash)")
    suspend fun delete(url: String, bodyHash: Long)
}