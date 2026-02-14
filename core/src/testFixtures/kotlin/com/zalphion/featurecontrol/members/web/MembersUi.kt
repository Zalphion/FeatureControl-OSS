package com.zalphion.featurecontrol.members.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.web.getControlled
import com.zalphion.featurecontrol.web.mainNavBar
import com.zalphion.featurecontrol.web.toInputProperty
import com.zalphion.featurecontrol.web.waitForAll
import com.zalphion.featurecontrol.web.waitForReady
import java.time.Instant

private val urlRegex = ".*teams/([^/]+)/members.*".toRegex()

class MembersUi(private val page: Page) {

    init {
        page.waitForReady()
        PlaywrightAssertions.assertThat(page).hasURL(urlRegex.toPattern())
    }

    val mainNavBar get() = page.mainNavBar()

    var searchTerm by page
        .getByRole(AriaRole.MAIN)
        .getByRole(AriaRole.NAVIGATION)
        .getByPlaceholder("Search")
        .toInputProperty()

    val members get() = page
        .getByRole(AriaRole.MAIN)
        .locator("tbody tr")
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

    fun send(block: (MembersUi) -> Unit = {}): MembersUi {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Send")).click()
        return MembersUi(locator.page()).also(block)
    }
}