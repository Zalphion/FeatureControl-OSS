package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.config.web.ConfigSpecPage
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.web.FeatureCreateUI
import com.zalphion.featurecontrol.features.web.FeaturePage
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.web.getModal
import com.zalphion.featurecontrol.web.waitForAll
import io.kotest.matchers.nulls.shouldNotBeNull

class ApplicationUi(private val section: Locator) {

    val name get() = section.getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setLevel(2))
        .let { AppName.parse(it.textContent().trim()) }

    fun newFeature(block: (FeatureCreateUI) -> Unit): FeatureCreateUI {
        section.getElement(AriaRole.BUTTON, "New Feature").click()
        val modal = section.page().getModal("New Feature")
        return FeatureCreateUI(modal).also(block)
    }

    fun select(featureKey: FeatureKey, block: (FeaturePage) -> Unit = {}): FeaturePage {
        section
            .getByRole(AriaRole.LINK)
            .filter(Locator.FilterOptions().setHasText("Feature"))
            .waitForAll()
            .map { it.getByRole(AriaRole.HEADING) }
            .find { it.textContent().trim() == featureKey.value }
            .shouldNotBeNull()
            .click()

        return FeaturePage(section.page()).also(block)
    }

    fun config(block: (ConfigSpecPage) -> Unit = {}): ConfigSpecPage {
        section
            .getByRole(AriaRole.LINK)
            .filter(Locator.FilterOptions().setHasText("Config"))
            .first()
            .click()

        return ConfigSpecPage(section.page()).also(block)
    }

    val features get() = section
        .getByRole(AriaRole.LINK)
        .filter(Locator.FilterOptions().setHasText("Feature"))
        .waitForAll()
        .map { it.getByRole(AriaRole.HEADING) }
        .map { FeatureKey.parse(it.textContent().trim()) }

    val selectedFeature get() = section
        .locator("a[aria-current=page]")
        .filter(Locator.FilterOptions().setHasText("Feature"))
        .getByRole(AriaRole.HEADING)
        .first()
        .takeIf { it.count() > 0 }
        ?.let { FeatureKey.parse(it.textContent().trim()) }

    fun more(block: (ApplicationMenuUi) -> Unit = {}): ApplicationMenuUi {
        section.getElement(AriaRole.BUTTON, "More Options").click()
        return ApplicationMenuUi(section, name).also(block)
    }
}

fun Page.application() = getByRole(AriaRole.REGION, Page.GetByRoleOptions().setName("Application Details"))
    .let(::ApplicationUi)
