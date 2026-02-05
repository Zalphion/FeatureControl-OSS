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
        .getByPlaceholder("Search")
        .toInputProperty()

    val members get() = page
        .getByRole(AriaRole.MAIN)
        .locator("tbody tr:visible") // only pick rows that aren't hidden by the search filter
        .waitForAll()
        .map(::MemberUi)

    fun inviteMember(block: (InviteMemberUi) -> Unit = {}) = page
        .getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Invite"))
        .also { it.click() }
        .let { InviteMemberUi(it.getControlled()) }
        .also(block)
}

class MemberUi(private val locator: Locator) {
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
        .toInputProperty(EmailAddress)

    fun send(block: (MembersPage) -> Unit = {}): MembersPage {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Send")).click()
        return MembersPage(locator.page()).also(block)
    }
}