package net.k1ra.hoodies_network_kmm.cache.database

import androidx.room3.RoomDatabase
import net.k1ra.sharedprefkmm.util.IODispatcher


/** Platform builder, including the SQLiteDriver. Must be built (not just constructed) so Room initializes its coroutine scope. */
internal expect fun createCacheDatabaseBuilder(): RoomDatabase.Builder<CacheDatabase>

internal fun buildCacheDatabase(): CacheDatabase =
    createCacheDatabaseBuilder()
        .setQueryCoroutineContext(IODispatcher)
        .build()