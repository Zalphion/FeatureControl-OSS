package com.zalphion.featurecontrol.teams.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.web.getControlled
import com.zalphion.featurecontrol.web.mainNavBar
import com.zalphion.featurecontrol.web.toInputProperty
import com.zalphion.featurecontrol.web.waitForAll
import java.time.Instant

class MembersPage(private val page: Page) {

    val mainNavBar get() = page.mainNavBar()

    var searchTerm by page
        .getByRole(AriaRole.MAIN)
        .getByRole(AriaRole.NAVIGATION)
        .getByRole(AriaRole.SEARCH)
        .toInputProperty()

    val members get() = page
        .getByRole(AriaRole.MAIN)
        .locator("tbody tr")
        .waitForAll()
        .map(::MemberComponent)

    fun inviteMember(block: (InviteMemberUi) -> Unit = {}) = page
        .getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Invite Member"))
        .also { it.click() }
        .let { InviteMemberUi(it.getControlled()) }
        .also(block)
}

class MemberComponent(private val locator: Locator) {
    val username get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Username"))
        .textContent().trim()
        .takeIf { it.isNotEmpty() }

    val emailAddress get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Email Address"))
        .let { EmailAddress.parse(it.textContent().trim()) }

    val active get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Status"))
        .textContent().trim()
        .equals("Active", ignoreCase = true)

    val expires get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Status"))
        .locator("time")
        .takeIf { it.count() > 0 }
        ?.let { Instant.parse(it.getAttribute("datetime")) }
}

class InviteMemberUi(private val locator: Locator) {

    var emailAddress by locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Email Address"))
        .toInputProperty()

    fun send(block: (MembersPage) -> Unit = {}): MembersPage {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Send Invite")).click()
        return MembersPage(locator.page()).also(block)
    }
}