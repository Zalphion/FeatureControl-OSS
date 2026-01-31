package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions
import kotlin.reflect.KProperty

class CheckboxLocatorProperty(private val locator: Locator) {
    init {
        PlaywrightAssertions.assertThat(locator).isVisible()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = locator.isChecked

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        locator.isChecked = value
    }
}

fun Locator.toCheckboxProperty() = CheckboxLocatorProperty(this)