package com.zalphion.featurecontrol.users.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.MainNavBar
import com.zalphion.featurecontrol.web.pageSkeleton
import com.zalphion.featurecontrol.web.withRichMethod
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.web.TeamsComponent
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.web.PageLink
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.SideNav
import com.zalphion.featurecontrol.web.ariaHidden
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.ariaLabelledBy
import com.zalphion.featurecontrol.web.timestamp
import com.zalphion.featurecontrol.web.uri
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h3
import kotlinx.html.id
import kotlinx.html.nav
import kotlinx.html.section
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.ul
import org.http4k.core.Method
import org.http4k.core.Uri

fun Core.userPageComponent(
    permissions: Permissions<User>,
    navBar: MainNavBar<MemberDetails?>,
    messages: List<FlashMessageDto>,
) = pageSkeleton(
    messages = messages,
    subTitle = PageSpec.userSettings.name,
    topNav = navBar,
    sideNav = SideNav(
        pages = listOf(PageLink(PageSpec.userSettings, Uri.of("profile"))),
        selected = PageSpec.userSettings,
        topBar = { h3 { +navBar.permissions.principal.let { it.userName ?: it.emailAddress.value } }}
    )
) {
    invitations(navBar.memberships.filter { details -> !details.member.active }, permissions)
    teams(it, navBar.memberships.filter { details -> details.member.active }, permissions)
}

private fun FlowContent.teams(
    core: Core,
    teams: List<MemberDetails>,
    permissions: Permissions<User>
) = section {
    val sectionHeaderId = "team-memberships-heading"
    ariaLabelledBy = sectionHeaderId

    nav("uk-navbar-container uk-navbar-transparent") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h3("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                id = sectionHeaderId
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
) = section {
    val sectionHeaderId = "user-invitations-heading"
    ariaLabelledBy = sectionHeaderId

    nav("uk-navbar-container uk-navbar-transparent") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            h3("uk-navbar-item uk-logo uk-margin-remove-bottom") {
                id = sectionHeaderId
                span {
                    attributes["uk-icon"] = "icon: mail"
                }
                +"Invitations"
            }
        }
    }

    table("uk-table uk-table-hover") {
        thead {
            ariaHidden = true
            tr {
                th { +"Team" }
                th { +"Invited By" }
                th { +"Expires" }
                th { +"Actions" }
            }
        }
        tbody {
            for (details in invitations) {
                tr {
                    td {
                        ariaLabel = "Team"
                        +details.team.teamName.value
                    }

                    td {
                        ariaLabel = "Invited By"
                        +details.member.invitedBy?.toEmailAddress()?.value.orEmpty()
                    }
                    td {
                        ariaLabel = "Expires"
                        details.member.invitationExpiresOn?.let(::timestamp)
                    }
                    td {
                        if (permissions.userUpdate(details.member.userId)) {
                            ul("uk-iconnav") {
                                acceptInvitation(details.team)
                                rejectInvitation(details.member)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.acceptInvitation(team: Team) {
    form("invitations/${team.teamId}", method = FormMethod.post) {
        withRichMethod(Method.POST)
        button(type = ButtonType.submit, classes = "uk-icon-button") {
            attributes["uk-icon"] = "icon: check"
            attributes["uk-tooltip"] = "Accept"
            ariaLabel = "Accept"
        }
    }
}

private fun FlowContent.rejectInvitation(member: Member) {
    form(member.uri().toString(), method = FormMethod.post) {
        withRichMethod(Method.DELETE)
        button(type = ButtonType.submit, classes = "uk-icon-button") {
            attributes["uk-icon"] = "icon: trash"
            attributes["uk-tooltip"] = "Reject"
            ariaLabel = "Reject"
        }
    }
}