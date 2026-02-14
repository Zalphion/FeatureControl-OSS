package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.web.ariaLabel
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.sanitizeSearchTerm
import com.zalphion.featurecontrol.web.template
import com.zalphion.featurecontrol.web.timestamp
import com.zalphion.featurecontrol.web.tr
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod
import kotlinx.html.LI
import kotlinx.html.TD
import kotlinx.html.TR
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
            core: Core,
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
                            th { +"Role"}
                            th { +"Status" }
                            for (header in extraColumns.map { it.first }) {
                                th { +header }
                            }
                            th { +"Actions" }
                        }
                    }
                    tbody {
                        for (details in data.members) {
                            if (data.filterModel != null) {
                                template {
                                    val searchTerms = "${details.user.userName}${details.user.emailAddress}".sanitizeSearchTerm()
                                    attributes["x-if"] = "'$searchTerms'.includes(${data.filterModel}.toLowerCase())"

                                    tr {
                                        memberRow(core, data.permissions, details, extraColumns, extraActions)
                                    }
                                }
                            } else {
                                tr {
                                    memberRow(core, data.permissions, details, extraColumns, extraActions)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun TR.memberRow(
    core: Core,
    permissions: Permissions<*>,
    details: MemberDetails,
    extraColumns: List<Pair<String, TD.(MemberDetails) -> Unit>>,
    extraActions: List<LI.(MemberDetails) -> Unit>
) {
    td {
        ariaLabel = "Username"
        +details.user.userName.orEmpty()
    }

    td {
        ariaLabel = "Email Address"
        +details.user.emailAddress.value
    }

    td {
        ariaLabel = "Role"
        core.render(this, RoleComponent(details))
    }

    // status
    td {
        ariaLabel = "Status"
        memberStatus(details.member)
    }

    for (render in extraColumns.map { it.second }) {
        td { render(details) }
    }

    // actions
    td {
        ul("uk-iconnav") {
            if(details.member.active && permissions.memberDelete(details.member)) {
                li { removeMemberButton(details) }
            }
            if (!details.member.active && permissions.memberUpdate(details.member)) {
                li { resendInvitation(details.member) }
            }

            for (action in extraActions) {
                li { action(details) }
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
    form(method = FormMethod.post, action = membersUri(member.teamId, member.userId).toString()) {
        button(type = ButtonType.submit, classes = "uk-icon-button") {
            attributes["uk-icon"] = "icon: refresh"
            attributes["uk-tooltip"] = "Resend Invitation"
        }
    }
}