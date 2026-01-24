package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory

class DeleteModalUi<Name: StringValue, UI: Any>(
    private val modal: Locator,
    private val nameFactory: StringValueFactory<Name>,
    private val newPage: (Page) -> UI
) {

    init {
        assertThat(modal).isVisible()
    }

    val name: Name = modal
        .getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setLevel(2))
        .textContent()
        .removePrefix("Delete")
        .removeSuffix("?")
        .trim()
        .let(nameFactory::parse)

    fun confirm(block: (UI) -> Unit = {}): UI {
        modal.getElement(AriaRole.BUTTON, "Delete").click()
        return newPage(modal.page()).also(block)
    }

    fun cancel(block: UI.() -> Unit = {}): UI {
        modal.getElement(AriaRole.BUTTON, "Cancel").click()
        return newPage(modal.page()).also(block)
    }
}