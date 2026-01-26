package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.teams.TeamId
import org.http4k.format.AutoMarshalling
import se.ansman.kotshi.JsonSerializable

class ConfigStorage private constructor(
    private val specs: Repository<StoredConfigSpec, TeamId, AppId>,
    private val environments: Repository<StoredConfigEnvironment, AppId, EnvironmentName>
) {
    operator fun get(teamId: TeamId, appId: AppId): ConfigSpec? = specs[teamId, appId]?.toModel()
    operator fun get(appId: AppId, environmentName: EnvironmentName): ConfigEnvironment? = environments[appId, environmentName]?.toModel()

    operator fun plusAssign(config: ConfigSpec) = specs.save(config.teamId, config.appId, config.toStored())
    operator fun plusAssign(environment: ConfigEnvironment) = environments.save(environment.appId, environment.environmentName, environment.toStored())

    fun delete(teamId: TeamId, appId: AppId) = specs.delete(teamId, appId)
    fun delete(appId: AppId, environmentName: EnvironmentName) = environments.delete(appId, environmentName)

    companion object {
        fun create(baseStorageName: String, versionsStorageName: String, storageDriver: StorageDriver, json: AutoMarshalling) = ConfigStorage(
            specs = storageDriver.create(
                name = baseStorageName,
                groupIdMapper = TeamId.toBiDiMapping(),
                itemIdMapper = AppId.toBiDiMapping(),
                documentMapper = json.asBiDiMapping()
            ),
            environments = storageDriver.create(
                name = versionsStorageName,
                groupIdMapper = AppId.toBiDiMapping(),
                itemIdMapper = EnvironmentName.toBiDiMapping(),
                documentMapper = json.asBiDiMapping()
            )
        )
    }
}

fun ConfigStorage.getOrEmpty(teamId: TeamId, appId: AppId) =
    get(teamId, appId) ?: ConfigSpec(teamId, appId, emptyMap())

fun ConfigStorage.getOrEmpty(teamId: TeamId, appId: AppId, environmentName: EnvironmentName) =
    get(appId, environmentName)
    ?.takeIf { it.teamId == teamId }
    ?: ConfigEnvironment(teamId, appId, environmentName, emptyMap())

@JsonSerializable
data class StoredConfigSpec(
    val teamId: TeamId,
    val appId: AppId,
    val properties: Map<PropertyKey, StoredProperty>
)

@JsonSerializable
data class StoredProperty(
    val description: String,
    val type: StoredPropertyType,
)

@JsonSerializable
enum class StoredPropertyType { Boolean, Number, String, Secret }

@JsonSerializable
data class StoredConfigEnvironment(
    val teamId: TeamId,
    val appId: AppId,
    val environmentName: EnvironmentName,
    val values: Map<PropertyKey, StoredPropertyValue>
)

@JsonSerializable
data class StoredPropertyValue(
    val type: StoredPropertyType,
    val value: String
)

private fun ConfigSpec.toStored() = StoredConfigSpec(
    teamId = teamId,
    appId = appId,
    properties = properties.mapValues { (_, value) ->
        StoredProperty(
            description = value.description,
            type = value.type.toStored()
        )
    }
)

private fun StoredConfigSpec.toModel() = ConfigSpec(
    teamId = teamId,
    appId = appId,
    properties = properties.mapValues { (_, value) ->
        Property(
            description = value.description,
            type = value.type.toModel()
        )
    }
)

fun ConfigEnvironment.toStored() = StoredConfigEnvironment(
    teamId = teamId,
    appId = appId,
    environmentName = environmentName,
    values = values.mapValues { (_, value) ->
        StoredPropertyValue(
            type = value.type.toStored(),
            value = value.value
        )
    }
)

fun StoredConfigEnvironment.toModel() = ConfigEnvironment(
    teamId = teamId,
    appId = appId,
    environmentName = environmentName,
    values = values.mapValues { (_, value) ->
        PropertyValue(
            type = value.type.toModel(),
            value = value.value
        )
    }
)

private fun PropertyType.toStored() = when(this) {
    PropertyType.Boolean -> StoredPropertyType.Boolean
    PropertyType.Number -> StoredPropertyType.Number
    PropertyType.String -> StoredPropertyType.String
    PropertyType.Secret -> StoredPropertyType.Secret
}

private fun StoredPropertyType.toModel() = when(this) {
    StoredPropertyType.Boolean -> PropertyType.Boolean
    StoredPropertyType.Number -> PropertyType.Number
    StoredPropertyType.String -> PropertyType.String
    StoredPropertyType.Secret -> PropertyType.Secret
}