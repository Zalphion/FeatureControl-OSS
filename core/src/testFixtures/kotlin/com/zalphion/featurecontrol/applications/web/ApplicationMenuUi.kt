package com.zalphion.featurecontrol.applications.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.web.DeleteModalUi
import com.zalphion.featurecontrol.web.getControlled

class ApplicationMenuUi(private val section: Locator) {

    fun update(block: (ApplicationCreateUpdateUi) -> Unit = {}) = section
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Update Application"))
        .also { it.click() }
        .getControlled()
        .let(::ApplicationCreateUpdateUi)
        .also(block)

    fun delete(block: (DeleteModalUi<AppName, ApplicationsUi>) -> Unit = {}) = section
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Delete Application"))
        .also { it.click() }
        .getControlled()
        .let { DeleteModalUi(it, AppName, ::ApplicationsUi) }
        .also(block)
}