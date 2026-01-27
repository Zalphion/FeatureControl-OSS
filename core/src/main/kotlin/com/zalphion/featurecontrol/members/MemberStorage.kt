package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.lib.mapItem
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.memberNotFound
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.storage.StorageCompanion
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserId
import dev.andrewohara.utils.pagination.Paginator
import dev.forkhandles.result4k.asResultOr
import se.ansman.kotshi.JsonSerializable
import java.time.Instant

class MemberStorage private constructor(private val repository: Repository<StoredMember, TeamId, UserId>) {
    operator fun plusAssign(member: Member) = repository.save(member.teamId, member.userId, member.toStored())
    fun list(userId: UserId): Paginator<Member, TeamId> = repository.listInverse(userId).mapItem { it.toModel() }
    fun list(teamId: TeamId): Paginator<Member, UserId> = repository.list(teamId).mapItem { it.toModel() }
    operator fun get(teamId: TeamId, userId: UserId): Member? = repository[teamId, userId]?.toModel()
    operator fun minusAssign(member: Member) = repository.delete(member.teamId, member.userId)

    fun getOrFail(teamId: TeamId, userId: UserId) = get(teamId, userId).asResultOr { memberNotFound(teamId, userId) }

    companion object: StorageCompanion<MemberStorage, StoredMember, TeamId, UserId>(
        documentType = StoredMember::class,
        groupIdMapping = TeamId.toBiDiMapping(),
        itemIdMapping = UserId.toBiDiMapping(),
        createFn = ::MemberStorage
    )
}

@JsonSerializable
data class StoredMember(
    val teamId: TeamId,
    val userId: UserId,
    val invitedBy: UserId?,
    val invitationExpiresOn: Instant?,
    val extensions: Extensions
)

@JsonSerializable
enum class StoredUserRole { Tester, Developer, Admin  }

private fun Member.toStored() = StoredMember(
    teamId = teamId,
    userId = userId,
    invitedBy = invitedBy,
    invitationExpiresOn = invitationExpiresOn,
    extensions = extensions
)

private fun StoredMember.toModel() = Member(
    teamId = teamId,
    userId = userId,
    invitedBy = invitedBy,
    invitationExpiresOn = invitationExpiresOn,
    extensions = extensions
)

