package com.creative.feature_battery.domain.model

data class BatterySeverityRule(
    val metric: BatteryMetric,
    val range: ClosedFloatingPointRange<Float>,
    val severity: Severity
)
