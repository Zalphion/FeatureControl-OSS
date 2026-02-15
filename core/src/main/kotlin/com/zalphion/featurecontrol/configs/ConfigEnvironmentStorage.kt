package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.storage.StorageCompanion
import com.zalphion.featurecontrol.teams.TeamId
import se.ansman.kotshi.JsonSerializable

class ConfigEnvironmentStorage private constructor(
    private val storage: Repository<StoredConfigEnvironment, AppId, EnvironmentName>
) {
    operator fun get(appId: AppId, environmentName: EnvironmentName): ConfigEnvironment? = storage[appId, environmentName]?.toModel()

    fun getOrEmpty(teamId: TeamId, appId: AppId, environmentName: EnvironmentName) = get(appId, environmentName)
        ?.takeIf { it.teamId == teamId }
        ?: ConfigEnvironment(teamId, appId, environmentName, emptyMap())

    operator fun plusAssign(environment: ConfigEnvironment) = storage.save(environment.appId, environment.name, environment.toStored())

    fun delete(appId: AppId, environmentName: EnvironmentName) = storage.delete(appId, environmentName)

    companion object: StorageCompanion<ConfigEnvironmentStorage, StoredConfigEnvironment, AppId, EnvironmentName>(
        groupIdMapping = AppId.toBiDiMapping(),
        itemIdMapping = EnvironmentName.toBiDiMapping(),
        documentType = StoredConfigEnvironment::class,
        createFn = ::ConfigEnvironmentStorage
    )
}


@JsonSerializable
enum class StoredPropertyType { Boolean, Number, String, Secret }

@JsonSerializable
data class StoredConfigEnvironment(
    val teamId: TeamId,
    val appId: AppId,
    val environmentName: EnvironmentName,
    val values: Map<PropertyKey, String>
)

fun ConfigEnvironment.toStored() = StoredConfigEnvironment(
    teamId = teamId,
    appId = appId,
    environmentName = name,
    values = values
)

fun StoredConfigEnvironment.toModel() = ConfigEnvironment(
    teamId = teamId,
    appId = appId,
    name = environmentName,
    values = values
)