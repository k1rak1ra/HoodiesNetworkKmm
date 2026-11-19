package net.k1ra.hoodies_network_kmm.cookies.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class CookieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val value: String,
)