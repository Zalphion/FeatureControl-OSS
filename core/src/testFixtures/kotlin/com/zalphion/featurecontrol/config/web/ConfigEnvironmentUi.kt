package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.web.waitForAll
import com.zalphion.featurecontrol.web.waitForReady
import io.kotest.matchers.shouldBe

private val urlRegex = ".*/applications/([^/]+)/config/([^/]+).*".toRegex()

class ConfigEnvironmentUi(private val page: Page) {

    init {
        page.waitForReady()
        assertThat(page).hasURL(urlRegex.toPattern())
    }

    val uriAppId get() = AppId.parse(urlRegex.find(page.url())!!.groupValues[1])
    val uriEnvironmentName get() = EnvironmentName.parse(urlRegex.find(page.url())!!.groupValues[2])

    val applications get() = page.applicationsList()
    val application get() = page.application()
    val environments = ConfigNavBarUi(page.getByRole(AriaRole.MAIN).getByRole(AriaRole.NAVIGATION))

    val values get() = page
        .getByRole(AriaRole.MAIN)
        .locator("tbody tr:visible")
        .waitForAll()
        .map { ConfigValueUi(it) }

    fun update(block: (ConfigEnvironmentUi) -> Unit = {}): ConfigEnvironmentUi {
        page.getByRole(AriaRole.MAIN)
            .getElement(AriaRole.BUTTON, "Update")
            .click()

        return ConfigEnvironmentUi(page)
            .also { it.uriAppId shouldBe uriAppId }
            .also { it.uriEnvironmentName shouldBe uriEnvironmentName }
            .also(block)
    }

    fun reset(block: (ConfigEnvironmentUi) -> Unit = {}): ConfigEnvironmentUi {
        page.getByRole(AriaRole.MAIN)
            .getElement(AriaRole.BUTTON, "Reset")
            .click()

        return ConfigEnvironmentUi(page).also(block)
    }
}