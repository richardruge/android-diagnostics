package com.creative.feature_battery.domain

import com.creative.feature_battery.domain.model.*
import com.creative.feature_battery.domain.rules.BatterySeverityRules

class BatterySeverityEvaluator(
    private val rules: List<BatterySeverityRule> = BatterySeverityRules
) {

    fun evaluate(info: BatteryInfo): Severity {
        val matched = rules.mapNotNull { rule ->
            val value = when (rule.metric) {
                BatteryMetric.TEMPERATURE -> info.temperatureC
                BatteryMetric.LEVEL -> info.level.toFloat()
            }

            if (value in rule.range) rule.severity else null
        }

        return matched.maxByOrNull { it.ordinal } ?: Severity.NORMAL
    }
}