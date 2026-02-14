package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.config.web.ConfigSpecUi
import com.zalphion.featurecontrol.web.toInputProperty
import com.zalphion.featurecontrol.web.waitForAll

class ApplicationCreateUpdateUi(private val modal: Locator) {

    var name by modal
        .getByLabel("Name")
        .toInputProperty(AppName)

    fun forEnvironment(name: EnvironmentName, block: (ApplicationEnvironmentUi) -> Unit = {}): ApplicationEnvironmentUi {
        val row = modal.locator("tbody tr")
            .waitForAll()
            // can't use a locator by value because alpine.js doesn't populate the DOM with a value
            .find { it.locator("input[aria-label='Environment']").inputValue() == name.value }
            ?: error("Environment $name not found")

        return ApplicationEnvironmentUi(row).also(block)
    }

    fun newEnvironment(block: (ApplicationEnvironmentUi) -> Unit = {}): ApplicationEnvironmentUi {
        modal.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Add")).click()
        val row = modal.locator("tbody tr").last()
        return ApplicationEnvironmentUi(row).also(block)
    }

    fun submit(block: (ConfigSpecUi) -> Unit = {}) = modal
        .locator("button[type=submit]")
        .click()
        .let { ConfigSpecUi(modal.page()) }
        .also(block)
}

class ApplicationEnvironmentUi(val locator: Locator) {

    var name by locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Environment"))
        .toInputProperty(EnvironmentName)

    var description by locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Description"))
        .toInputProperty()

    var colour by locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Colour"))
        .toInputProperty(Colour)
}