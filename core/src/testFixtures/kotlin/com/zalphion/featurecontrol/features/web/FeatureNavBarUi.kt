package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.web.waitForAll

class FeatureNavBarUi(val locator: Locator) {

    val options get() = locator
        .locator(".uk-subnav")
        .getByRole(AriaRole.LINK)
        .filter(Locator.FilterOptions().setHasNotText("General"))
        .waitForAll()
        .map { EnvironmentName.parse(it.textContent().trim()) }

    val selected: EnvironmentName? get() = locator
        .locator("a[aria-current='page']")
        .first()
        .takeIf { it.count() > 0 }
        ?.let { EnvironmentName.parse(it.textContent().trim()) }

    fun selectGeneral(block: (FeatureUi) -> Unit = {}) = locator
        .getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("General"))
        .also { it.click() }
        .let { FeatureUi(locator.page()) }
        .also(block)

    fun select(environment: EnvironmentName, block: (FeatureEnvironmentUi) -> Unit = {}) = locator
        .getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName(environment.value))
        .also { it.click() }
        .let { FeatureEnvironmentUi(locator.page()) }
        .also(block)

    fun more(block: (FeatureMenuUi) -> Unit = {}) =  locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("More Options"))
        .also { it.click() }
        .let { FeatureMenuUi(locator) }
        .also(block)
}