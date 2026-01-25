package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.SubjectId
import com.zalphion.featurecontrol.features.Weight
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.web.toInputProperty
import com.zalphion.featurecontrol.web.toTagBuilderProperty
import com.zalphion.featurecontrol.web.waitForAll

private val urlRegex = ".*applications/([^/]+)/features/([^/]+)/environments/([^/]+).*".toRegex()

class FeatureEnvironmentPage(private val page: Page) {

    init {
        PlaywrightAssertions.assertThat(page).hasURL(urlRegex.toPattern())
    }

    val appId = AppId.parse(urlRegex.find(page.url())!!.groupValues[1])
    val featureKey = FeatureKey.parse(urlRegex.find(page.url())!!.groupValues[2])
    val environment = EnvironmentName.parse(urlRegex.find(page.url())!!.groupValues[3])

    val applications get() = page.applicationsList()
    val application get() = page.application()
    val featureNav = FeatureNavComponent(page.getByRole(AriaRole.MAIN).getByRole(AriaRole.NAVIGATION), featureKey)

    val variants = page
        .getByRole(AriaRole.MAIN)
        .getByRole(AriaRole.TABLE)
        .locator("tbody tr")
        .waitForAll()
        .map { VariantEnvironmentUI(it) }

    fun update(block: (FeatureEnvironmentPage) -> Unit): FeatureEnvironmentPage {
        page.getElement(AriaRole.BUTTON, "Update").click()
        return FeatureEnvironmentPage(page).also(block)
    }

    fun reset(block: (FeatureEnvironmentPage) -> Unit): FeatureEnvironmentPage {
        page.getElement(AriaRole.BUTTON, "Reset").click()
        return FeatureEnvironmentPage(page).also(block)
    }
}

class VariantEnvironmentUI(private val locator: Locator) {

    init {
        PlaywrightAssertions.assertThat(locator).isVisible()
    }

    val variant get() = locator
        .getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("Variant"))
        .textContent()

    val weight by locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Weight"))
        .toInputProperty(Weight.toBiDiMapping())

    val subjectIds = locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Subject Ids"))
        .toTagBuilderProperty(SubjectId.toBiDiMapping())
}