package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.config.web.ConfigSpecPageUi
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.web.DeleteModalUi
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.web.getModal

private val urlRegex = ".*applications/([^/]+)/features/([^/]+).*".toRegex()

class FeaturePageUi(private val page: Page) {

    init {
        PlaywrightAssertions.assertThat(page).hasURL(urlRegex.toPattern())
    }

    private val main = page.getByRole(AriaRole.MAIN)
        .also { PlaywrightAssertions.assertThat(it).isVisible() }

    val appId = AppId.parse(urlRegex.find(page.url())!!.groupValues[1])
    val featureKey = FeatureKey.parse(urlRegex.find(page.url())!!.groupValues[2])

    val applications = page.applicationsList()
    val application = page.application()

    // TODO navbar
    val featureEdit = FeatureEditUi(main)

    fun update(block: (FeaturePageUi) -> Unit = {}): FeaturePageUi {
        main.getElement(AriaRole.BUTTON, "Update").click()
        return FeaturePageUi(page).also(block)
    }

    fun more(block: (FeatureMenuUi) -> Unit = {}): FeatureMenuUi {
        main.getElement(AriaRole.BUTTON, "More Options").click()
        return FeatureMenuUi(main, featureKey).also(block)
    }
}

class FeatureMenuUi(private val section: Locator, private val key: FeatureKey) {
    fun delete(
        block: (DeleteModalUi<FeatureKey, ConfigSpecPageUi>) -> Unit = {}
    ): DeleteModalUi<FeatureKey, ConfigSpecPageUi> {
        section.getElement(AriaRole.BUTTON, "Delete Feature").click()

        val deleteModal = section.page().getModal("Delete $key")
        return DeleteModalUi(deleteModal, FeatureKey, ::ConfigSpecPageUi).also(block)
    }
}