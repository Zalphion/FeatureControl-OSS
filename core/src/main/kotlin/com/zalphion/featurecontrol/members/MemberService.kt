package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.forbidden
import com.zalphion.featurecontrol.invitationNotFound
import com.zalphion.featurecontrol.memberAlreadyExists
import com.zalphion.featurecontrol.memberNotFound
import com.zalphion.featurecontrol.ActionAuth
import com.zalphion.featurecontrol.ServiceAction
import com.zalphion.featurecontrol.events.EventId
import com.zalphion.featurecontrol.events.MemberCreatedEvent
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.users.UserCreateData
import com.zalphion.featurecontrol.users.UserId
import com.zalphion.featurecontrol.users.UserService
import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import dev.andrewohara.utils.result.failIf
import dev.andrewohara.utils.result.recoverIf
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.begin
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.peek
import dev.forkhandles.values.random
import kotlin.collections.map

// TODO cannot remove last admin of team
class UpdateMember(val teamId: TeamId, val userId: UserId, val role: UserRole): ServiceAction<Member>(
    auth = ActionAuth { core, user ->
        begin
            .failIf({ userId == user.userId}, { forbidden })
            .flatMap { core.members.getOrFail(teamId, userId) }
            .map { ActionAuth.byTeam(teamId, UserRole.Admin) }
            .flatMap { it(core, user) }
    }
) {
    override fun execute(core: Core) = core
        .members[teamId, userId].asResultOr { memberNotFound(teamId, userId) }
        .map { it.copy(role = role) }
        .peek(core.members::plusAssign)
}

class ListMembersForTeam(val teamId: TeamId): ServiceAction<Paginator<MemberDetails, UserId>>(
    auth = ActionAuth.byTeam(teamId)
) {
    override fun execute(core: Core): Result4k<Paginator<MemberDetails, UserId>, AppError> {
        val team = core.teams.getOrFail(teamId).onFailure { return it }
        return Paginator<MemberDetails, UserId> { cursor ->
            val members = core.members.list(teamId)[cursor]
            val relevantUserIds = members.items.map { it.userId }.plus(members.items.mapNotNull { it.invitedBy }).distinct()
            val relevantUsers = core.users[relevantUserIds].associateBy { it.userId }

            Page(
                items = members.items.mapNotNull { member ->
                    val user = relevantUsers[member.userId] ?: return@mapNotNull null
                    val invitedBy = member.invitedBy?.let(relevantUsers::get)
                    MemberDetails(member, user,invitedBy, team)
                },
                next = members.next
            )
        }.asSuccess()
    }
}

class ListMembersForUser(val userId: UserId): ServiceAction<Paginator<MemberDetails, TeamId>>(
    auth = ActionAuth { _, user -> begin.failIf({user.userId != userId}, {forbidden})}
) {
    override fun execute(core: Core): Result4k<Paginator<MemberDetails, TeamId>, AppError> {
        val user = core.users.getOrFail(userId).onFailure { return it }
        return Paginator<MemberDetails, TeamId> { cursor ->
            val members = core.members.list(userId)[cursor]
            val teams = core.teams.batchGet(members.items.map { it.teamId }).associateBy { it.teamId }
            val inviters = core.users[members.items.mapNotNull { it.invitedBy }.distinct()].associateBy { it.userId }
            Page(
                items = members.items.mapNotNull { member ->
                    val team = teams[member.teamId] ?: return@mapNotNull null
                    val invitedBy = member.invitedBy?.let(inviters::get)
                    MemberDetails(member, user, invitedBy, team)
                },
                next = members.next
            )
        }.asSuccess()
    }
}

// TODO cannot remove last admin of team
class RemoveMember(val teamId: TeamId, val userId: UserId): ServiceAction<MemberDetails>(
    auth = ActionAuth { core, user ->
        core.members.getOrFail(teamId, userId)
            .map { ActionAuth.byTeam(teamId, UserRole.Admin) }
            .flatMap { it(core, user) }
            .recoverIf({user.userId == userId}, { })
    }
) {
    override fun execute(core: Core): Result4k<MemberDetails, AppError> {
        val team = core.teams.getOrFail(teamId).onFailure { return it }
        val user = core.users.getOrFail(userId).onFailure { return it }
        val member = core.members.getOrFail(teamId, userId).onFailure { return it }
        val invitedBy = member.invitedBy?.let(core.users::get)

        core.members -= member
        return MemberDetails(member, user, invitedBy, team).asSuccess()
    }
}

class CreateMember(
    val teamId: TeamId,
    val sender: UserId,
    val emailAddress: EmailAddress,
    val role: UserRole
) : ServiceAction<MemberDetails>(
    auth = ActionAuth.byTeam(teamId, UserRole.Admin)
) {
    override fun execute(core: Core): Result4k<MemberDetails, AppError> {
        val team = core.teams.getOrFail(teamId).onFailure { return it }
        val invitedBy = core.users.getOrFail(sender).onFailure { return it }
        val user = UserService(core).getOrCreate(UserCreateData(
            emailAddress = emailAddress,
            userName = null,
            photoUrl = null
        )).onFailure { return it }

        val existingMember = core.members[teamId, user.userId]
        if (existingMember != null) {
            return memberAlreadyExists(existingMember).asFailure()
        }

        val time = core.clock.instant()
        val member = Member(
            teamId = teamId,
            userId = user.userId,
            role = role,
            invitationExpiresOn = time + core.config.invitationRetention,
            invitedBy = invitedBy.userId
        )
        core.members += member

        return MemberDetails(member, user, invitedBy, team)
            .asSuccess()
            .peek { core.emitEvent(MemberCreatedEvent(
                teamId = teamId,
                eventId = EventId.random(core.random),
                time = time,
                member = it
            )) }
    }
}

class AcceptInvitation(val teamId: TeamId, val userId: UserId): ServiceAction<MemberDetails>(
    auth = ActionAuth { _, user -> begin.failIf({user.userId != userId}, {forbidden})}
) {
    override fun execute(core: Core): Result4k<MemberDetails, AppError> {
        val member = core.members.getOrFail(teamId, userId)
            .failIf(Member::active) { invitationNotFound(teamId, userId) }
            .onFailure { return it }

        val team = core.teams.getOrFail(teamId).onFailure { return it }
        val user = core.users.getOrFail(userId).onFailure { return it }
        val invitedBy = member.invitedBy?.let(core.users::get)

        val updated =  member.copy(invitationExpiresOn = null)
        core.members += updated
        return MemberDetails(updated, user, invitedBy, team).asSuccess()
    }
}

// TODO this must be subject to rate limiting
class ResendInvitation(val teamId: TeamId, val userId: UserId): ServiceAction<MemberDetails>(
    auth = ActionAuth.byTeam(teamId, UserRole.Admin)
) {
    override fun execute(core: Core): Result4k<MemberDetails, AppError> {
        val member = core.members.getOrFail(teamId, userId)
            .failIf({it.active}, { invitationNotFound(teamId, userId) })
            .onFailure { return it }

        val team = core.teams.getOrFail(teamId).onFailure { return it }
        val user = core.users.getOrFail(userId).onFailure { return it }
        val invitedBy = member.invitedBy?.let(core.users::get)
        val details = MemberDetails(member, user, invitedBy, team)

        core.emitEvent(MemberCreatedEvent(
            teamId = teamId,
            eventId = EventId.random(core.random),
            time = core.clock.instant(),
            member = details
        ))

        return details.asSuccess()
    }
}