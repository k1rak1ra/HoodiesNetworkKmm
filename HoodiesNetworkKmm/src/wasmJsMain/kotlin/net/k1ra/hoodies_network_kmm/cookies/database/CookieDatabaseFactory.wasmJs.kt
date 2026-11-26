package net.k1ra.hoodies_network_kmm.cookies.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import com.eygraber.sqldelight.androidx.driver.opfs.OpfsMultiTabMode
import com.eygraber.sqldelight.androidx.driver.opfs.opfsWorker

internal actual fun createCookieDatabaseBuilder(): RoomDatabase.Builder<CookieDatabase> {
    return Room.databaseBuilder<CookieDatabase>("HoodiesNetworkCookies.db") {
        CookieDatabaseBuilder.initialize()
    }.setDriver(WebWorkerSQLiteDriver(opfsWorker(multiTabMode = OpfsMultiTabMode.Shared).worker)).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
}