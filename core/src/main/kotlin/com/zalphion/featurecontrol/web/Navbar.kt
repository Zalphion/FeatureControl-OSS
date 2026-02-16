package com.zalphion.featurecontrol.web

import com.zalphion.featurecontrol.teams.web.teamSelector
import com.zalphion.featurecontrol.users.web.avatarView
import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teamNotFound
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.User
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.onFailure
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.UL
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.id
import kotlinx.html.li
import kotlinx.html.nav
import kotlinx.html.onClick
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.ul
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.toList

data class MainNavBar<T>(
    val appName: String,
    val permissions: Permissions<User>,
    val memberships: List<MemberDetails>,
    val selectedTeam: T,
    val pages: Collection<PageLink>,
    val selectedPage: PageSpec?
) {
    companion object {
        fun get(
            core: Core,
            permissions: Permissions<User>,
            teamId: TeamId,
            selected: PageSpec?
        ): Result4k<MainNavBar<MemberDetails>, AppError> {
            // TODO could maybe user the permissions object to determine this
            val authorizedTeams = core.members.list(permissions.principal.userId)
                .invoke(permissions)
                .onFailure { error(it) }
                .toList()

            val team = authorizedTeams
                .find { it.team.teamId == teamId }
                ?: return teamNotFound(teamId).asFailure()

            return MainNavBar(
                appName = core.appName,
                permissions = permissions,
                memberships = authorizedTeams,
                selectedTeam = team,
                pages = core.getPages(teamId),
                selectedPage = selected
            ).asSuccess()
        }

        fun get(
            core: Core,
            permissions: Permissions<User>
        ): MainNavBar<MemberDetails?> {
            val authorizedTeams = core.members.list(permissions.principal.userId)
                .invoke(permissions)
                .onFailure { error(it) }
                .toList()

            return MainNavBar(
                appName = core.appName,
                permissions = permissions,
                memberships = authorizedTeams,
                selectedTeam = null,
                pages = emptyList(),
                selectedPage = null
            )
        }
    }
}

internal fun FlowContent.renderNavbar(model: MainNavBar<out MemberDetails?>) = with(model) {
    nav("uk-navbar-container uk-navbar-transparent uk-background-primary uk-light") {
        attributes["uk-navbar"] = ""
        div("uk-navbar-left") {
            ul("uk-navbar-nav") {
                div("uk-navbar-item uk-logo") {
                    +model.appName
                }
                for (page in pages) {
                    pageLink(page, selectedPage)
                }
            }
        }

        div("uk-navbar-right") {
            div("uk-navbar-item") {
                teamSelector(memberships.map { it.team },selectedTeam, permissions)
            }
            div("uk-navbar-item") {
                userWidget(permissions.principal)
            }
        }
    }
}

private fun UL.pageLink(page: PageLink, selectedPage: PageSpec?) {
    li {
        if (page.spec == selectedPage) {
            classes += "uk-active"
        }
        a( if (page.enabled) page.uri.toString() else "#", classes = "uk-navbar-item") {
            if (!page.enabled) {
                classes += "uk-link-muted"
                ariaDisabled = true
            }
            if (page.spec == selectedPage) {
                ariaCurrent = AriaCurrent.Page
            }
            span("uk-icon uk-margin-xsmall-right") {
                attributes["uk-icon"] = page.spec.icon
            }
            +page.spec.name
        }
    }
}

private fun FlowContent.userWidget(user: User) {
    val dropdownId = "user-widget-dropdown"

    button(type = ButtonType.button, classes = "uk-button uk-button-default uk-border-pill uk-padding-remove-left") {
        style = "padding-right: 10px;"
        ariaLabel = "User Widget"
        ariaHasPopup = AriaPopup.Menu
        ariaControls = dropdownId

        avatarView(user.photoUrl, 40) {
            classes += "uk-margin-small-right"
        }
        if (user.userName != null) {
            span {
                ariaLabel = "Username"
                +user.userName
            }
        } else {
            span {
                ariaLabel = "Email"
                +user.emailAddress.value
            }
        }
        span {
            attributes["uk-drop-parent-icon"] = "ratio: 1.5"
        }
    }

    div("uk-navbar-dropdown") {
        id = dropdownId
        attributes["uk-dropdown"] = "mode: click;"

        form(LOGOUT_PATH, method = FormMethod.post) {
            id = "logout"
            style = "display: none;"
        }

        ul("uk-nav uk-navbar-dropdown-nav") {
            li {
                a("/profile") {
                    span {
                        attributes["uk-icon"] = "icon: user"
                    }
                    +"Settings"
                }
            }
            li("uk-nav-divider")
            li {
                a("#") {
                    onClick = "event.preventDefault(); document.getElementById('logout').submit();"
                    span {
                        attributes["uk-icon"] = "icon: sign-out"
                    }
                    +"Logout"
                }
            }
        }
    }
}