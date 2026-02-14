package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.config.web.ConfigSpecUi
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.web.DeleteModalUi
import com.zalphion.featurecontrol.web.getControlled
import com.zalphion.featurecontrol.web.waitForReady

private val urlRegex = ".*applications/([^/]+)/features/([^/]+).*".toRegex()

class FeatureUi(private val page: Page) {

    init {
        page.waitForReady()
        PlaywrightAssertions.assertThat(page).hasURL(urlRegex.toPattern())
    }

    private val main get() = page.getByRole(AriaRole.MAIN)

    val appId = AppId.parse(urlRegex.find(page.url())!!.groupValues[1])
    val featureKey = FeatureKey.parse(urlRegex.find(page.url())!!.groupValues[2])

    val applications = page.applicationsList()
    val application = page.application()

    val environments = FeatureNavBarUi(main.getByRole(AriaRole.NAVIGATION))
    val edit = FeatureEditUi(main)

    fun update(block: (FeatureUi) -> Unit = {}): FeatureUi {
        main.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Update")).click()
        return FeatureUi(page).also(block)
    }

    fun reset(block: (FeatureUi) -> Unit = {}): FeatureUi {
        main.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Reset")).click()
        return FeatureUi(page).also(block)
    }
}

class FeatureMenuUi(private val section: Locator) {
    fun delete(block: (DeleteModalUi<FeatureKey, ConfigSpecUi>) -> Unit = {}) = section
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Delete Feature"))
        .also { it.click() }
        .getControlled()
        .let { DeleteModalUi(it, FeatureKey, ::ConfigSpecUi) }
        .also(block)
}