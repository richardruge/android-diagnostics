package com.creative.core_system.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
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
    context: Context,
) : NetworkSystemDataSource {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

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
        var bssid: String? = null
        var freq: Int? = null
        var wifiStandard: String? = null
        var linkSpeed: Int? = null
        var ipAddress: String? = null
        var gatewayIp: String? = null
        var dnsServers: List<String> = emptyList()

        if (type == NetworkType.WIFI) {
            val wifiInfo = wifiManager.connectionInfo
            signalStrength = wifiInfo.rssi
            signalLevel = WifiManager.calculateSignalLevel(wifiInfo.rssi, 5)
            ssid = wifiInfo.ssid.removeSurrounding("\"")
            bssid = wifiInfo.bssid
            freq = wifiInfo.frequency

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                wifiStandard = when (wifiInfo.wifiStandard) {
                    ScanResult.WIFI_STANDARD_11BE -> "Wi-Fi 7"
                    ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6"
                    ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5"
                    ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4"
                    ScanResult.WIFI_STANDARD_LEGACY -> "Legacy"
                    else -> "Unknown"
                }
            }

            linkSpeed = wifiInfo.linkSpeed

            @Suppress("DEPRECATION")
            val dhcpInfo = wifiManager.dhcpInfo
            gatewayIp = Formatter.formatIpAddress(dhcpInfo.gateway)
            ipAddress = Formatter.formatIpAddress(dhcpInfo.ipAddress)
        }

        activeNetwork?.let { network ->
            val linkProps = cm.getLinkProperties(network)
            dnsServers = linkProps?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()
            if ((ipAddress == null) || (ipAddress == "0.0.0.0")) {
                ipAddress = linkProps?.linkAddresses?.firstOrNull()?.address?.hostAddress
            }
        }

        return NetworkState(
            isConnected = isConnected,
            type = type,
            signalStrengthDbm = signalStrength,
            signalLevel = signalLevel,
            ssid = ssid,
            bssid = bssid,
            frequencyMhz = freq,
            wifiStandard = wifiStandard,
            linkSpeedMbps = linkSpeed,
            ipAddress = ipAddress,
            gatewayIp = gatewayIp,
            dnsServers = dnsServers,
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
        } catch (ignored: Exception) {
            null
        }
    }
}
