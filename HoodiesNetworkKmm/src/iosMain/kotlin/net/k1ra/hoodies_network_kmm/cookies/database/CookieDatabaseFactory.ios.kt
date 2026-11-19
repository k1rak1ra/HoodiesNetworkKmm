package net.k1ra.hoodies_network_kmm.cookies.database

import androidx.room3.Room
import androidx.room3.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual fun createCookieDatabaseBuilder(): RoomDatabase.Builder<CookieDatabase> {
    return Room.databaseBuilder<CookieDatabase>("${databaseDirectory()}/HoodiesNetworkCookies.db") {
        CookieDatabaseBuilder.initialize()
    }.setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())
}

@OptIn(ExperimentalForeignApi::class)
fun databaseDirectory(): String {
    // The app's home directory isn't writable on device; Application Support is, but may not exist yet
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null
    )
    return requireNotNull(url?.path) { "Unable to resolve the Application Support directory" }
}