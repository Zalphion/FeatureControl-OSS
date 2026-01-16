package com.zalphion.featurecontrol.teams

import com.zalphion.featurecontrol.storage.EmptyKey
import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.teamNotFound
import dev.forkhandles.result4k.asResultOr
import org.http4k.format.AutoMarshalling

class TeamStorage private constructor(private val repository: Repository<Team, TeamId, EmptyKey>) {
    operator fun get(teamId: TeamId): Team? = repository[teamId, EmptyKey.INSTANCE]
    fun batchGet(ids: Collection<TeamId>): Collection<Team> = repository[ids.map { it to EmptyKey.INSTANCE }]

    operator fun plusAssign(team: Team) = repository.save(team.teamId, EmptyKey.INSTANCE,team)
    operator fun minusAssign(team: Team) = repository.delete(team.teamId, EmptyKey.INSTANCE)
    fun getOrFail(teamId: TeamId) = get(teamId).asResultOr { teamNotFound(teamId) }

    companion object {
        fun create(storage: Storage, json: AutoMarshalling) = TeamStorage(storage.create(
            name = "teams",
            groupIdMapper = TeamId.toBiDiMapping(),
            itemIdMapper = EmptyKey.toBiDiMapping(),
            documentMapper = json.asBiDiMapping<Team>() // TODO DTO
        ))
    }
}
