package net.k1ra.hoodies_network_kmm.database

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory() {
    suspend fun createDriver(): SqlDriver
}