package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.web.searchBar
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.nav
import kotlinx.html.onClick
import kotlinx.html.span
import kotlin.collections.plus

private const val FILTER_MODEL = "invitations_filter"

fun FlowContent.teamInvitations(
    core: Core,
    team: Team,
    invitations: List<MemberDetails>,
    permissions: Permissions<User>
) {
    nav("uk-navbar-container uk-navbar-transparent") {
        attributes["x-data"] = "{ $FILTER_MODEL: ''}"
        attributes["uk-navbar"] = ""

        val modalId = core.components(this, InviteMemberModalComponent(team))

        div("uk-navbar-left") {
            searchBar(FILTER_MODEL, "Search") {
                classes + "uk-navbar-item"
            }
            div("uk-navbar-item") {
                button(type = ButtonType.button, classes = "uk-button uk-button-primary") {
                    onClick = "UIkit.modal('#$modalId').show()"
                    span {
                        attributes["uk-icon"] = "icon: mail"
                    }
                    +"Invite"
                }
            }
        }
    }

    core.components(this, MembersComponent(team, invitations, permissions, FILTER_MODEL))
}