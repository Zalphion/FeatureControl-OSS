package com.zalphion.featurecontrol.config.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.web.waitForAll
import io.kotest.matchers.shouldBe

class ConfigNavBarUi(private val locator: Locator) {

    val options get() = locator
        .locator(".uk-subnav")
        .getByRole(AriaRole.LINK)
        .filter(Locator.FilterOptions().setHasNotText("Properties"))
        .waitForAll()
        .map { EnvironmentName.parse(it.textContent().trim()) }

    val selected: EnvironmentName? get() = locator
        .locator("a[aria-current='page']")
        .filter(Locator.FilterOptions().setHasNotText("Properties"))
        .first()
        .takeIf { it.count() > 0 }
        ?.let { EnvironmentName.parse(it.textContent().trim()) }

    fun selectProperties(block: (ConfigSpecPage) -> Unit = {}): ConfigSpecPage {
        locator.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Properties")).click()
        return ConfigSpecPage(locator.page()).also(block)
    }

    fun select(environment: EnvironmentName, block: (ConfigEnvironmentPage) -> Unit = {}): ConfigEnvironmentPage {
        locator.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName(environment.value)).click()
        return ConfigEnvironmentPage(locator.page())
            .also { it.uriEnvironmentName shouldBe environment }
            .also { it.environments.selected shouldBe environment }
            .also(block)
    }
}