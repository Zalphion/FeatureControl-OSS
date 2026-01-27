package com.zalphion.featurecontrol.users

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.teams.TeamName
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.begin
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.flatMapFailure
import dev.forkhandles.result4k.map

class UserService(private val core: Core) {

    fun create(data: UserCreateData) = begin
        .failIf(
            cond = { core.users[data.emailAddress] != null },
            f = { userAlreadyExists(data.emailAddress) }
        ).map { data.toUser() }
        .map { user ->
            val team = Team(
                teamId = TeamId.random(core.random),
                teamName = TeamName.parse("${user.userName}'s Team"),
            )
            val member = Member(
                teamId = team.teamId,
                userId = user.userId,
                invitedBy = null,
                invitationExpiresOn = null,
                extensions = emptyMap()
            )

            core.users += user
            core.teams += team
            core.members += member

            MemberDetails(member, user, team)
        }

    fun getOrCreate(data: UserCreateData) = begin
        .flatMap { core.users.getOrFail(data.emailAddress) }
        .flatMapFailure { create(data).map { it.user } }
}