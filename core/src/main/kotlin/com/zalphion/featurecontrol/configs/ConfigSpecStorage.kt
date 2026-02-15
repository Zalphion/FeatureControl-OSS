package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.StorageCompanion
import com.zalphion.featurecontrol.teams.TeamId
import se.ansman.kotshi.JsonSerializable

class ConfigSpecStorage private constructor(private val storage: Repository<StoredConfigSpec, TeamId, AppId>) {
    operator fun get(teamId: TeamId, appId: AppId): ConfigSpec? = storage[teamId, appId]?.toModel()

    operator fun plusAssign(config: ConfigSpec) = storage.save(config.teamId, config.appId, config.toStored())

    fun delete(teamId: TeamId, appId: AppId) = storage.delete(teamId, appId)

    fun getOrEmpty(teamId: TeamId, appId: AppId) = get(teamId, appId) ?: ConfigSpec(teamId, appId, emptyMap())

    companion object: StorageCompanion<ConfigSpecStorage, StoredConfigSpec, TeamId, AppId>(
        documentType = StoredConfigSpec::class,
        groupIdMapping = TeamId.toBiDiMapping(),
        itemIdMapping = AppId.toBiDiMapping(),
        createFn = ::ConfigSpecStorage
    )
}

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

internal fun PropertyType.toStored() = when(this) {
    PropertyType.Boolean -> StoredPropertyType.Boolean
    PropertyType.Number -> StoredPropertyType.Number
    PropertyType.String -> StoredPropertyType.String
    PropertyType.Secret -> StoredPropertyType.Secret
}

internal fun StoredPropertyType.toModel() = when(this) {
    StoredPropertyType.Boolean -> PropertyType.Boolean
    StoredPropertyType.Number -> PropertyType.Number
    StoredPropertyType.String -> PropertyType.String
    StoredPropertyType.Secret -> PropertyType.Secret
}