package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.web.ariaHidden
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.components.modal
import com.zalphion.featurecontrol.web.components.modalCloseButton
import com.zalphion.featurecontrol.web.components.modalIconButton
import com.zalphion.featurecontrol.web.uri
import com.zalphion.featurecontrol.web.withRichMethod
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.LI
import kotlinx.html.TD
import kotlinx.html.button
import kotlinx.html.form
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.ul
import org.http4k.core.Method

class TeamsComponent(
    val memberships: List<MemberDetails>,
    val permissions: Permissions<*>
) {
    companion object {
        fun core(
            core: Core,
            extraColumnsFn: (TeamsComponent) -> List<Pair<String, TD.(MemberDetails) -> Unit>> = { emptyList() },
            extraActionsFn: (TeamsComponent) -> List<LI.(MemberDetails) -> Unit> = { emptyList() }
        ) = Component<TeamsComponent> { flow, data ->
            val extraColumns = extraColumnsFn(data)
            val extraActions = extraActionsFn(data)
            flow.table("uk-table uk-table-hover") {
                thead {
                    ariaHidden = true
                    tr {
                        th { +"Team" }
                        th { +"Role" }
                        for (header in extraColumns.map { it.first }) {
                            th { +header }
                        }
                        th { +"Actions" }
                    }
                }
                tbody {
                    for (details in data.memberships) {
                        tr {
                            td {
                                ariaLabel = "Team"
                                +details.team.teamName.value
                            }

                            td {
                                ariaLabel = "Role"
                                core.render(this, RoleComponent(details))
                            }

                            for (render in extraColumns.map { it.second }) {
                                td { render(details) }
                            }

                            td {
                                ul("uk-iconnav") {
                                    if (data.permissions.userUpdate(details.user.userId)) {
                                        li {
                                            modalIconButton(
                                                tooltip = "Leave",
                                                modalId = leaveTeamModal(details),
                                                icon = "icon: sign-out"
                                            )
                                        }
                                    }

                                    for (action in extraActions) {
                                        li { action(details) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun FlowContent.leaveTeamModal(details: MemberDetails): String {
    val modalId = "leave-team-${details.team.teamId}"
    modal(
        modalId = modalId,
        header = {
            span("uk-text-danger uk-margin-small-right") {
                attributes["uk-icon"] = "icon: warning; ratio: 2;"
            }
            +"Leave ${details.team.teamName}?"
        },
        body = {
            p {
                +"Are you sure you want to leave ${details.team.teamName}?"
            }
        },
        footer = {
            form(details.member.uri().toString(), method = FormMethod.post) {
                withRichMethod(Method.DELETE)

                button(type = ButtonType.submit, classes = "uk-button uk-button-danger") {
                    +"Leave"
                }

                modalCloseButton("Cancel")
            }
        }
    )

    return modalId
}