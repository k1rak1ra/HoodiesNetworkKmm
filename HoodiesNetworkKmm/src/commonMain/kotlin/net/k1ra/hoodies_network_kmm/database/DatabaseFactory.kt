package net.k1ra.hoodies_network_kmm.database

import net.k1ra.hoodiesnetworkkmm.database.HoodiesNetworkCacheDatabaseQueries

object DatabaseFactory {
    private var instance: HoodiesNetworkCacheDatabaseQueries? = null

    suspend fun provideCacheDatabase() : HoodiesNetworkCacheDatabaseQueries {
        if (instance == null)
            instance = HoodiesNetworkCacheDatabaseQueries(DriverFactory().createDriver())

        return instance as HoodiesNetworkCacheDatabaseQueries
    }
}