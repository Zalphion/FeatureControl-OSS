package com.zalphion.featurecontrol.users.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.web.ApplicationsUi
import com.zalphion.featurecontrol.teams.TeamName
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.web.ModalUi
import com.zalphion.featurecontrol.web.getControlled
import com.zalphion.featurecontrol.web.getTime
import com.zalphion.featurecontrol.web.mainNavBar
import com.zalphion.featurecontrol.web.waitForAll
import com.zalphion.featurecontrol.web.waitForReady

private val urlRegex = ".*profile.*".toRegex()

class UserSettingsUi(private val page: Page) {

    init {
        page.waitForReady()
        PlaywrightAssertions.assertThat(page).hasURL(urlRegex.toPattern())
    }

    val mainNavBar get() = page.mainNavBar()

    val memberships get() = page
        .getByRole(AriaRole.REGION, Page.GetByRoleOptions().setName("Teams"))
        .getByRole(AriaRole.ROW)
        .waitForAll()
        .map(::UserMembershipUi)

    val invitations get() = page
        .getByRole(AriaRole.REGION, Page.GetByRoleOptions().setName("Invitations"))
        .getByRole(AriaRole.ROW)
        .waitForAll()
        .map(::UserInvitationUi)
}

class UserMembershipUi(private val locator: Locator) {

    val teamName get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Team"))
        .textContent().trim()
        .let(TeamName::parse)

    val role get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Role"))
        .textContent().trim()

    fun leave(block: (LeaveTeamUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Leave"))
        .also { it.click() }
        .getControlled()
        .let(::LeaveTeamUi)
        .also(block)
}

class LeaveTeamUi(locator: Locator): ModalUi(locator) {

    fun confirm(block: (UserSettingsUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Leave"))
        .also { it.click() }
        .let { UserSettingsUi(locator.page()) }
        .also(block)

}

class UserInvitationUi(private val locator: Locator) {

    val teamName get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Team"))
        .textContent().trim()
        .let(TeamName::parse)

    val invitedBy get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Invited By"))
        .textContent().trim()
        .takeIf { it.isNotEmpty() }
        ?.let(EmailAddress::parse)

    val expires get() = locator
        .getByRole(AriaRole.CELL, Locator.GetByRoleOptions().setName("Expires"))
        .getByRole(AriaRole.TIME)
        .takeIf { it.count() > 0 }
        ?.getTime()

    fun accept(block: (ApplicationsUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Accept"))
        .also { it.click() }
        .let { ApplicationsUi(locator.page()) }
        .also(block)

    fun reject(block: (UserSettingsUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Reject"))
        .also { it.click() }
        .let { UserSettingsUi(locator.page()) }
        .also(block)
}