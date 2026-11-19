package net.k1ra.hoodies_network_kmm.cache.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

internal actual fun createCacheDatabaseBuilder(): RoomDatabase.Builder<CacheDatabase> {
    return Room.databaseBuilder<CacheDatabase>(File(System.getProperty("java.io.tmpdir"), "HoodiesNetworkCache.db").absolutePath) {
        CacheDatabaseBuilder.initialize()
    }.setDriver(BundledSQLiteDriver())
}