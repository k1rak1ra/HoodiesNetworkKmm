package net.k1ra.hoodies_network_kmm.cookies.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import net.k1ra.sharedprefkmm.SharedPrefKmmInitContentProvider

internal actual fun createCookieDatabaseBuilder(): RoomDatabase.Builder<CookieDatabase> {
    return Room.databaseBuilder<CookieDatabase>(
        context = SharedPrefKmmInitContentProvider.appContext,
        name = "HoodiesNetworkCookies.db"
    ) { CookieDatabaseBuilder.initialize() }.setDriver(BundledSQLiteDriver())
}