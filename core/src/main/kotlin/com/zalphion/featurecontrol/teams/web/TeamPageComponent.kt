package com.zalphion.featurecontrol.teams.web

import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.MainNavBar
import com.zalphion.featurecontrol.web.PageLink
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.components.modalTextButton
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.components.moreMenu
import com.zalphion.featurecontrol.web.pageSkeleton
import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.memberNotFound
import com.zalphion.featurecontrol.web.SideNav
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.li
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.ul
import kotlin.collections.set

data class TeamPageComponent(
    val navBar: MainNavBar<out MemberDetails?>,
    val team: MemberDetails,
    val pages: List<PageLink>
) {
    val filterModel = "team_element_filter"

    companion object {
        fun create(core: Core, permissions: Permissions<User>, teamId: TeamId, selected: PageSpec?): Result4k<TeamPageComponent, AppError> {
            val navBar = MainNavBar.get(core, permissions, teamId, selected).onFailure { return it }
            val team = navBar.memberships.find { (_, _, team) -> team.teamId == teamId } ?: return memberNotFound(teamId, permissions.principal.userId).asFailure()

            return TeamPageComponent(
                navBar = navBar,
                team = team,
                pages = listOf(
                    PageLink(PageSpec.members, membersUri(teamId)),
                )
            ).asSuccess()
        }
    }
}

fun Core.teamPage(
    model: TeamPageComponent,
    messages: List<FlashMessageDto>,
    content: FlowContent.(TeamPageComponent) -> Unit
) = pageSkeleton(
    messages = messages,
    subTitle = "Manage Team",
    topNav = model.navBar,
    sideNav = SideNav(
        topBar = { teamNavBar(model.team.team) },
        pages = model.pages,
        selected = model.navBar.selectedPage,
    ),
    mainContent = { content(model) }
)

private fun FlowContent.teamNavBar(team: Team) {
    nav("uk-navbar-container") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h3("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                span("uk-margin-small-right") {
                    attributes["uk-icon"] = "icon: users"
                }
                +team.teamName.value
            }
        }

        div("uk-navbar-right") {
            ul("uk-iconnav") {
                li {
                    moreMenu(team.teamId) { dropdownId ->
                        li {
                            val updateModalId = createUpdateTeamModal(team)
                            modalTextButton(
                                label = "Rename",
                                icon = "icon: file-edit",
                                modalId = updateModalId,
                                dropdownToCloseId = dropdownId
                            )
                        }
                    }
                }
            }
        }
    }
}