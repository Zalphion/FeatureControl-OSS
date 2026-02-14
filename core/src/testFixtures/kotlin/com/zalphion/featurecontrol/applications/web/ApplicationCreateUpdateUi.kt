package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.web.getElement
import com.zalphion.featurecontrol.config.web.ConfigSpecUi
import com.zalphion.featurecontrol.web.toInputProperty
import com.zalphion.featurecontrol.web.waitForAll

class ApplicationCreateUpdateUi private constructor(
    private val modal: Locator,
    private val submitButtonLabel: String
) {

    companion object {
        fun create(modal: Locator) = ApplicationCreateUpdateUi(modal, "Create")
        fun update(modal: Locator) = ApplicationCreateUpdateUi(modal, "Update")
    }

    var name by modal
        .getByLabel("Name")
        .toInputProperty(AppName)

    fun forEnvironment(name: EnvironmentName, block: (ApplicationEnvironmentUi) -> Unit = {}): ApplicationEnvironmentUi {
        val row = modal.locator("tbody tr:visible")
            .waitForAll()
            // can't use a locator by value because alpine.js doesn't populate the DOM with a value
            .find { it.locator("input[aria-label='Environment']").inputValue() == name.value }
            ?: error("Environment $name not found")

        return ApplicationEnvironmentUi(row).also(block)
    }

    fun newEnvironment(block: (ApplicationEnvironmentUi) -> Unit = {}): ApplicationEnvironmentUi {
        modal.getElement(AriaRole.BUTTON, "Add").click()
        val row = modal.locator("tbody tr:visible").last()
        return ApplicationEnvironmentUi(row).also(block)
    }

    fun submit(block: (ConfigSpecUi) -> Unit = {}): ConfigSpecUi {
        modal.getElement(AriaRole.BUTTON, submitButtonLabel).click()
        return ConfigSpecUi(modal.page()).also(block)
    }
}

class ApplicationEnvironmentUi(val locator: Locator) {

    var name by locator
        .getElement(AriaRole.TEXTBOX, "Environment")
        .toInputProperty(EnvironmentName)

    var description by locator
        .getElement(AriaRole.TEXTBOX, "Description")
        .toInputProperty()

    var colour by locator
        .getElement(AriaRole.TEXTBOX, "Colour")
        .toInputProperty(Colour)
}