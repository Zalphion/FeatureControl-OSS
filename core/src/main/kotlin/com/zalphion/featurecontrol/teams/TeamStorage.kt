package com.zalphion.featurecontrol.teams

import com.zalphion.featurecontrol.storage.EmptyKey
import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.storage.StorageCompanion
import com.zalphion.featurecontrol.teamNotFound
import dev.forkhandles.result4k.asResultOr
import se.ansman.kotshi.JsonSerializable

class TeamStorage private constructor(private val repository: Repository<StoredTeam, TeamId, EmptyKey>) {
    operator fun get(teamId: TeamId): Team? = repository[teamId, EmptyKey.INSTANCE]?.toModel()
    fun batchGet(ids: Collection<TeamId>): Collection<Team> = repository[ids.map { it to EmptyKey.INSTANCE }].map { it.toModel() }

    operator fun plusAssign(team: Team) = repository.save(team.teamId, EmptyKey.INSTANCE,team.toStored())
    operator fun minusAssign(team: Team) = repository.delete(team.teamId, EmptyKey.INSTANCE)
    fun getOrFail(teamId: TeamId) = get(teamId).asResultOr { teamNotFound(teamId) }

    companion object: StorageCompanion<TeamStorage, StoredTeam, TeamId, EmptyKey>(
        documentType = StoredTeam::class,
        groupIdMapping = TeamId.toBiDiMapping(),
        itemIdMapping = EmptyKey.toBiDiMapping(),
        createFn = ::TeamStorage
    )
}

@JsonSerializable
data class StoredTeam(
    val teamId: TeamId,
    val teamName: TeamName
)

private fun Team.toStored() = StoredTeam(teamId, teamName)
private fun StoredTeam.toModel() = Team(teamId, teamName)
