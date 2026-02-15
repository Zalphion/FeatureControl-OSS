package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.invitationNotFound
import com.zalphion.featurecontrol.memberAlreadyExists
import com.zalphion.featurecontrol.events.EventId
import com.zalphion.featurecontrol.events.MemberCreatedEvent
import com.zalphion.featurecontrol.lib.filterItem
import com.zalphion.featurecontrol.preAuth
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.teams.TeamStorage
import com.zalphion.featurecontrol.users.UserCreateData
import com.zalphion.featurecontrol.users.UserId
import com.zalphion.featurecontrol.users.UserService
import com.zalphion.featurecontrol.users.UserStorage
import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.peek
import dev.forkhandles.values.random
import java.time.Clock
import java.time.Duration
import kotlin.collections.map
import kotlin.random.Random

class MemberService(
    private val clock: Clock,
    private val random: Random,
    private val invitationRetention: Duration,
    private val userService: UserService,
    private val teams: TeamStorage,
    private val users: UserStorage,
    private val members: MemberStorage
) {
    // TODO cannot remove last admin of team
    fun update(teamId: TeamId, userId: UserId, data: MemberUpdateData) = preAuth {
        it.memberUpdate(teamId, userId)
    }.checkEntitlements(teamId) {
        it.getRequirements(data)
    }.flatMap {
        members.getOrFail(teamId, userId)
            .map { it.update(data) }
            .peek(members::plusAssign)
    }

    fun list(teamId: TeamId) = preAuth {
        it.teamRead(teamId)
    }.flatMap {
        val team = teams.getOrFail(teamId).onFailure { return@flatMap it }
        Paginator<MemberDetails, UserId> { cursor ->
            val members = members.list(teamId)[cursor]
            val relevantUserIds = members.items.map { it.userId }.plus(members.items.mapNotNull { it.invitedBy }).distinct()
            val relevantUsers = users[relevantUserIds].associateBy { it.userId }

            Page(
                items = members.items.mapNotNull { member ->
                    val user = relevantUsers[member.userId] ?: return@mapNotNull null
                    MemberDetails(member, user, team)
                },
                next = members.next
            )
        }.asSuccess()
    }.after { permissions, _, members ->
        members.filterItem { permissions.memberRead(it.member) }
    }

    fun list(userId: UserId) = preAuth {
        it.userUpdate(userId)
    }.flatMap {
        val user = users.getOrFail(userId).onFailure { return@flatMap it }
        Paginator<MemberDetails, TeamId> { cursor ->
            val members = members.list(userId)[cursor]
            val teams = teams.batchGet(members.items.map { it.teamId }).associateBy { it.teamId }
            Page(
                items = members.items.mapNotNull { member ->
                    val team = teams[member.teamId] ?: return@mapNotNull null
                    MemberDetails(member, user, team)
                },
                next = members.next
            )
        }.asSuccess()
    }.after { permissions, _, members ->
        members.filterItem { permissions.memberRead(it.member) }
    }

    // TODO cannot remove last admin of team
    fun remove(teamId: TeamId, userId: UserId) = preAuth {
        it.memberDelete(teamId, userId)
    }.flatMap {
        val team = teams.getOrFail(teamId).onFailure { return@flatMap it }
        val user = users.getOrFail(userId).onFailure { return@flatMap it }
        val member = members.getOrFail(teamId, userId).onFailure { return@flatMap it }

        members -= member
        MemberDetails(member, user, team).asSuccess()
    }

    fun invite(teamId: TeamId, sender: UserId, data: MemberCreateData) = preAuth {
        it.memberCreate(teamId)
    }.checkEntitlements(teamId) {
        it.getRequirements(data)
    }.flatMap {
        val team = teams.getOrFail(teamId).onFailure { return@flatMap it }
        val user = userService.getOrCreate(UserCreateData(
            emailAddress = data.emailAddress,
            userName = null,
            photoUrl = null
        )).onFailure { return@flatMap it }

        val existingMember = members[teamId, user.userId]
        if (existingMember != null) {
            return@flatMap memberAlreadyExists(existingMember).asFailure()
        }

        val member = Member(
            teamId = teamId,
            userId = user.userId,
            invitationExpiresOn = clock.instant() + invitationRetention,
            invitedBy = sender,
            extensions = data.extensions
        )
        members += member

        MemberDetails(member, user, team).asSuccess()
    }.after { _, app, details ->
        app.emitEvent(MemberCreatedEvent(
            teamId = teamId,
            eventId = EventId.random(random),
            time = clock.instant(),
            member = details
        ))
        details
    }

    fun acceptInvitation(teamId: TeamId, userId: UserId) = preAuth {
        it.userUpdate(userId)
    }.flatMap {
        val member = members.getOrFail(teamId, userId)
            .failIf(Member::active) { invitationNotFound(teamId, userId) }
            .onFailure { return@flatMap it }

        val team = teams.getOrFail(teamId).onFailure { return@flatMap it }
        val user = users.getOrFail(userId).onFailure { return@flatMap it }

        val updated =  member.copy(invitationExpiresOn = null)
        members += updated
        MemberDetails(updated, user, team).asSuccess()
    }

    // TODO this must be subject to rate limiting
    fun resendInvitation(teamId: TeamId, userId: UserId) = preAuth {
        it.memberUpdate(teamId, userId)
    }.flatMap {
        val member = members.getOrFail(teamId, userId)
            .failIf({it.active}, { invitationNotFound(teamId, userId) })
            .onFailure { return@flatMap it }

        val team = teams.getOrFail(teamId).onFailure { return@flatMap it }
        val user = users.getOrFail(userId).onFailure { return@flatMap it }
         MemberDetails(member, user, team).asSuccess()
    }.after { _, app, details ->
        app.emitEvent(MemberCreatedEvent(
            teamId = teamId,
            eventId = EventId.random(random),
            time = clock.instant(),
            member = details
        ))
        details
    }
}