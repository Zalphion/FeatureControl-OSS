package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.http4k.lens.BiDiMapping

class RenameModalUi<NameType: Any, NextUi: Any>(
    locator: Locator,
    nameMapping: BiDiMapping<String, NameType>,
    private val nextUiFn: (Page) -> NextUi
): ModalUi(locator) {

    var name by locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Name"))
        .toInputProperty(nameMapping)

    fun submit(block: (NextUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Rename"))
        .also { it.click() }
        .let { nextUiFn(it.page()) }
        .also(block)
}