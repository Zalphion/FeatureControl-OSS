package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.web.getElement

private val urlRegex = ".*/applications/([^/]+)/config.*".toRegex()

class ConfigSpecPageUi(private val page: Page) {

    init {
        assertThat(page).hasURL(urlRegex.toPattern())
    }

    val appId get() = AppId.parse(urlRegex.find(page.url())!!.groupValues[1])

    val applications get() = page.applicationsList()
    val application get() = page.application()

    val properties get() = page
        .getByRole(AriaRole.MAIN)
        .locator("tbody tr").all()
        .map { ConfigPropertyUi(it) }

    fun newProperty(block: (ConfigPropertyUi) -> Unit): ConfigPropertyUi {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add Property")).click()
        return properties.last().also(block)
    }

    fun update(block: (ConfigSpecPageUi) -> Unit = {}): ConfigSpecPageUi {
        page.getByRole(AriaRole.MAIN)
            .getElement(AriaRole.BUTTON, "Update")
            .click()
        return ConfigSpecPageUi(page).also(block)
    }
}
