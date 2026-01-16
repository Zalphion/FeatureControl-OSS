package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.applicationNotFound
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.teams.TeamId
import dev.forkhandles.result4k.asResultOr
import org.http4k.format.AutoMarshalling

class ApplicationStorage private constructor(private val repository: Repository<Application, TeamId, AppId>) {

    fun list(teamId: TeamId, pageSize: Int) = repository.list(teamId, pageSize)
    operator fun get(teamId: TeamId, appId: AppId) = repository[teamId, appId]
    operator fun plusAssign(application: Application) = repository.save(application.teamId, application.appId,application)
    operator fun minusAssign(application: Application) = repository.delete(application.teamId, application.appId)

    fun getOrFail(teamId: TeamId, appId: AppId) =
        get(teamId, appId).asResultOr { applicationNotFound(appId) }

    companion object {
        fun create(storage: Storage, json: AutoMarshalling) = ApplicationStorage(storage.create(
            name = "applications",
            groupIdMapper = TeamId.toBiDiMapping(),
            itemIdMapper = AppId.toBiDiMapping(),
            documentMapper = json.asBiDiMapping<Application>() // TODO DTO
        ))
    }
}