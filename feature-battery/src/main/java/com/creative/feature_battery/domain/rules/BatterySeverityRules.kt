package com.creative.feature_battery.domain.rules

import com.creative.feature_battery.domain.model.BatterySeverityRule
import com.creative.feature_battery.domain.model.*

val BatterySeverityRules: List<BatterySeverityRule> = listOf(
    // Temperature rules (°C)
    BatterySeverityRule(
        metric = BatteryMetric.TEMPERATURE,
        range = 45f..100f,
        severity = Severity.HIGH
    ),
    BatterySeverityRule(
        metric = BatteryMetric.TEMPERATURE,
        range = 38f..45f,
        severity = Severity.MEDIUM
    ),
    BatterySeverityRule(
        metric = BatteryMetric.TEMPERATURE,
        range = -50f..38f,
        severity = Severity.NORMAL
    ),

    // Level rules (%)
    BatterySeverityRule(
        metric = BatteryMetric.LEVEL,
        range = 0f..15f,
        severity = Severity.CRITICAL
    ),
    BatterySeverityRule(
        metric = BatteryMetric.LEVEL,
        range = 15f..30f,
        severity = Severity.LOW
    ),
    BatterySeverityRule(
        metric = BatteryMetric.LEVEL,
        range = 30f..100f,
        severity = Severity.NORMAL
    )
)