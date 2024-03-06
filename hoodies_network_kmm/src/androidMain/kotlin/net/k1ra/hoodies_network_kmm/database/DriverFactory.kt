package net.k1ra.hoodies_network_kmm.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import net.k1ra.hoodies_network_kmm.HoodiesNetworkInit
import net.k1ra.hoodies_network_kmm.config.HttpClientConfig

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        //If we're in test mode and can't use Context, use in-memory DB instead
        return if (HttpClientConfig.testMode) {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
                HoodiesNetworkDatabase.Schema.create(this)
            }
        } else {

            HoodiesNetworkInit.context ?: HoodiesNetworkInit.throwExceptionForMissingContext()

            AndroidSqliteDriver(
                HoodiesNetworkDatabase.Schema,
                HoodiesNetworkInit.context!!,
                "HoodiesNetwork.db"
            )
        }
    }
}