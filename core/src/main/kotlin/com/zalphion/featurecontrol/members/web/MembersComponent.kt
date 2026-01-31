package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.invitationsUri
import com.zalphion.featurecontrol.web.timestamp
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.LI
import kotlinx.html.TD
import kotlinx.html.button
import kotlinx.html.form
import kotlinx.html.li
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.ul

class MembersComponent(
    val team: Team,
    val members: List<MemberDetails>,
    val permissions: Permissions<*>,
    val filterModel: String?
) {
    companion object {
        fun core(
            extraColumnsFn: (MembersComponent) -> List<Pair<String, TD.(MemberDetails) -> Unit>> = { emptyList() },
            extraActionsFn: (MembersComponent) -> List<LI.(MemberDetails) -> Unit> = { emptyList() }
        ) = Component<MembersComponent> { flow, data ->
            val extraColumns = extraColumnsFn(data)
            val extraActions = extraActionsFn(data)
            with(flow) {
                table("uk-table uk-table-hover") {
                    thead {
                        tr {
                            th { +"Name" }
                            th { +"Email" }
                            th { +"Status" }
                            for (header in extraColumns.map { it.first }) {
                                th { +header }
                            }
                        }
                    }
                    tbody {
                        for (details in data.members) {
                            tr {
                                if (data.filterModel != null) {
                                    val searchTerms = "${details.user.userName}${details.user.emailAddress}".lowercase().replace("'", "\"")
                                    attributes["x-show"] = "'$searchTerms'.toLowerCase().includes(filter.toLowerCase())"
                                }

                                td { +details.user.userName.orEmpty() }

                                td { +details.user.emailAddress.value }

                                // status
                                td { memberStatus(details.member) }

                                for (render in extraColumns.map { it.second }) {
                                    td { render(details) }
                                }

                                // actions
                                td {
                                    ul("uk-iconnav") {
                                       if(details.member.active && data.permissions.memberDelete(details.member)) {
                                            li { removeMemberButton(details) }
                                        }
                                        if (!details.member.active && data.permissions.memberUpdate(details.member)) {
                                            li { resendInvitation(details.member) }
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
}

internal fun TD.memberStatus(member: Member) {
    member.ifInactive { expiresOn ->
        span {
            attributes["uk-icon"] = "icon: question"
        }
        +"Pending: Expires on"
        timestamp(expiresOn)
    }  ?: +"Active"
}

private fun FlowContent.resendInvitation(member: Member) {
    form(method = FormMethod.post, action = invitationsUri(member.teamId, member.userId).toString()) {
        button(type = ButtonType.submit, classes = "uk-icon-button") {
            attributes["uk-icon"] = "icon: refresh"
            attributes["uk-tooltip"] = "Resend Invitation"
        }
    }
}