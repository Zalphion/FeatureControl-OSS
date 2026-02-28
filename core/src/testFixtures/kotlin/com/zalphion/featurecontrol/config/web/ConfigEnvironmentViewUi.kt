package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.web.getControlled
import com.zalphion.featurecontrol.web.waitForAll
import com.zalphion.featurecontrol.web.waitForReady

class ConfigEnvironmentViewUi(val page: Page) {

    init {
        page.waitForReady()
        assertThat(page).hasURL(".*/applications/([^/]+)/config/([^/]+).*".toRegex().toPattern())
    }

    val applications get() = page.applicationsList()
    val application get() = page.application()
    val environments = ConfigNavBarUi(page.getByRole(AriaRole.MAIN).getByRole(AriaRole.NAVIGATION))

    val values get() = page
        .getByRole(AriaRole.MAIN)
        .locator("tbody tr:visible")
        .waitForAll()
        .map {
            val key = it
                .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Key"))
                .textContent().trim()

            val value = it
                .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Value"))
                .textContent().trim()

            PropertyKey.parse(key) to value
        }

    fun update(block: (ConfigEnvironmentEditUi) -> Unit = {}) = page
        .getByRole(AriaRole.MAIN)
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Update"))
        .also { it.click() }
        .getControlled()
        .let(::ConfigEnvironmentEditUi)
        .also(block)
}