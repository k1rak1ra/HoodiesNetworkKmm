package net.k1ra.hoodies_network_kmm.cookies.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

internal actual fun createCookieDatabaseBuilder(): RoomDatabase.Builder<CookieDatabase> {
    return Room.databaseBuilder<CookieDatabase>(File(System.getProperty("java.io.tmpdir"), "HoodiesNetworkCookies.db").absolutePath) {
        CookieDatabaseBuilder.initialize()
    }.setDriver(BundledSQLiteDriver())
}