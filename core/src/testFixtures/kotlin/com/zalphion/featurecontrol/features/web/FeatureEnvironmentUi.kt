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
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.features.Weight
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.web.ModalUi
import com.zalphion.featurecontrol.web.getControlled
import com.zalphion.featurecontrol.web.toInputProperty
import com.zalphion.featurecontrol.web.toListProperty
import com.zalphion.featurecontrol.web.waitForAll
import com.zalphion.featurecontrol.web.waitForReady

private val urlRegex = ".*applications/([^/]+)/features/([^/]+)/environments/([^/]+).*".toRegex()

class FeatureEnvironmentUi(private val page: Page) {

    init {
        page.waitForReady()
        PlaywrightAssertions.assertThat(page).hasURL(urlRegex.toPattern())
    }

    val uriAppId = AppId.parse(urlRegex.find(page.url())!!.groupValues[1])
    val uriFeatureKey = FeatureKey.parse(urlRegex.find(page.url())!!.groupValues[2])
    val uriEnvironment = EnvironmentName.parse(urlRegex.find(page.url())!!.groupValues[3])

    val applications get() = page.applicationsList()
    val application get() = page.application()
    val environments = FeatureNavBarUi(page.getByRole(AriaRole.MAIN).getByRole(AriaRole.NAVIGATION))

    val variants = page
        .getByRole(AriaRole.MAIN)
        .getByRole(AriaRole.TABLE)
        .locator("tbody tr:visible")
        .waitForAll()
        .map { VariantEnvironmentUi(it) }
        .associateBy { it.name }

    fun update(block: (FeatureEnvironmentUi) -> Unit): FeatureEnvironmentUi {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Update")).click()
        return FeatureEnvironmentUi(page).also(block)
    }

    fun reset(block: (FeatureEnvironmentUi) -> Unit): FeatureEnvironmentUi {
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Reset")).click()
        return FeatureEnvironmentUi(page).also(block)
    }
}

class VariantEnvironmentUi(private val locator: Locator) {

    val name get() = locator
        .getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("Variant"))
        .let { Variant.parse(it.textContent().trim()) }

    var weight by locator
        .getByRole(AriaRole.SPINBUTTON, Locator.GetByRoleOptions().setName("Weight"))
        .toInputProperty(Weight)

    fun subjectIdsModal(block: (SubjectIdsModalUi) -> Unit) {
        locator
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Subjects"))
            .also { it.click() }
            .getControlled()
            .let(::SubjectIdsModalUi)
            .use(block)
    }
}

class SubjectIdsModalUi(locator: Locator): ModalUi(locator) {

    var subjectIds by locator
        .toListProperty(SubjectId.toBiDiMapping())
}