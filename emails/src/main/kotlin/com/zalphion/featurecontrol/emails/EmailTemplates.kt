package com.zalphion.featurecontrol.emails

import com.zalphion.featurecontrol.members.MemberDetails
import org.http4k.core.Uri

fun FullEmailMessage.Companion.invitation(
    appName: String,
    loginUri: Uri,
    details: MemberDetails
): FullEmailMessage {
    val invitedBy = details.member.invitedBy

    return FullEmailMessage(
        to = listOf(details.user.emailAddress),
        data = EmailMessageData(
            subject = "You've been invited to join ${details.team.teamName}",
            textBody = """
            ${if (invitedBy != null) {
                "${invitedBy.toEmailAddress()} has invited you to join ${details.team.teamName} on $appName."
            } else {
                "You've been invited to join ${details.team.teamName}."
            }}
            
            To accept, log in at $loginUri 
            
            ${
                if (details.member.invitationExpiresOn != null) {
                    "This invitation expires on ${details.member.invitationExpiresOn}."
                } else ""
            }
        """.trimIndent(),
            htmlBody = """"""
        )
    )
}