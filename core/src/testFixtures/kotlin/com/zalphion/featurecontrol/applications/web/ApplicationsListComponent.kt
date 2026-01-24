package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.web.getElement
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.zalphion.featurecontrol.config.web.ConfigSpecPageUi
import com.zalphion.featurecontrol.web.getModal
import com.zalphion.featurecontrol.web.waitForAll

open class ApplicationsListComponent(private val section: Locator) {

    init {
        assertThat(section.getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setLevel(2))).isVisible()
    }

    fun select(appName: AppName, block: (ConfigSpecPageUi) -> Unit = {}): ConfigSpecPageUi {
        section.getElement(AriaRole.LINK, appName.value).click()
        return ConfigSpecPageUi(section.page()).also(block)
    }

    fun new(block: (ApplicationCreateUpdateUi) -> Unit): ApplicationCreateUpdateUi {
        section.getElement(AriaRole.BUTTON, "New Application").click()

        val modal = section.page().getModal("New Application")

        return ApplicationCreateUpdateUi.create(modal).also(block)
    }

    val list get() = section
        .getByRole(AriaRole.LINK)
        .waitForAll()
        .map { it.getByRole(AriaRole.HEADING).textContent() }
        .map { AppName.parse(it.trim()) }

    val selected get() = section
        .locator("a[aria-current=page]")
        .getByRole(AriaRole.HEADING)
        .first()
        .takeIf { it.count() > 0 }
        ?.textContent()?.trim()?.takeIf { it.isNotBlank() }
        ?.let(AppName::parse)
}

fun Page.applicationsList(): ApplicationsListComponent {
    val section = getByRole(AriaRole.COMPLEMENTARY, Page.GetByRoleOptions().setName("Applications Bar"))
    return ApplicationsListComponent(section)
}