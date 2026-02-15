package com.zalphion.featurecontrol.users

import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.members.MemberStorage
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.teams.TeamName
import com.zalphion.featurecontrol.teams.TeamStorage
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.begin
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.flatMapFailure
import dev.forkhandles.result4k.map
import kotlin.random.Random

class UserService(
    private val random: Random,
    private val teams: TeamStorage,
    private val users: UserStorage,
    private val members: MemberStorage
) {

    fun create(data: UserCreateData) = begin
        .failIf(
            cond = { users[data.emailAddress] != null },
            f = { userAlreadyExists(data.emailAddress) }
        ).map { data.toUser() }
        .map { user ->
            // TODO don't force user to have a team; UI should prompt them instead
            val team = Team(
                teamId = TeamId.random(random),
                teamName = TeamName.parse("${user.userName}'s Team"),
            )
            val member = Member(
                teamId = team.teamId,
                userId = user.userId,
                invitedBy = null,
                invitationExpiresOn = null,
                extensions = emptyMap()
            )

            users += user
            teams += team
            members += member

            MemberDetails(member, user, team)
        }

    fun getOrCreate(data: UserCreateData) = begin
        .flatMap { users.getOrFail(data.emailAddress) }
        .flatMapFailure { create(data).map { it.user } }
}