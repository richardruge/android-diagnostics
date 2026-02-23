package com.creative.core_system.battery

interface BatterySystemDataSource {
    fun getBatteryLevel(): Int
    fun getBatteryHealth(): Int
}