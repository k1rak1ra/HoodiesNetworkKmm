package net.k1ra.hoodies_network_kmm.cache.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig
import net.k1ra.sharedprefkmm.SharedPrefKmmInitContentProvider

internal actual fun createCacheDatabaseBuilder(): RoomDatabase.Builder<CacheDatabase> {
    return if (HttpClientConfig.testMode) {
        Room.inMemoryDatabaseBuilder<CacheDatabase> { CacheDatabaseBuilder.initialize() }
            .setDriver(BundledSQLiteDriver())
    } else {
        Room.databaseBuilder<CacheDatabase>(
            context = SharedPrefKmmInitContentProvider.appContext,
            name = "HoodiesNetworkCache.db"
        ) { CacheDatabaseBuilder.initialize() }.setDriver(BundledSQLiteDriver())
    }
}