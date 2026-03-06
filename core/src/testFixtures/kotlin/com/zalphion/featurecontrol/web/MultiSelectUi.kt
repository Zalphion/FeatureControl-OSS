package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import org.http4k.lens.BiDiMapping

class MultiSelectUi<T: Any>(
    private val locator: Locator,
    private val mapping: BiDiMapping<String, T>
) {
    private val elements get() = locator.locator("label").waitForAll()

    val options get() = elements.map { mapping(it.textContent().trim()) }

    var selected
        get() = locator
            .locator("label:has(input:checked)")
            .waitForAll()
            .map { mapping(it.textContent().trim()) }
        set(value) {
            for (element in elements) {
                element.isChecked = mapping(element.textContent().trim()) in value
            }
        }
}