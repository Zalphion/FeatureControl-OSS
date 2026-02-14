package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.web.waitForAll
import com.zalphion.featurecontrol.web.waitForReady

private val urlRegex = ".*/applications/([^/]+)/config.*".toRegex()

class ConfigSpecUi(private val page: Page) {

    init {
        page.waitForReady()
        assertThat(page).hasURL(urlRegex.toPattern())
    }

    val uriAppId get() = AppId.parse(urlRegex.find(page.url())!!.groupValues[1])

    val applications get() = page.applicationsList()
    val application get() = page.application()
    val environments = ConfigNavBarUi(page.getByRole(AriaRole.MAIN).getByRole(AriaRole.NAVIGATION))

    val properties get() = page
        .getByRole(AriaRole.MAIN)
        .locator("tbody tr:visible")
        .waitForAll()
        .map { ConfigPropertyUi(it) }

    fun newProperty(block: (ConfigPropertyUi) -> Unit): ConfigPropertyUi {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add")).click()
        return properties.last().also(block)
    }

    fun update(block: (ConfigSpecUi) -> Unit = {}): ConfigSpecUi {
        page.getByRole(AriaRole.MAIN)
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Update"))
            .click()

        return ConfigSpecUi(page).also(block)
    }

    fun reset(block: (ConfigSpecUi) -> Unit = {}): ConfigSpecUi {
        page.getByRole(AriaRole.MAIN)
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Reset"))
            .click()

        return ConfigSpecUi(page).also(block)
    }
}
