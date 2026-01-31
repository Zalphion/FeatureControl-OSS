package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import java.math.BigDecimal
import kotlin.reflect.KProperty

class NumberLocatorProperty(private val locator: Locator) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = locator.inputValue()
        .trim()
        .takeIf { it.isNotEmpty() }
        ?.toBigDecimal()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: BigDecimal?) {
        locator.fill(value?.toString() ?: "")
    }
}

fun Locator.toNumberProperty() = NumberLocatorProperty(this)