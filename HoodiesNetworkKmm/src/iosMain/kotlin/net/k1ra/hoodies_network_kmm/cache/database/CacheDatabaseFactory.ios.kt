package net.k1ra.hoodies_network_kmm.cache.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import net.k1ra.hoodies_network_kmm.cookies.database.databaseDirectory

internal actual fun createCacheDatabaseBuilder(): RoomDatabase.Builder<CacheDatabase> {
    return Room.databaseBuilder<CacheDatabase>("${databaseDirectory()}/HoodiesNetworkCache.db") {
        CacheDatabaseBuilder.initialize()
    }.setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
}