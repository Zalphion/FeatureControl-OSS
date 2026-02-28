package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.web.ModalUi
import com.zalphion.featurecontrol.web.waitForAll

class ConfigEnvironmentEditUi(locator: Locator): ModalUi(locator) {
    val values get() = locator
        .locator("tbody tr:visible")
        .waitForAll()
        .map(::ConfigValueUi)

    fun submit(block: (ConfigEnvironmentViewUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Update"))
        .also { it.click() }
        .let { ConfigEnvironmentViewUi(it.page()) }
        .also(block)
}