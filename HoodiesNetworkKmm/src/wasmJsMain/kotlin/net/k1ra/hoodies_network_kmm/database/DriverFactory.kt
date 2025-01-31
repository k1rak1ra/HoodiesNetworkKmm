package net.k1ra.hoodies_network_kmm.database

import app.cash.sqldelight.async.coroutines.awaitCreate
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver

actual class DriverFactory {
    actual suspend fun createDriver(): SqlDriver {
        val driver = createDefaultWebWorkerDriver()
        HoodiesNetworkDatabase.Schema.awaitCreate(driver)
        return driver
    }
}