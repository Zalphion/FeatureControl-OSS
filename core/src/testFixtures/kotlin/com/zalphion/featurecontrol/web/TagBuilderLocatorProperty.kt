package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import org.http4k.lens.BiDiMapping
import kotlin.reflect.KProperty

class TagBuilderLocatorProperty<T: Any>(
    private val locator: Locator,
    private val mapping: BiDiMapping<String, T>
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> {
        return locator.getByRole(AriaRole.LISTITEM)
            .waitForAll()
            .map { mapping(it.textContent().trim()) }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>?) {
        // remove existing tags
        locator.getByRole(AriaRole.LIST)
            .getByRole(AriaRole.BUTTON)
            .waitForAll()
            .forEach { it.click() }

        val input = locator.getByRole(AriaRole.TEXTBOX)
        for (tag in value.orEmpty()) {
            input.fill(mapping(tag))
            input.press("Enter")
        }
    }
}

fun <T: Any> Locator.toTagBuilderProperty(mapping: BiDiMapping<String, T>) = TagBuilderLocatorProperty(this, mapping)