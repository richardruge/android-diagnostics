package com.creative.core_system.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import com.creative.core_model.NetworkState
import com.creative.core_model.NetworkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.net.InetAddress

class NetworkSystemDataSourceImpl(
    private val context: Context
) : NetworkSystemDataSource {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    override fun observeNetworkState(): Flow<NetworkState> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: android.net.Network, capabilities: NetworkCapabilities) {
                trySend(getNetworkState())
            }
            override fun onLost(network: android.net.Network) {
                trySend(getNetworkState())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, networkCallback)

        awaitClose {
            cm.unregisterNetworkCallback(networkCallback)
        }
    }.onStart { emit(getNetworkState()) }

    override fun getNetworkState(): NetworkState {
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        val type = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            !isConnected -> NetworkType.NONE
            else -> NetworkType.UNKNOWN
        }

        var signalStrength: Int? = null
        var signalLevel = 0
        var ssid: String? = null
        var freq: Int? = null
        var linkSpeed: Int? = null

        if (type == NetworkType.WIFI) {
            val wifiInfo = wifiManager.connectionInfo
            signalStrength = wifiInfo.rssi
            signalLevel = WifiManager.calculateSignalLevel(wifiInfo.rssi, 5)
            ssid = wifiInfo.ssid.removeSurrounding("\"")
            freq = wifiInfo.frequency
            linkSpeed = wifiInfo.linkSpeed
        } else if (type == NetworkType.CELLULAR) {
            // Signal strength for cellular simplified
        }

        return NetworkState(
            isConnected = isConnected,
            type = type,
            signalStrengthDbm = signalStrength,
            signalLevel = signalLevel,
            ssid = ssid,
            frequencyMhz = freq,
            linkSpeedMbps = linkSpeed,
            ipAddress = null
        )
    }

    override suspend fun runPingTest(host: String): Long? = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val address = InetAddress.getByName(host)
            if (address.isReachable(5000)) {
                System.currentTimeMillis() - startTime
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
