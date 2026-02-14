package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.config.web.ConfigSpecUi
import com.zalphion.featurecontrol.web.getModal
import com.zalphion.featurecontrol.web.waitForAll
import io.kotest.matchers.shouldBe

open class ApplicationsListUi(private val section: Locator) {

    fun select(appName: AppName, block: (ConfigSpecUi) -> Unit = {}): ConfigSpecUi {
        section.getElement(AriaRole.LINK, appName.value).click()
        return ConfigSpecUi(section.page())
            .also { it.application.name shouldBe appName }
            .also(block)
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

fun Page.applicationsList(): ApplicationsListUi {
    val section = getByRole(AriaRole.COMPLEMENTARY, Page.GetByRoleOptions().setName("Applications Bar"))
    return ApplicationsListUi(section)
}