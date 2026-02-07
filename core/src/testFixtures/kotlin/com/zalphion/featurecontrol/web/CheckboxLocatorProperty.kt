package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import kotlin.reflect.KProperty

class CheckboxLocatorProperty(private val locator: Locator): PropertyDelegate<Boolean> {

    override fun getValue(thisRef: Any?, property: KProperty<*>) = locator.isChecked

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean?) {
        locator.isChecked = value ?: false
    }
}

fun Locator.toCheckboxProperty() = CheckboxLocatorProperty(this)