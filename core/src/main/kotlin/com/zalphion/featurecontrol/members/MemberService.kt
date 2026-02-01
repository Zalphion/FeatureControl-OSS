package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.forbidden
import com.zalphion.featurecontrol.invitationNotFound
import com.zalphion.featurecontrol.memberAlreadyExists
import com.zalphion.featurecontrol.ActionAuth
import com.zalphion.featurecontrol.ServiceAction
import com.zalphion.featurecontrol.events.EventId
import com.zalphion.featurecontrol.events.MemberCreatedEvent
import com.zalphion.featurecontrol.lib.filterItem
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserCreateData
import com.zalphion.featurecontrol.users.UserId
import com.zalphion.featurecontrol.users.UserService
import dev.andrewohara.utils.pagination.Page
import dev.andrewohara.utils.pagination.Paginator
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.begin
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.peek
import dev.forkhandles.values.random
import kotlin.collections.map

// TODO cannot remove last admin of team
class UpdateMember(
    val teamId: TeamId,
    val userId: UserId,
    val data: MemberUpdateData
): ServiceAction<Member>(
    preAuth = ActionAuth.byTeam(teamId, {it.memberUpdate(teamId, userId)}) { getRequirements(data)}
) {
    override fun execute(core: Core) = core
        .members.getOrFail(teamId, userId)
        .map { it.update(data) }
        .peek(core.members::plusAssign)
}

class ListMembersForTeam(val teamId: TeamId): ServiceAction<Paginator<MemberDetails, UserId>>(
    preAuth = ActionAuth.byTeam(teamId, {it.teamRead(teamId)}),
    postAuth = { members, permissions -> members.filterItem { permissions.memberRead(it.member) }}
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
                    MemberDetails(member, user, team)
                },
                next = members.next
            )
        }.asSuccess()
    }
}

class ListMembersForUser(val userId: UserId): ServiceAction<Paginator<MemberDetails, TeamId>>(
    preAuth = ActionAuth { _, permissions -> begin.failIf({!permissions.userUpdate(userId)}, {forbidden})},
    postAuth = { members, permissions -> members.filterItem { permissions.memberRead(it.member) }}
) {
    override fun execute(core: Core): Result4k<Paginator<MemberDetails, TeamId>, AppError> {
        val user = core.users.getOrFail(userId).onFailure { return it }
        return Paginator<MemberDetails, TeamId> { cursor ->
            val members = core.members.list(userId)[cursor]
            val teams = core.teams.batchGet(members.items.map { it.teamId }).associateBy { it.teamId }
            Page(
                items = members.items.mapNotNull { member ->
                    val team = teams[member.teamId] ?: return@mapNotNull null
                    MemberDetails(member, user, team)
                },
                next = members.next
            )
        }.asSuccess()
    }
}

// TODO cannot remove last admin of team
class RemoveMember(val teamId: TeamId, val userId: UserId): ServiceAction<MemberDetails>(
    preAuth = ActionAuth.byTeam(teamId, {it.memberDelete(teamId, userId)})
) {
    override fun execute(core: Core): Result4k<MemberDetails, AppError> {
        val team = core.teams.getOrFail(teamId).onFailure { return it }
        val user = core.users.getOrFail(userId).onFailure { return it }
        val member = core.members.getOrFail(teamId, userId).onFailure { return it }

        core.members -= member
        return MemberDetails(member, user, team).asSuccess()
    }
}

class InviteUser(
    val teamId: TeamId,
    val sender: UserId,
    val data: MemberCreateData
) : ServiceAction<MemberDetails>(
    preAuth = ActionAuth.byTeam(teamId, {it.memberCreate(teamId)}) { getRequirements(data) }
) {
    override fun execute(core: Core): Result4k<MemberDetails, AppError> {
        val team = core.teams.getOrFail(teamId).onFailure { return it }
        val user = UserService(core).getOrCreate(UserCreateData(
            emailAddress = data.emailAddress,
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
            invitationExpiresOn = time + core.config.invitationRetention,
            invitedBy = sender,
            extensions = data.extensions
        )
        core.members += member

        return MemberDetails(member, user, team)
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
    preAuth = ActionAuth { _, permissions -> begin.failIf({!permissions.userUpdate(userId)}, {forbidden})}
) {
    override fun execute(core: Core): Result4k<MemberDetails, AppError> {
        val member = core.members.getOrFail(teamId, userId)
            .failIf(Member::active) { invitationNotFound(teamId, userId) }
            .onFailure { return it }

        val team = core.teams.getOrFail(teamId).onFailure { return it }
        val user = core.users.getOrFail(userId).onFailure { return it }

        val updated =  member.copy(invitationExpiresOn = null)
        core.members += updated
        return MemberDetails(updated, user, team).asSuccess()
    }
}

// TODO this must be subject to rate limiting
class ResendInvitation(val teamId: TeamId, val userId: UserId): ServiceAction<MemberDetails>(
    preAuth = ActionAuth.byTeam(teamId, {it.memberUpdate(teamId, userId)})
) {
    override fun execute(core: Core): Result4k<MemberDetails, AppError> {
        val member = core.members.getOrFail(teamId, userId)
            .failIf({it.active}, { invitationNotFound(teamId, userId) })
            .onFailure { return it }

        val team = core.teams.getOrFail(teamId).onFailure { return it }
        val user = core.users.getOrFail(userId).onFailure { return it }
        val details = MemberDetails(member, user, team)

        core.emitEvent(MemberCreatedEvent(
            teamId = teamId,
            eventId = EventId.random(core.random),
            time = core.clock.instant(),
            member = details
        ))

        return details.asSuccess()
    }
}