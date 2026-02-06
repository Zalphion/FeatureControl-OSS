package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.config.web.ConfigSpecPage
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.web.DeleteModalUi
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.web.getModal

private val urlRegex = ".*applications/([^/]+)/features/([^/]+).*".toRegex()

class FeaturePage(private val page: Page) {

    init {
        PlaywrightAssertions.assertThat(page).hasURL(urlRegex.toPattern())
    }

    private val main get() = page.getByRole(AriaRole.MAIN)

    val appId = AppId.parse(urlRegex.find(page.url())!!.groupValues[1])
    val featureKey = FeatureKey.parse(urlRegex.find(page.url())!!.groupValues[2])

    val applications = page.applicationsList()
    val application = page.application()

    val environments = FeatureNavBarUi(main.getByRole(AriaRole.NAVIGATION), featureKey)
    val edit = FeatureEditUi(main)

    fun update(block: (FeaturePage) -> Unit = {}): FeaturePage {
        main.getElement(AriaRole.BUTTON, "Update").click()
        return FeaturePage(page).also(block)
    }

    fun reset(block: (FeaturePage) -> Unit = {}): FeaturePage {
        main.getElement(AriaRole.BUTTON, "Reset").click()
        return FeaturePage(page).also(block)
    }
}

class FeatureMenuUi(private val section: Locator, private val key: FeatureKey) {
    fun delete(
        block: (DeleteModalUi<FeatureKey, ConfigSpecPage>) -> Unit = {}
    ): DeleteModalUi<FeatureKey, ConfigSpecPage> {
        section.getElement(AriaRole.BUTTON, "Delete Feature").click()

        val deleteModal = section.page().getModal("Delete $key")
        return DeleteModalUi(deleteModal, FeatureKey, ::ConfigSpecPage).also(block)
    }
}