package com.creative.core_system.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class NetworkSystemDataSourceImpl(
    context: Context
) : NetworkSystemDataSource {

    private val cm =
        context.getSystemService(ConnectivityManager::class.java)

    override fun isConnected(): Boolean {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun getConnectionType(): String {
        val network = cm.activeNetwork ?: return "none"
        val caps = cm.getNetworkCapabilities(network) ?: return "none"

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            else -> "other"
        }
    }
}