package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.web.ApplicationsPage
import com.zalphion.featurecontrol.teams.TeamName
import com.zalphion.featurecontrol.members.web.MembersPage

class MainNavBarUi(private val locator: Locator) {

    fun goToApplications(block: (ApplicationsPage) -> Unit = {}) = locator
        .getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Applications"))
        .also { it.click() }
        .let { ApplicationsPage(locator.page()) }
        .also(block)

    val currentTeam get() = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Team").setExact(true))
        .let { TeamName.parse(it.textContent().trim()) }

    fun openTeams(block: (TeamMenuUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Team").setExact(true))
        .also { it.click() }
        .getControlled()
        .also { PlaywrightAssertions.assertThat(it).isVisible() } // wait for the menu to open
        .let { TeamMenuUi(it) }
        .also(block)

    // TODO user widget
}

fun Page.mainNavBar() = MainNavBarUi(getByRole(AriaRole.NAVIGATION).first())

class TeamMenuUi(private val locator: Locator) {

    val options get() = locator
        .getByRole(AriaRole.MENUITEM)
        .waitForAll()
        .map { TeamName.parse(it.textContent().trim()) }

    fun goToTeam(team: TeamName, block: (ApplicationsPage) -> Unit): ApplicationsPage {
        locator.getByRole(AriaRole.MENUITEM, Locator.GetByRoleOptions().setName(team.value)).click()
        return ApplicationsPage(locator.page()).also(block)
    }

    fun manageTeam(block: (MembersPage) -> Unit): MembersPage {
        locator.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Manage Team")).click()
        return MembersPage(locator.page()).also(block)
    }

    fun createTeam(block: (CreateTeamUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Create Team"))
        .also { it.click() }
        .getControlled()
        .let(::CreateTeamUi)
        .also(block)
}

class CreateTeamUi(private val locator: Locator) {

    var name by locator
        .getByRole(AriaRole.TEXTBOX, Locator.GetByRoleOptions().setName("Team Name"))
        .toInputProperty(TeamName)

    fun create(block: (MembersPage) -> Unit): MembersPage {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Create")).click()
        return MembersPage(locator.page()).also(block)
    }
}