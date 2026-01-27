package com.zalphion.featurecontrol.users.web

import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.NavBar
import com.zalphion.featurecontrol.web.deleteModal
import com.zalphion.featurecontrol.web.invitationsUri
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.navbar
import com.zalphion.featurecontrol.web.pageSkeleton
import com.zalphion.featurecontrol.web.withRichMethod
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.web.TeamsComponent
import com.zalphion.featurecontrol.users.User
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.aside
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h3
import kotlinx.html.main
import kotlinx.html.nav
import kotlinx.html.onClick
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.ul
import org.http4k.core.Method

fun Core.userPage(
    permissions: Permissions<User>,
    navBar: NavBar<MemberDetails?>,
    messages: List<FlashMessageDto>,
    subTitle: String? = null
) = pageSkeleton(messages, subTitle) {
    navbar(navBar)

    div("uk-flex uk-height-viewport") {
        aside("uk-width-medium uk-background-muted uk-padding-small uk-overflow-auto") {
            style = "box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);"

            h3("uk-logo") {
                span("uk-margin-small-right") {
                    attributes["uk-icon"] = "icon: users"
                }
                +"User Settings"
            }
        }

        main("uk-width-expand uk-padding-small uk-overflow-auto") {
            teams(this@userPage, navBar.memberships.filter { details -> details.member.active }, permissions)
            invitations(navBar.memberships.filter { details -> !details.member.active }, permissions)
        }
    }
}

private fun FlowContent.teams(
    core: Core,
    teams: List<MemberDetails>,
    permissions: Permissions<User>
) {
    nav("uk-navbar-container uk-navbar-transparent") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h3("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                span {
                    attributes["uk-icon"] = "icon: users"
                }
                +"Teams"
            }
        }
    }

    core.render(this, TeamsComponent(teams, permissions))
}

private fun FlowContent.invitations(
    invitations: List<MemberDetails>,
    permissions: Permissions<User>
) {
    nav("uk-navbar-container uk-navbar-transparent") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h3("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                span {
                    attributes["uk-icon"] = "icon: mail"
                }
                +"Invitations"
            }
        }
    }

    table("uk-table uk-table-hover") {
        thead {
            tr {
                th { +"Team" }
                th { +"Invited By" }
                th { +"Expires" }
            }
        }
        tbody {
            for (details in invitations) {
                tr {
                    td { +details.team.teamName.value }
                    td { +details.member.invitedBy?.toEmailAddress()?.value.orEmpty() }
                    td("timestamp") { +details.member.invitationExpiresOn.toString() }
                    td {
                        if (permissions.userUpdate(details.member.userId)) {
                            ul("uk-iconnav") {
                                acceptInvitation(details.team)
                                revokeInvitation(details)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun FlowContent.acceptInvitation(team: Team) {
    form(invitationsUri(team.teamId).toString(), method = FormMethod.post) {
        withRichMethod(Method.POST)
        button(type = ButtonType.submit, classes = "uk-icon-button") {
            attributes["uk-icon"] = "icon: check"
            attributes["uk-tooltip"] = "Accept Invitation"
        }
    }
}

fun FlowContent.leaveTeam(team: Team) {
    form(membersUri(team.teamId).toString(), method = FormMethod.post) {
        withRichMethod(Method.DELETE)
        button(type = ButtonType.submit, classes = "uk-icon-button") {
            attributes["uk-icon"] = "icon: sign-out"
            attributes["uk-tooltip"] = "Leave Team"
        }
    }
}

fun FlowContent.revokeInvitation(details: MemberDetails) {
    val modalId = deleteModal(
        resourceName = "Invitation from ${details.member.invitedBy?.toEmailAddress()} to ${details.team.teamName}",
        action = membersUri(details.team.teamId, details.user.userId),
    )
    button(type = ButtonType.button, classes = "uk-icon-button") {
        attributes["uk-icon"] = "icon: trash"
        attributes["uk-tooltip"] = "Revoke Invitation"
        onClick = "UIkit.modal('#$modalId').show()"
    }
}