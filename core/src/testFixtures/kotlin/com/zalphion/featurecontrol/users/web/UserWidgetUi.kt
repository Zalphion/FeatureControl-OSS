package com.zalphion.featurecontrol.users.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.web.getControlled

class UserWidgetUi(private val locator: Locator) {

    val username get() = locator
        .getByLabel("Username")
        .takeIf { it.isVisible }
        ?.textContent()?.trim()

    val email get() = locator
        .getByLabel("Email")
        .takeIf { it.isVisible }
        ?.let { EmailAddress.parse(it.textContent().trim()) }

    fun open(block: (UserMenuUi) -> Unit = {}) = locator
        .also { it.click() }
        .getControlled()
        .let(::UserMenuUi)
        .also(block)
}

fun Page.userWidget() = getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("User Widget"))
    .let(::UserWidgetUi)

class UserMenuUi(private val locator: Locator) {

    fun goToSettings(block: (UserSettingsPage) -> Unit = {}) = locator
        .getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Settings"))
        .also { it.click() }
        .let { UserSettingsPage(locator.page()) }
        .also(block)

    fun logout() {
        locator
            .getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Logout"))
            .click()

        // TODO get login POM
    }
}