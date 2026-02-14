package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.web.waitForAll
import io.kotest.matchers.shouldBe

class FeatureNavBarUi(private val locator: Locator) {

    val options get() = locator
        .locator("uk-subnav")
        .getByRole(AriaRole.LINK)
        .filter(Locator.FilterOptions().setHasNotText("General"))
        .waitForAll()
        .map { EnvironmentName.parse(it.textContent().trim()) }

    val selected: EnvironmentName? get() = locator
        .locator("a[aria-current='page']")
        .first()
        .takeIf { it.count() > 0 }
        ?.let { EnvironmentName.parse(it.textContent().trim()) }

    fun selectGeneral(block: (FeatureUi) -> Unit = {}): FeatureUi {
        locator.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("General")).click()
        return FeatureUi(locator.page()).also(block)
    }

    fun select(environment: EnvironmentName, block: (FeatureEnvironmentUi) -> Unit = {}): FeatureEnvironmentUi {
        locator.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName(environment.value)).click()
        return FeatureEnvironmentUi(locator.page())
            .also { it.uriEnvironment shouldBe environment }
            .also { it.environments.selected shouldBe environment }
            .also(block)
    }

    fun more(block: (FeatureMenuUi) -> Unit = {}): FeatureMenuUi {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("More Options")).click()
        return FeatureMenuUi(locator).also(block)
    }
}