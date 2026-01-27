package com.zalphion.featurecontrol.teams.web

import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.applicationsUri
import kotlinx.html.*
import kotlin.collections.plus

fun FlowContent.teamSelector(
    memberships: List<Team>,
    current: MemberDetails?,
    permissions: Permissions<User>
) {
    button(type = ButtonType.button, classes = "uk-button uk-button-default uk-border-pill") {
        style = "padding-left: 10px; padding-right: 10px"
        span("uk-margin-small-right") {
            attributes["uk-icon"] = "icon: users; ratio: 1.5"
        }
        +(current?.team?.teamName?.value ?: "Select a Team")
        span {
            attributes["uk-drop-parent-icon"] = "ratio: 1.5"
        }
    }

    div("uk-navbar-dropdown") {
        attributes["uk-dropdown"] = "mode: click;"

        ul("uk-nav uk-navbar-dropdown-nav") {
            for (team in memberships) {
                li {
                    if (current?.team == team) classes + "uk-active"
                    a(applicationsUri(team.teamId).toString()) {
                        +team.teamName.value
                    }
                }
            }
            li("uk-nav-divider")
            if (current != null && permissions.teamUpdate(current.team.teamId)) {
                li {
                    a(membersUri(current.team.teamId).toString()) {
                        span {
                            attributes["uk-icon"] = "icon: file-edit"
                        }
                        +"Manage Team"
                    }
                }
            }
            li {
                val createTeamModalId = createUpdateTeamModal(null)
                a(classes = "navbar-item") {
                    onClick = "UIkit.modal('#$createTeamModalId').show()"
                    span {
                        attributes["uk-icon"] = "icon: users"
                    }
                    +"Create Team"
                }
            }
        }
    }
}