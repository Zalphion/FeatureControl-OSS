package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.storage.EmptyKey
import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import org.http4k.format.AutoMarshalling

class ConfigStorage private constructor(
    private val specs: Repository<ConfigSpec, AppId, EmptyKey>,
    private val environments: Repository<ConfigEnvironment, AppId, EnvironmentName>
) {
    operator fun get(appId: AppId): ConfigSpec? = specs[appId, EmptyKey.INSTANCE]
    operator fun get(appId: AppId, environmentName: EnvironmentName): ConfigEnvironment? = environments[appId, environmentName]

    operator fun plusAssign(config: ConfigSpec) = specs.save(config.appId, EmptyKey.INSTANCE,config)
    operator fun plusAssign(environment: ConfigEnvironment) = environments.save(environment.appId, environment.environmentName, environment)

    operator fun minusAssign(appId: AppId) = specs.delete(appId, EmptyKey.INSTANCE)
    fun delete(appId: AppId, environmentName: EnvironmentName) = environments.delete(appId, environmentName)

    companion object {
        fun create(storage: Storage, json: AutoMarshalling) = ConfigStorage(
            specs = storage.create(
                name = "configs",
                groupIdMapper = AppId.toBiDiMapping(),
                itemIdMapper = EmptyKey.toBiDiMapping(),
                documentMapper = json.asBiDiMapping<ConfigSpec>() // TODO DTO
            ),
            environments = storage.create(
                name = "environments",
                groupIdMapper = AppId.toBiDiMapping(),
                itemIdMapper = EnvironmentName.toBiDiMapping(),
                documentMapper = json.asBiDiMapping<ConfigEnvironment>() // TODO DTO
            )
        )
    }
}

fun ConfigStorage.getOrEmpty( appId: AppId) =
    get(appId) ?: ConfigSpec(appId, emptyMap())

fun ConfigStorage.getOrEmpty(appId: AppId, environmentName: EnvironmentName) =
    get(appId, environmentName) ?: ConfigEnvironment(appId, environmentName, emptyMap())