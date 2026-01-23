package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.applicationNotFound
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.mapItem
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.teams.TeamId
import dev.forkhandles.result4k.asResultOr
import org.http4k.format.AutoMarshalling
import se.ansman.kotshi.JsonSerializable

class ApplicationStorage private constructor(private val repository: Repository<StoredApplication, TeamId, AppId>) {

    fun list(teamId: TeamId, pageSize: Int) = repository.list(teamId, pageSize).mapItem { it.toModel() }
    operator fun get(teamId: TeamId, appId: AppId) = repository[teamId, appId]?.toModel()
    operator fun plusAssign(application: Application) = repository.save(application.teamId, application.appId,application.toStored())
    operator fun minusAssign(application: Application) = repository.delete(application.teamId, application.appId)

    fun getOrFail(teamId: TeamId, appId: AppId) =
        get(teamId, appId).asResultOr { applicationNotFound(appId) }

    companion object {
        fun create(storageDriver: StorageDriver, json: AutoMarshalling) = ApplicationStorage(storageDriver.create(
            name = "applications",
            groupIdMapper = TeamId.toBiDiMapping(),
            itemIdMapper = AppId.toBiDiMapping(),
            documentMapper = json.asBiDiMapping()
        ))
    }
}

@JsonSerializable
data class StoredApplication(
    val teamId: TeamId,
    val appId: AppId,
    val appName: AppName,
    val environments: List<StoredEnvironment>,
    val extensions: Extensions
)

private fun StoredApplication.toModel() = Application(
    teamId = teamId,
    appId = appId,
    appName = appName,
    environments = environments.map { it.toModel() },
    extensions = extensions
)

private fun Application.toStored() = StoredApplication(
    teamId = teamId,
    appId = appId,
    appName = appName,
    environments = environments.map { it.toStored() },
    extensions = extensions
)

@JsonSerializable
data class StoredEnvironment(
    val name: EnvironmentName,
    val description: String,
    val colour: Colour,
    val extensions: Extensions
)

private fun Environment.toStored() = StoredEnvironment(
    name = name,
    description = description,
    colour = colour,
    extensions = extensions
)

private fun StoredEnvironment.toModel() = Environment(
    name = name,
    description = description,
    colour = colour,
    extensions = extensions
)