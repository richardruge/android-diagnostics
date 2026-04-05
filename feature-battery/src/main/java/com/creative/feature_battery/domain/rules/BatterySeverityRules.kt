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
    ),

    // Cycle Count Rules (Health/Wear)
    BatterySeverityRule(
        metric = BatteryMetric.CYCLE_COUNT,
        range = 0f..300f,
        severity = Severity.NORMAL // Like new
    ),
    BatterySeverityRule(
        metric = BatteryMetric.CYCLE_COUNT,
        range = 300f..500f,
        severity = Severity.LOW // Mild wear
    ),
    BatterySeverityRule(
        metric = BatteryMetric.CYCLE_COUNT,
        range = 500f..800f,
        severity = Severity.MEDIUM // Aging
    ),
    BatterySeverityRule(
        metric = BatteryMetric.CYCLE_COUNT,
        range = 800f..10000f,
        severity = Severity.HIGH // Critical wear / Replacement recommended
    ),

    // State of Health Rules (%)
    BatterySeverityRule(
        metric = BatteryMetric.STATE_OF_HEALTH,
        range = 90f..100f,
        severity = Severity.NORMAL
    ),
    BatterySeverityRule(
        metric = BatteryMetric.STATE_OF_HEALTH,
        range = 80f..90f,
        severity = Severity.LOW
    ),
    BatterySeverityRule(
        metric = BatteryMetric.STATE_OF_HEALTH,
        range = 70f..80f,
        severity = Severity.MEDIUM
    ),
    BatterySeverityRule(
        metric = BatteryMetric.STATE_OF_HEALTH,
        range = 0f..70f,
        severity = Severity.HIGH
    )
)
