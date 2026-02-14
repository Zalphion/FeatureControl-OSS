package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.config.web.ConfigSpecUi
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.web.FeatureCreateUI
import com.zalphion.featurecontrol.features.web.FeatureUi
import com.zalphion.featurecontrol.web.getControlled
import com.zalphion.featurecontrol.web.waitForAll
import io.kotest.matchers.nulls.shouldNotBeNull

class ApplicationUi(private val section: Locator) {

    val name get() = section.getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setLevel(2))
        .let { AppName.parse(it.textContent().trim()) }

    fun newFeature(block: (FeatureCreateUI) -> Unit) = section
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("New Feature"))
        .also { it.click() }
        .getControlled()
        .let(::FeatureCreateUI)
        .also(block)

    fun select(featureKey: FeatureKey, block: (FeatureUi) -> Unit = {}): FeatureUi {
        section
            .getByRole(AriaRole.LINK)
            .filter(Locator.FilterOptions().setHasText("Feature"))
            .waitForAll()
            .map { it.getByRole(AriaRole.HEADING) }
            .find { it.textContent().trim() == featureKey.value }
            .shouldNotBeNull()
            .click()

        return FeatureUi(section.page()).also(block)
    }

    fun config(block: (ConfigSpecUi) -> Unit = {}): ConfigSpecUi {
        section
            .getByRole(AriaRole.LINK)
            .filter(Locator.FilterOptions().setHasText("Config"))
            .first()
            .click()

        return ConfigSpecUi(section.page()).also(block)
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

    fun more(block: (ApplicationMenuUi) -> Unit = {}) = section
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("More Options"))
        .also { it.click() }
        .getControlled()
        .let(::ApplicationMenuUi)
        .also(block)
}

fun Page.application() = getByRole(AriaRole.REGION, Page.GetByRoleOptions().setName("Application Details"))
    .let(::ApplicationUi)
