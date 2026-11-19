package net.k1ra.hoodies_network_kmm.cookies.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

@Dao
interface CookieDao {
    @Insert
    suspend fun insert(item: CookieEntity)

    @Query("SELECT * FROM CookieEntity")
    suspend fun get(): List<CookieEntity>

    @Query("DELETE FROM CookieEntity WHERE (name = :name)")
    suspend fun delete(name: String)

    @Query("DELETE FROM CookieEntity")
    suspend fun deleteAll()
}