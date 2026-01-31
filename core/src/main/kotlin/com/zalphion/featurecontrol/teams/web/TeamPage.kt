package com.zalphion.featurecontrol.teams.web

import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.NavBar
import com.zalphion.featurecontrol.web.PageLink
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.modalTextButton
import com.zalphion.featurecontrol.web.invitationsUri
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.moreMenu
import com.zalphion.featurecontrol.web.navbar
import com.zalphion.featurecontrol.web.pageSkeleton
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.memberNotFound
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.aside
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.li
import kotlinx.html.main
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.ul
import kotlin.collections.plus
import kotlin.collections.set

data class TeamPage(
    val navBar: NavBar<out MemberDetails?>,
    val team: MemberDetails,
    val pages: List<PageLink>
) {
    val filterModel = "team_element_filter"

    companion object {
        fun create(core: Core, permissions: Permissions<User>, teamId: TeamId, selected: PageSpec?): Result4k<TeamPage, AppError> {
            val navBar = NavBar.get(core, permissions, teamId, selected).onFailure { return it }
            val team = navBar.memberships.find { (_, _, team) -> team.teamId == teamId } ?: return memberNotFound(teamId, permissions.principal.userId).asFailure()

            return TeamPage(
                navBar = navBar,
                team = team,
                pages = listOf(
                    PageLink(PageSpec.members, membersUri(teamId)),
                    PageLink(PageSpec.invitations, invitationsUri(teamId))
                )
            ).asSuccess()
        }
    }
}

fun Core.teamPage(
    model: TeamPage,
    messages: List<FlashMessageDto>,
    content: FlowContent.(TeamPage) -> Unit
) = pageSkeleton(messages, "Manage Team") {
    navbar(model.navBar)

    div("uk-flex uk-height-viewport") {
        aside("uk-width-large uk-background-muted uk-padding-small uk-overflow-auto") {
            style = "box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);"

            teamNavBar(model.team.team)

            div {
                for (page in model.pages) {
                    iconButton(page, selected = page.spec == model.navBar.selectedPage)
                }
            }
        }

        main("uk-width-expand uk-padding-small uk-overflow-auto") {
            content(model)
        }
    }
}

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

private fun FlowContent.iconButton(page: PageLink, selected: Boolean) {
    a(page.uri.toString()) {
        button(type = ButtonType.button, classes = "uk-button uk-button-large uk-width-1-1") {
            if (page.tooltip != null) attributes["uk-tooltip"] = page.tooltip
            if (!page.enabled) attributes["disabled"] = ""
            classes + if (selected) "uk-button-primary" else "uk-button-default"
            style = "padding-bottom: 10px; padding-top: 10px; margin-top: 10px; margin-bottom: 10px;"

            span("uk-margin-small-right") {
                attributes["uk-icon"] = page.spec.icon
            }
            +page.spec.name
        }
    }
}