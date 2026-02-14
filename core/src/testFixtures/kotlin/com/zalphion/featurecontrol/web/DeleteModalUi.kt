package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory

class DeleteModalUi<Name: StringValue, NextUi: Any>(
    private val modal: Locator,
    private val nameFactory: StringValueFactory<Name>,
    private val newPage: (Page) -> NextUi
): ModalUi(modal) {

    val name: Name = modal
        .getByRole(AriaRole.HEADING, Locator.GetByRoleOptions().setLevel(2))
        .textContent()
        .removePrefix("Delete")
        .removeSuffix("?")
        .trim()
        .let(nameFactory::parse)

    fun confirm(block: (NextUi) -> Unit = {}): NextUi {
        modal.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Delete")).click()
        return newPage(modal.page()).also(block)
    }

    fun cancel(block: NextUi.() -> Unit = {}): NextUi {
        modal.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Cancel")).click()
        return newPage(modal.page()).also(block)
    }
}