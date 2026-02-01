package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.confirmCancelButtons
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.withRichMethod
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
import kotlinx.html.label
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.strong
import org.http4k.core.Method

class InviteMemberModalComponent(val team: Team, val modalId: String) {

    companion object {
        fun core(
            extraInputs: FlowContent.(InviteMemberModalComponent) -> Unit = {}
        ) = Component<InviteMemberModalComponent> { flow, data ->
            flow.createMemberModal(data, extraInputs)
        }
    }
}

private fun FlowContent.createMemberModal(
    data: InviteMemberModalComponent,
    extraInputs: FlowContent.(InviteMemberModalComponent) -> Unit
) = div("uk-modal uk-modal-container") {
    id = data.modalId

    div("uk-modal-dialog") {
        form(method = FormMethod.post, action = membersUri(data.team.teamId).toString(), classes = "uk-form-horizontal") {
            withRichMethod(Method.POST)

            div("uk-modal-header") {
                h2("uk-modal-title") { +"Invite Members" }
            }

            div("uk-modal-body") {
                button(type = ButtonType.button, classes = "uk-modal-close-default") {
                    attributes["uk-close"] = ""
                }

                p {
                    + "Invite new members to "
                    strong {
                        + data.team.teamName.value
                    }
                }

                div("uk-margin") {
                    label("uk-form-label") {
                        span {
                            attributes["uk-icon"] = "icon: mail"
                        }
                        +"Email Address"
                    }
                    input(InputType.email, classes = "uk-input") {
                        name = "emailAddress"
                        required = true
                        placeholder = "Email Address"
                    }
                }

                extraInputs(data)
            }

            div("uk-modal-footer") {
                confirmCancelButtons("Send")
            }
        }
    }
}
