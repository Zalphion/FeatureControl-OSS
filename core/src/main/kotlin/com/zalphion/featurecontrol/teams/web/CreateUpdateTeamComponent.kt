package com.zalphion.featurecontrol.teams.web

import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.confirmCancelButtons
import com.zalphion.featurecontrol.web.teamUri
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h2
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.p

fun FlowContent.createUpdateTeamModal(team: Team?): String {
    val modalId = if (team == null) {
        "create-team-modal"
    } else {
        "update-team-modal-${team.teamId}"
    }

    div("uk-modal uk-modal-container") {
        id = modalId

        div("uk-modal-dialog") {
            form(
                method = FormMethod.post,
                action = if (team != null) teamUri(team.teamId).toString() else "/teams",
                classes = "uk-form-stacked"
            ) {

                div("uk-modal-header") {
                    h2("uk-modal-title") {
                        + if (team == null) "Create Team" else "Update ${team.teamName}"
                    }
                }

                div("uk-modal-body") {
                    button(type = ButtonType.button, classes = "uk-modal-close-default") {
                        attributes["uk-close"] = ""
                    }

                    p {
                        + if (team == null) {
                            "Create a new team to organize your applications and control access to users."
                        } else {
                            "Update ${team.teamName}"
                        }
                    }

                    div("uk-margin") {
                        input(InputType.text, classes = "uk-input uk-width-large") {
                            name = "teamName"
                            placeholder = team?.teamName?.value ?: "Team Name"
                            required = true
                        }
                    }
                }

                div("uk-modal-footer") {
                    confirmCancelButtons(if (team == null) "Create" else "Update")
                }
            }
        }
    }

    return modalId
}