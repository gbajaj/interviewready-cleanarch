package com.gauravbajaj.interviewready.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple utility to check network connectivity status.
 *
 * Provides basic methods to check if the device has internet connectivity.
 */
@Singleton
class NetworkConnectivityChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    /**
     * Checks if the device currently has network connectivity.
     *
     * @return true if connected to network, false otherwise
     */
    fun isConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnectedOrConnecting == true
        }
    }

    /**
     * Gets the current network connection type.
     *
     * @return NetworkConnectionType indicating the type of connection
     */
    fun getConnectionType(): NetworkConnectionType {
        if (!isConnected()) {
            return NetworkConnectionType.NONE
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkConnectionType.NONE
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
                ?: return NetworkConnectionType.NONE

            when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                    NetworkConnectionType.WIFI
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                    NetworkConnectionType.CELLULAR
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                    NetworkConnectionType.ETHERNET
                else -> NetworkConnectionType.OTHER
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkConnectionType.WIFI
                ConnectivityManager.TYPE_MOBILE -> NetworkConnectionType.CELLULAR
                ConnectivityManager.TYPE_ETHERNET -> NetworkConnectionType.ETHERNET
                else -> NetworkConnectionType.OTHER
            }
        }
    }

    /**
     * Gets a user-friendly description of the current connection status.
     *
     * @return String describing the connection status
     */
    fun getConnectionStatusDescription(): String {
        return when {
            !isConnected() -> "No internet connection"
            getConnectionType() == NetworkConnectionType.WIFI -> "Connected via WiFi"
            getConnectionType() == NetworkConnectionType.CELLULAR -> "Connected via mobile data"
            else -> "Connected to internet"
        }
    }
}

/**
 * Enum representing different types of network connections.
 */
enum class NetworkConnectionType {
    NONE,       // No connection
    WIFI,       // WiFi connection
    CELLULAR,   // Cellular/mobile data
    ETHERNET,   // Ethernet connection
    OTHER       // Other type of connection
}