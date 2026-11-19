package net.k1ra.hoodies_network_kmm.cookies.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(entities = [CookieEntity::class], version = 1)
@ConstructedBy(CookieDatabaseBuilder::class)
abstract class CookieDatabase : RoomDatabase() {
    abstract fun getDao(): CookieDao
}

@Suppress("KotlinNoActualForExpect")
expect object CookieDatabaseBuilder: RoomDatabaseConstructor<CookieDatabase> {
    override fun initialize(): CookieDatabase
}