package com.creative.core_system.network

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat
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
    private val context: Context,
) : NetworkSystemDataSource {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    override fun observeNetworkState(): Flow<NetworkState> = callbackFlow {
        val networkCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(network: android.net.Network, capabilities: NetworkCapabilities) {
                    trySend(getNetworkState(capabilities))
                }
                override fun onLost(network: android.net.Network) {
                    trySend(getNetworkState())
                }
            }
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: android.net.Network, capabilities: NetworkCapabilities) {
                    trySend(getNetworkState(capabilities))
                }
                override fun onLost(network: android.net.Network) {
                    trySend(getNetworkState())
                }
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
        return getNetworkState(cm.getNetworkCapabilities(cm.activeNetwork))
    }

    private fun getNetworkState(caps: NetworkCapabilities?): NetworkState {
        val activeNetwork = cm.activeNetwork
        
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasNearbyPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        val isLocationEnabled =
            locationManager?.isLocationEnabled == true

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
            @Suppress("DEPRECATION")
            val connectionInfo = wifiManager.connectionInfo
            val transportWifiInfo = caps?.transportInfo as? WifiInfo
            
            // Prefer connectionInfo for SSID if transportInfo is redacted (common on Android 12+ getNetworkCapabilities)
            val wifiInfo = if (transportWifiInfo != null && 
                transportWifiInfo.ssid != null && 
                transportWifiInfo.ssid != WifiManager.UNKNOWN_SSID && 
                transportWifiInfo.ssid != "<unknown ssid>") {
                transportWifiInfo
            } else {
                connectionInfo
            }

            // Process wifiInfo directly
            signalStrength = wifiInfo?.rssi
            signalLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                wifiManager.calculateSignalLevel(wifiInfo?.rssi ?: 0)
            } else {
                @Suppress("DEPRECATION")
                WifiManager.calculateSignalLevel(wifiInfo?.rssi ?: 0, 5)
            }
            
            val rawSsid = wifiInfo?.ssid
            ssid = when {
                rawSsid == null || rawSsid.isEmpty() || rawSsid.contains("unknown", ignoreCase = true) -> {
                    when {
                        !hasLocationPermission -> "Permission Required"
                        !hasNearbyPermission -> "Nearby Devices Permission"
                        !isLocationEnabled -> "Location (GPS) Off"
                        else -> "Restricted by System"
                    }
                }
                else -> rawSsid.removeSurrounding("\"")
            }
            bssid = wifiInfo?.bssid
            freq = wifiInfo?.frequency

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                wifiStandard = when (wifiInfo?.wifiStandard) {
                    ScanResult.WIFI_STANDARD_11BE -> "Wi-Fi 7"
                    ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6"
                    ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5"
                    ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4"
                    ScanResult.WIFI_STANDARD_LEGACY -> "Legacy"
                    else -> "Unknown"
                }
            }

            linkSpeed = wifiInfo?.linkSpeed

            @Suppress("DEPRECATION")
            val dhcpInfo = wifiManager.dhcpInfo
            gatewayIp = formatIpAddress(dhcpInfo.gateway)
            val dhcpIp = formatIpAddress(dhcpInfo.ipAddress)
            ipAddress = dhcpIp.takeIf { it != "0.0.0.0" }
        }

        activeNetwork?.let { network ->
            val linkProps = cm.getLinkProperties(network)
            dnsServers = linkProps?.dnsServers?.mapNotNull { it.hostAddress } ?: emptyList()
            if (ipAddress == null) {
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

    private fun formatIpAddress(ip: Int): String {
        return try {
            val bytes = byteArrayOf(
                (ip and 0xFF).toByte(),
                (ip shr 8 and 0xFF).toByte(),
                (ip shr 16 and 0xFF).toByte(),
                (ip shr 24 and 0xFF).toByte()
            )
            InetAddress.getByAddress(bytes).hostAddress ?: "0.0.0.0"
        } catch (_: Exception) {
            "0.0.0.0"
        }
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
        } catch (_: Exception) {
            null
        }
    }
}
