package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.web.waitForAll

class FeatureNavComponent(private val locator: Locator, private val featureKey: FeatureKey) {

    init {
        PlaywrightAssertions.assertThat(locator).isVisible()
    }

    val environments get() = locator
        .locator("uk-subnav")
        .getByRole(AriaRole.LINK)
        .filter(Locator.FilterOptions().setHasNotText("General"))
        .waitForAll()
        .map { EnvironmentName.parse(it.innerText().trim()) }

    fun selectGeneral(block: (FeaturePageUi) -> Unit = {}): FeaturePageUi {
        locator.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("General")).click()
        return FeaturePageUi(locator.page()).also(block)
    }

    fun select(environment: EnvironmentName, block: (FeatureEnvironmentPage) -> Unit = {}): FeatureEnvironmentPage {
        locator.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName(environment.value)).click()
        return FeatureEnvironmentPage(locator.page()).also(block)
    }

    fun more(block: (FeatureMenuUi) -> Unit = {}): FeatureMenuUi {
        locator.getElement(AriaRole.BUTTON, "More Options").click()
        return FeatureMenuUi(locator, featureKey).also(block)
    }
}