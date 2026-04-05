package com.creative.core_system.battery

interface BatterySystemDataSource {
    fun getBatteryLevel(): Int
    fun getBatteryHealth(): Int
    fun isCharging(): Boolean
    fun getCapacityMah(): Int?
    fun getCycleCount(): Int?
    fun getStateOfHealth(): Int?
}
