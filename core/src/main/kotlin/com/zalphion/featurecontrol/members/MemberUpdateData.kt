package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.plugins.Extensions

data class MemberUpdateData(
    val extensions: Extensions?
)

fun Member.update(data: MemberUpdateData) = Member(
    teamId = teamId,
    userId = userId,
    invitedBy = invitedBy,
    invitationExpiresOn = invitationExpiresOn,
    extensions = data.extensions ?: extensions
)