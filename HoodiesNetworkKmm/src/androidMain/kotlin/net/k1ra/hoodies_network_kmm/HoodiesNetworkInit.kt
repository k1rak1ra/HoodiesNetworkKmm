package net.k1ra.hoodies_network_kmm

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak") //Yes, yes, but this is supposed to be an application context!
internal object HoodiesNetworkInit {
    internal var context: Context? = null

    internal fun withAppContext(appContext: Context) = apply { context = appContext }

    internal fun throwExceptionForMissingContext() {
        throw Exception("Did you forget to call HoodiesNetworkInit.withAppContext() in your Application class")
    }
}