package net.k1ra.hoodies_network_kmm

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Hack from https://firebase.blog/posts/2016/12/how-does-firebase-initialize-on-android to get Context automatically
 */
class HoodiesNetworkInitContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        HoodiesNetworkInit.withAppContext(context!!)
        return true
    }

    override fun query(
        p0: Uri,
        p1: Array<out String>?,
        p2: String?,
        p3: Array<out String>?,
        p4: String?
    ): Cursor? {
        return null
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        return 0
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        return 0
    }
}