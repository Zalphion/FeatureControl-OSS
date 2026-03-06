package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.web.application
import com.zalphion.featurecontrol.applications.web.applicationsList
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

class FeatureEnvironmentUi(val page: Page) {

    init {
        page.waitForReady()
        PlaywrightAssertions.assertThat(page).hasURL(".*applications/([^/]+)/features/([^/]+)/environments/([^/]+).*".toPattern())
    }

    val applications get() = page.applicationsList()
    val application get() = page.application()
    val environments get() = FeatureNavBarUi(page.getByRole(AriaRole.MAIN).getByRole(AriaRole.NAVIGATION))

    val variants get() = page
        .getByRole(AriaRole.MAIN)
        .getByRole(AriaRole.TABLE)
        .locator("tbody tr:visible")
        .waitForAll()
        .map { VariantEnvironmentUi(it) }

    fun update(block: (FeatureEnvironmentUi) -> Unit) = page
        .getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Update"))
        .also { it.click() }
        .let { FeatureEnvironmentUi(page) }
        .also(block)

    fun reset(block: (FeatureEnvironmentUi) -> Unit) = page
        .getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Reset"))
        .also { it.click() }
        .let { FeatureEnvironmentUi(page) }
        .also(block)
}

class VariantEnvironmentUi(val locator: Locator) {

    val name get() = locator
        .getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setName("Variant"))
        .let { Variant.parse(it.textContent().trim()) }

    var weight by locator
        .getByRole(AriaRole.SPINBUTTON, Locator.GetByRoleOptions().setName("Weight"))
        .toInputProperty(Weight)

    fun subjectIds(block: (SubjectIdsModalUi) -> Unit) {
        locator
            .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Subjects"))
            .also { it.click() }
            .getControlled()
            .let(::SubjectIdsModalUi)
            .use(block)
    }
}

class SubjectIdsModalUi(locator: Locator): ModalUi(locator) {
    var subjectIds by locator.toListProperty(SubjectId.toBiDiMapping())
}