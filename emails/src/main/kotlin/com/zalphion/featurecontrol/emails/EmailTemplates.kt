package com.zalphion.featurecontrol.emails

import com.zalphion.featurecontrol.APP_NAME
import com.zalphion.featurecontrol.members.MemberDetails

fun FullEmailMessage.Companion.invitation(emails: Email, details: MemberDetails): FullEmailMessage {
    val invitedBy = details.member.invitedBy

    return FullEmailMessage(
        to = listOf(details.user.emailAddress),
        data = EmailMessageData(
            subject = "You've been invited to join ${details.team.teamName}",
            textBody = """
            ${if (invitedBy != null) {
                "${invitedBy.toEmailAddress()} has invited you to join ${details.team.teamName} on $APP_NAME."
            } else {
                "You've been invited to join ${details.team.teamName}."
            }}
            
            To accept, log in at ${emails.loginUri} 
            
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