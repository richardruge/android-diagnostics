package com.creative.core_model

data class ForegroundSession(
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val avgMa: Double,
    val totalMah: Double
)
