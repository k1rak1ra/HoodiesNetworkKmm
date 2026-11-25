package net.k1ra.hoodies_network_kmm.cache.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.eygraber.sqldelight.androidx.driver.opfs.opfsWorker

internal actual fun createCacheDatabaseBuilder(): RoomDatabase.Builder<CacheDatabase> {
    return Room.databaseBuilder<CacheDatabase>("HoodiesNetworkCache.db") {
        CacheDatabaseBuilder.initialize()
    }.setDriver(WebWorkerSQLiteDriver(opfsWorker().worker)).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
}