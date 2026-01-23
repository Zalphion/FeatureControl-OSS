package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.mapItem
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.memberNotFound
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserId
import dev.andrewohara.utils.pagination.Paginator
import dev.forkhandles.result4k.asResultOr
import org.http4k.format.AutoMarshalling
import se.ansman.kotshi.JsonSerializable
import java.time.Instant

class MemberStorage private constructor(private val repository: Repository<StoredMember, TeamId, UserId>) {
    operator fun plusAssign(member: Member) = repository.save(member.teamId, member.userId, member.toStored())
    fun list(userId: UserId, pageSize: Int): Paginator<Member, TeamId> = repository.listInverse(userId, pageSize).mapItem { it.toModel() }
    fun list(teamId: TeamId, pageSize: Int): Paginator<Member, UserId> = repository.list(teamId, pageSize).mapItem { it.toModel() }
    operator fun get(teamId: TeamId, userId: UserId): Member? = repository[teamId, userId]?.toModel()
    operator fun minusAssign(member: Member) = repository.delete(member.teamId, member.userId)

    fun getOrFail(teamId: TeamId, userId: UserId) = get(teamId, userId).asResultOr { memberNotFound(teamId, userId) }

    companion object {
        fun create(storageDriver: StorageDriver, json: AutoMarshalling) = MemberStorage(storageDriver.create(
            name = "members",
            groupIdMapper = TeamId.toBiDiMapping(),
            itemIdMapper = UserId.toBiDiMapping(),
            documentMapper = json.asBiDiMapping()
        ))
    }
}

@JsonSerializable
data class StoredMember(
    val teamId: TeamId,
    val userId: UserId,
    val invitedBy: UserId?,
    val role: StoredUserRole,
    val invitationExpiresOn: Instant?
)

@JsonSerializable
enum class StoredUserRole { Tester, Developer, Admin  }

private fun Member.toStored() = StoredMember(
    teamId = teamId,
    userId = userId,
    invitedBy = invitedBy,
    role = when(role) {
        UserRole.Admin -> StoredUserRole.Admin
        UserRole.Developer -> StoredUserRole.Developer
        UserRole.Tester -> StoredUserRole.Tester
    },
    invitationExpiresOn = invitationExpiresOn
)

private fun StoredMember.toModel() = Member(
    teamId = teamId,
    userId = userId,
    role = when(role) {
        StoredUserRole.Admin -> UserRole.Admin
        StoredUserRole.Developer -> UserRole.Developer
        StoredUserRole.Tester -> UserRole.Tester
    },
    invitedBy = invitedBy,
    invitationExpiresOn = invitationExpiresOn
)

