package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.web.getElement
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.zalphion.featurecontrol.config.web.ConfigSpecPage
import com.zalphion.featurecontrol.web.waitForAll

class ApplicationCreateUpdateUi private constructor(
    private val modal: Locator,
    private val submitButtonLabel: String
) {

    companion object {
        fun create(modal: Locator) = ApplicationCreateUpdateUi(modal, "Create")
        fun update(modal: Locator) = ApplicationCreateUpdateUi(modal, "Update")
    }

    init {
        assertThat(modal).isVisible()
    }

    fun setName(name: AppName) {
        modal.getByLabel("Name").fill(name.value)
    }

    fun forEnvironment(name: EnvironmentName, block: (ProjectEnvironmentUi) -> Unit = {}): ProjectEnvironmentUi {
        val row = modal.locator("tbody tr")
            .waitForAll()
            // can't use a locator by value because alpine.js doesn't populate the DOM with a value
            .find { it.locator("input[aria-label='Environment']").inputValue() == name.value }
            ?: error("Environment $name not found")

        return ProjectEnvironmentUi(row).also(block)
    }

    fun newEnvironment(block: (ProjectEnvironmentUi) -> Unit = {}): ProjectEnvironmentUi {
        modal.getElement(AriaRole.BUTTON, "Add Environment").click()
        val row = modal.locator("tbody tr").last()
        return ProjectEnvironmentUi(row).also(block)
    }

    fun submit(block: (ConfigSpecPage) -> Unit = {}): ConfigSpecPage {
        modal.getElement(AriaRole.BUTTON, submitButtonLabel).click()
        return ConfigSpecPage(modal.page()).also(block)
    }
}

class ProjectEnvironmentUi(private val row: Locator) {

    init {
        assertThat(row).isVisible()
    }

    fun setName(name: EnvironmentName) {
        row.getElement(AriaRole.TEXTBOX, "Environment").fill(name.value)
    }
    fun setDescription(description: String) {
        row.getElement(AriaRole.TEXTBOX, "Description").fill(description)
    }
    fun setColour(colour: Colour) {
        row.getElement(AriaRole.TEXTBOX, "Colour").fill(colour.value)
    }
}