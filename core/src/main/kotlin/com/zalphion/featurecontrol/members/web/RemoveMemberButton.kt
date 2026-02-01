package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.web.deleteModal
import com.zalphion.featurecontrol.web.membersUri
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.onClick

fun FlowContent.removeMemberButton(details: MemberDetails) {
    val modalId = deleteModal(
        action = membersUri(details.team.teamId),
        resourceName = "${details.user.fullName()} from ${details.team.teamName}",
    )

    a("#", classes = "uk-icon-button") {
        attributes["uk-icon"] = "icon: sign-out"
        attributes["uk-tooltip"] = "Remove from Team"
        onClick = "UIkit.modal('#$modalId').show()"
    }
}