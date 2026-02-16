package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.config.web.ConfigSpecUi
import com.zalphion.featurecontrol.web.TableFormUi
import com.zalphion.featurecontrol.web.TableRowUi
import com.zalphion.featurecontrol.web.toInputProperty

class ApplicationCreateUpdateUi(private val modal: Locator): TableFormUi<ApplicationEnvironmentUi, EnvironmentName>(
    table = modal.locator("table"),
    getRowUi = ::ApplicationEnvironmentUi,
    getKey = ApplicationEnvironmentUi::name
) {

    var name by modal
        .getByLabel("Name")
        .toInputProperty(AppName)

    fun submit(block: (ConfigSpecUi) -> Unit = {}) = modal
        .locator("button[type=submit]")
        .click()
        .let { ConfigSpecUi(modal.page()) }
        .also(block)
}

class ApplicationEnvironmentUi(locator: Locator): TableRowUi(locator) {

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