package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.memberNotFound
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserId
import dev.andrewohara.utils.pagination.Paginator
import dev.forkhandles.result4k.asResultOr
import org.http4k.format.AutoMarshalling

class MemberStorage private constructor(private val repository: Repository<Member, TeamId, UserId>) {
    operator fun plusAssign(member: Member) = repository.save(member.teamId, member.userId, member)
    fun list(userId: UserId, pageSize: Int): Paginator<Member, TeamId> = repository.listInverse(userId, pageSize)
    fun list(teamId: TeamId, pageSize: Int): Paginator<Member, UserId> = repository.list(teamId, pageSize)
    operator fun get(teamId: TeamId, userId: UserId): Member? = repository[teamId, userId]
    operator fun minusAssign(member: Member) = repository.delete(member.teamId, member.userId)

    fun getOrFail(teamId: TeamId, userId: UserId) = get(teamId, userId).asResultOr { memberNotFound(teamId, userId) }

    companion object {
        fun create(storage: Storage, json: AutoMarshalling) = MemberStorage(storage.create(
            name = "members",
            groupIdMapper = TeamId.toBiDiMapping(),
            itemIdMapper = UserId.toBiDiMapping(),
            documentMapper = json.asBiDiMapping<Member>() // TODO DTO
        ))
    }
}