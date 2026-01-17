package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.storage.EmptyKey
import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.web.appIdLens
import org.http4k.format.AutoMarshalling
import se.ansman.kotshi.JsonSerializable

class ConfigStorage private constructor(
    private val specs: Repository<StoredConfigSpec, AppId, EmptyKey>,
    private val environments: Repository<StoredConfigEnvironment, AppId, EnvironmentName>
) {
    operator fun get(appId: AppId): ConfigSpec? = specs[appId, EmptyKey.INSTANCE]?.toModel()
    operator fun get(appId: AppId, environmentName: EnvironmentName): ConfigEnvironment? = environments[appId, environmentName]?.toModel()

    operator fun plusAssign(config: ConfigSpec) = specs.save(config.appId, EmptyKey.INSTANCE,config.toStored())
    operator fun plusAssign(environment: ConfigEnvironment) = environments.save(environment.appId, environment.environmentName, environment.toStored())

    operator fun minusAssign(appId: AppId) = specs.delete(appId, EmptyKey.INSTANCE)
    fun delete(appId: AppId, environmentName: EnvironmentName) = environments.delete(appId, environmentName)

    companion object {
        fun create(storage: Storage, json: AutoMarshalling) = ConfigStorage(
            specs = storage.create(
                name = "configs",
                groupIdMapper = AppId.toBiDiMapping(),
                itemIdMapper = EmptyKey.toBiDiMapping(),
                documentMapper = json.asBiDiMapping()
            ),
            environments = storage.create(
                name = "environments",
                groupIdMapper = AppId.toBiDiMapping(),
                itemIdMapper = EnvironmentName.toBiDiMapping(),
                documentMapper = json.asBiDiMapping()
            )
        )
    }
}

fun ConfigStorage.getOrEmpty( appId: AppId) =
    get(appId) ?: ConfigSpec(appId, emptyMap())

fun ConfigStorage.getOrEmpty(appId: AppId, environmentName: EnvironmentName) =
    get(appId, environmentName) ?: ConfigEnvironment(appId, environmentName, emptyMap())

@JsonSerializable
data class StoredConfigSpec(
    val appId: AppId,
    val properties: Map<PropertyKey, StoredProperty>
)

@JsonSerializable
data class StoredProperty(
    val description: String,
    val group: String?,
    val type: StoredPropertyType,
)

@JsonSerializable
enum class StoredPropertyType { Boolean, Number, String, Secret }

@JsonSerializable
data class StoredConfigEnvironment(
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
    appId = appId,
    properties = properties.mapValues { (_, value) ->
        StoredProperty(
            description = value.description,
            group = value.group,
            type = value.type.toStored()
        )
    }
)

private fun StoredConfigSpec.toModel() = ConfigSpec(
    appId = appId,
    properties = properties.mapValues { (_, value) ->
        Property(
            description = value.description,
            group = value.group,
            type = value.type.toModel()
        )
    }
)

private fun ConfigEnvironment.toStored() = StoredConfigEnvironment(
    appId = appId,
    environmentName = environmentName,
    values = values.mapValues { (_, value) ->
        StoredPropertyValue(
            type = value.type.toStored(),
            value = value.value
        )
    }
)

private fun StoredConfigEnvironment.toModel() = ConfigEnvironment(
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