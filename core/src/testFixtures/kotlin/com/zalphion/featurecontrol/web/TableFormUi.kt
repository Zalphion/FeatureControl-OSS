package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole

abstract class TableFormUi<RowUI: TableRowUi, Key: Any>(
    protected val table: Locator,
    private val getRowUi: (Locator) -> RowUI,
    private val getKey: (RowUI) -> Key?
) {

    val options get() = table
        .also { it.page().waitForReady() }
        .locator("tbody tr")
        .waitForAll()
        .map(getRowUi)

    fun add(block: (RowUI) -> Unit = {}): RowUI {
        table.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Add")).click()
        return table
            .locator("tbody tr")
            .last()
            .let(getRowUi)
            .also(block)
    }

    operator fun get(key: Key) = options.find { getKey(it) == key }
}

abstract class TableRowUi(val locator: Locator) {
    fun remove() = locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Remove")).click()
}