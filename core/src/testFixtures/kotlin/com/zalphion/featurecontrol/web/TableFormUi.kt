package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole

abstract class TableFormUi<RowUI: TableRowUi>(
    protected val locator: Locator,
    private val getRowUi: (Locator) -> RowUI
) {

    val options get() = locator
        .also { it.page().waitForReady() }
        .locator("tbody tr")
        .waitForAll()
        .map(getRowUi)

    fun add(block: (RowUI) -> Unit = {}): RowUI {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Add")).click()
        return locator
            .locator("tbody tr")
            .last()
            .let(getRowUi)
            .also(block)
    }
}

abstract class TableRowUi(protected val locator: Locator) {
    fun remove() = locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Remove")).click()
}