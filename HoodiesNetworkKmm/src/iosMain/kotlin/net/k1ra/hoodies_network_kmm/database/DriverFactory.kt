package net.k1ra.hoodies_network_kmm.database

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        return NativeSqliteDriver(HoodiesNetworkDatabase.Schema.synchronous(), "HoodiesNetwork.db")
    }
}