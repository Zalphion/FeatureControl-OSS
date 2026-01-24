package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.features.FeatureKey

class FeatureCreateUI(private val section: Locator) {

    init {
        PlaywrightAssertions.assertThat(section).isVisible()
    }

    private val _key = section.getByLabel("Key")
    var key: FeatureKey
        get() = FeatureKey.parse(_key.inputValue())
        set(value) { _key.fill(value.value) }

    val edit = FeatureEditUi(section)

    fun submit(block: (FeaturePageUi) -> Unit): FeaturePageUi {
        section.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Create")).click()
        return FeaturePageUi(section.page()).also(block)
    }
}