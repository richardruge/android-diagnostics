package com.creative.core_system.network

interface NetworkSystemDataSource {
    fun isConnected(): Boolean
    fun getConnectionType(): String
}