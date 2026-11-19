package net.k1ra.hoodies_network_kmm.cache.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(entities = [CacheEntity::class], version = 1)
@ConstructedBy(CacheDatabaseBuilder::class)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun getDao(): CacheDao
}

@Suppress("KotlinNoActualForExpect")
expect object CacheDatabaseBuilder: RoomDatabaseConstructor<CacheDatabase> {
    override fun initialize(): CacheDatabase
}