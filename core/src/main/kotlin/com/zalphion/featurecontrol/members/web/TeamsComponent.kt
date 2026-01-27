package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.plugins.Component
import com.zalphion.featurecontrol.users.web.leaveTeam
import kotlinx.html.LI
import kotlinx.html.TD
import kotlinx.html.li
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.ul

class TeamsComponent(
    val memberships: List<MemberDetails>,
    val permissions: Permissions<*>
) {
    companion object {
        fun core(
            extraColumnsFn: (TeamsComponent) -> List<Pair<String, TD.(MemberDetails) -> Unit>> = { emptyList() },
            extraActionsFn: (TeamsComponent) -> List<LI.(MemberDetails) -> Unit> = { emptyList() }
        ) = Component<TeamsComponent> { flow, data ->
            val extraColumns = extraColumnsFn(data)
            val extraActions = extraActionsFn(data)
            with(flow) {
                table("uk-table uk-table-hover") {
                    thead {
                        tr {
                            th { +"Team" }
                            th { +"Status" }
                            for (header in extraColumns.map { it.first }) {
                                th { +header }
                            }
                        }
                    }
                    tbody {
                        for (details in data.memberships) {
                            tr {
                                td { +details.team.teamName.value }

                                td { memberStatus(details.member) }

                                for (render in extraColumns.map { it.second }) {
                                    td { render(details) }
                                }

                                td {
                                    ul("uk-iconnav") {
                                        if (data.permissions.memberUpdate(details.member)) {
                                            li { leaveTeam(details.team) }
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