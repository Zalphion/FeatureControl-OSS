package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.web.waitForAll
import com.zalphion.featurecontrol.web.waitForReady

class ConfigEnvironmentEditUi(
    val page: Page,
    uriRegex: Regex = ".*/teams/([^/]+)/applications/([^/]+)/config/([^/]+)/edit.*".toRegex()
) {

    init {
        page.waitForReady()
        assertThat(page).hasURL(uriRegex.toPattern())
    }

    val applications get() = page.applicationsList()
    val application get() = page.application()
    val environments = ConfigNavBarUi(page.getByRole(AriaRole.MAIN).getByRole(AriaRole.NAVIGATION))

    val values get() = page
        .getByRole(AriaRole.MAIN)
        .locator("tbody tr:visible")
        .waitForAll()
        .map(::ConfigValueUi)

    fun submit(block: (ConfigEnvironmentUi) -> Unit = {}) = page
        .getByRole(AriaRole.MAIN)
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Update"))
        .also { it.click() }
        .let { ConfigEnvironmentUi(it.page()) }
        .also(block)
}