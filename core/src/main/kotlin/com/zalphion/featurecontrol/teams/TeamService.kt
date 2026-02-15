package com.zalphion.featurecontrol.teams

import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberStorage
import com.zalphion.featurecontrol.preAuth
import com.zalphion.featurecontrol.users.UserId
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import kotlin.random.Random

class TeamService(
    private val random: Random,
    private val teams: TeamStorage,
    private val members: MemberStorage
) {
    fun create(ownerId: UserId, data: TeamCreateUpdateData) = preAuth {
        it.teamCreate()
    }.map {
        val team = Team(
            teamId = TeamId.random(random),
            teamName = data.teamName,
        )
        val member = Member(
            teamId = team.teamId,
            userId = ownerId,
            invitedBy = null,
            invitationExpiresOn = null,
            extensions = emptyMap()
        )

        teams += team
        members += member

        team
    }

    fun update(teamId: TeamId, data: TeamCreateUpdateData) = preAuth {
        it.teamUpdate(teamId)
    }.flatMap {
        teams.getOrFail(teamId).map { team ->
            teams += team.copy(teamName = data.teamName)
        }
    }
}