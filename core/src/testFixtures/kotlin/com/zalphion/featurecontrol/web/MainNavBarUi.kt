package com.zalphion.featurecontrol.web

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.zalphion.featurecontrol.applications.web.ApplicationsUi
import com.zalphion.featurecontrol.teams.TeamName
import com.zalphion.featurecontrol.members.web.MembersUi
import com.zalphion.featurecontrol.users.web.UserWidgetUi

class MainNavBarUi(val locator: Locator) {

    fun goToApplications(block: (ApplicationsUi) -> Unit = {}) = locator
        .getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Applications"))
        .also { it.click() }
        .let { ApplicationsUi(locator.page()) }
        .also(block)

    val user get() = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("User Widget"))
        .let(::UserWidgetUi)

    val currentTeam get() = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Team").setExact(true))
        .let { TeamName.parse(it.textContent().trim()) }

    val selectedPage: String? = locator
        .locator("a[aria-current=page]")
        .takeIf { it.isVisible }
        ?.textContent()?.trim()

    fun openTeams(block: (TeamMenuUi) -> Unit = {}) = locator
        .getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Team").setExact(true))
        .also { it.click() }
        .getControlled()
        .also { PlaywrightAssertions.assertThat(it).isVisible() } // wait for the menu to open
        .let { TeamMenuUi(it) }
        .also(block)
}

fun Page.mainNavBar() = MainNavBarUi(getByRole(AriaRole.NAVIGATION).first())

class TeamMenuUi(private val locator: Locator) {

    val options get() = locator
        .getByRole(AriaRole.MENUITEM)
        .waitForAll()
        .map { TeamName.parse(it.textContent().trim()) }

    fun goToTeam(team: TeamName, block: (ApplicationsUi) -> Unit): ApplicationsUi {
        locator.getByRole(AriaRole.MENUITEM, Locator.GetByRoleOptions().setName(team.value)).click()
        return ApplicationsUi(locator.page()).also(block)
    }

    fun manageTeam(block: (MembersUi) -> Unit): MembersUi {
        locator.getByRole(AriaRole.LINK, Locator.GetByRoleOptions().setName("Manage Team")).click()
        return MembersUi(locator.page()).also(block)
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

    fun create(block: (MembersUi) -> Unit): MembersUi {
        locator.getByRole(AriaRole.BUTTON, Locator.GetByRoleOptions().setName("Create")).click()
        return MembersUi(locator.page()).also(block)
    }
}