package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.web.ariaControls
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

private const val FILTER_MODEL = "members_filter"

fun FlowContent.membersView(
    core: Core,
    team: Team,
    members: List<MemberDetails>,
    permissions: Permissions<User>
) = div {
    attributes["x-data"] = "{ $FILTER_MODEL: ''}"

    nav("uk-navbar-container uk-navbar-transparent") {
        attributes["uk-navbar"] = ""

        div("uk-navbar-left") {
            searchBar(FILTER_MODEL, "Search") {
                classes += "uk-navbar-item"
            }
            div("uk-navbar-item") {
                val modalId = "invite_member_modal_${team.teamId}"
                core.render(this, InviteMemberModalComponent(team, modalId))
                button(type = ButtonType.button, classes = "uk-button uk-button-primary") {
                    ariaControls = modalId
                    onClick = "UIkit.modal('#$modalId').show()"
                    span {
                        attributes["uk-icon"] = "icon: mail"
                    }
                    +"Invite"
                }
            }
        }
    }

    core.render(this, MembersComponent(team, members, permissions, FILTER_MODEL))
}