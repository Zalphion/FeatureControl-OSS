package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.SelectOption
import org.http4k.lens.BiDiMapping
import kotlin.reflect.KProperty

class SelectLocatorProperty<T>(
    private val locator: Locator,
    private val mapping: BiDiMapping<String, T>
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return mapping(locator.inputValue())
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        val option = SelectOption().setLabel(value?.let(mapping::invoke) ?: "")
        locator.selectOption(option)
    }
}

fun <T> Locator.toSelectProperty(mapping: BiDiMapping<String, T>) = SelectLocatorProperty(this, mapping)