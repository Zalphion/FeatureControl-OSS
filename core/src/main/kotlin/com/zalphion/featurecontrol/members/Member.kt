package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.plugins.Extendable
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserId
import java.time.Instant

data class Member(
    val teamId: TeamId,
    val userId: UserId,
    val invitedBy: UserId?,
    val invitationExpiresOn: Instant?,
    override val extensions: Extensions
): Extendable<Member> {
    val active get() = invitationExpiresOn == null

    fun ifInactive(block: (Instant) -> Unit) = if (invitationExpiresOn == null) null else {
        block(invitationExpiresOn)
        invitationExpiresOn
    }

    override fun with(extensions: Extensions) = copy(extensions = this.extensions + extensions)
}