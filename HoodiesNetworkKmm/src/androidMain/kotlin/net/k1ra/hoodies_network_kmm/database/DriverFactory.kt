package net.k1ra.hoodies_network_kmm.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.k1ra.hoodies_network_kmm.HoodiesNetworkInit
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig

actual class DriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        //If we're in test mode and can't use Context, use in-memory DB instead
        return if (HttpClientConfig.testMode) {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                HoodiesNetworkDatabase.Schema.awaitCreate(this)
            }
        } else {

            HoodiesNetworkInit.context ?: HoodiesNetworkInit.throwExceptionForMissingContext()

            AndroidSqliteDriver(
                HoodiesNetworkDatabase.Schema.synchronous(),
                HoodiesNetworkInit.context!!,
                "HoodiesNetwork.db"
            )
        }
    }
}