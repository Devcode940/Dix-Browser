package com.devcode940.web.baseline

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

/**
 * Baseline Profile Generator (for performance optimization)
 */
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "com.devcode940.web"
        ) {
            // Add critical user journeys here
            startActivityAndWait()
        }
    }
}