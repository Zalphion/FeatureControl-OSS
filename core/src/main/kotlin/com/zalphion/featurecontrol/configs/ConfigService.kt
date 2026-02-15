package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.crypto.Encryption
import com.zalphion.featurecontrol.crypto.aesGcm
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.ApplicationStorage
import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.lib.peekOrFail
import com.zalphion.featurecontrol.preAuth
import com.zalphion.featurecontrol.teams.TeamId
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.peek
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.random.Random

class ConfigService(
    private val appSecret: AppSecret,
    private val random: Random,
    private val applications: ApplicationStorage,
    private val specs: ConfigSpecStorage,
    private val environments: ConfigEnvironmentStorage
) {
    private fun encryption(
        appId: AppId,
        environmentName: EnvironmentName
    ) = Encryption.aesGcm(
        appSecret = appSecret,
        keySalt = "$appId:$environmentName".encodeToByteArray(),
        usage = "config",
        random = random
    )

    fun getSpec(teamId: TeamId, appId: AppId) = preAuth {
        it.configRead(teamId, appId)
    }.flatMap {
        applications.getOrFail(teamId, appId)
            .map { specs.getOrEmpty(teamId, appId) }
    }

    fun updateSpec(teamId: TeamId, appId: AppId, properties: Map<PropertyKey, Property>) = preAuth {
        it.configUpdate(teamId, appId)
    }.flatMap {
        applications.getOrFail(teamId, appId)
            .map { specs.getOrEmpty(teamId, appId) }
            .map { it.copy(properties = properties) }
            .peek(specs::plusAssign)
    }

    /**
     * Secret values will not be decoded
     */
    fun getEnvironment(teamId: TeamId, appId: AppId, environmentName: EnvironmentName) = preAuth {
        it.configRead(teamId, appId, environmentName)
    }.flatMap {
        applications.getOrFail(teamId, appId)
            .peekOrFail { it.getOrFail(environmentName) }
            .map { environments.getOrEmpty(teamId, appId, environmentName) }
    }

    fun updateEnvironment(teamId: TeamId, appId: AppId, environmentName: EnvironmentName, data: Map<PropertyKey, String>) = preAuth {
        it.configUpdate(teamId, appId, environmentName)
    }.flatMap {
        applications
            .getOrFail(teamId, appId)
            .peekOrFail { it.getOrFail(environmentName) }
            .flatMap { processValues(teamId, appId, environmentName, data) }
            .peek(environments::plusAssign)
    }

    fun processValues(
        teamId: TeamId, appId: AppId, environment: EnvironmentName,
        data: Map<PropertyKey, String>
    ): Result4k<ConfigEnvironment, AppError> {
        val config = specs.getOrEmpty(teamId, appId)
        val encryption = encryption(appId, environment)

        val processed = data.mapNotNull { (key, value) ->
            val property = config.getOrFail(key).onFailure { return it }
            val processedValue = when(property.type) {
                PropertyType.Secret -> encryption.encrypt(value.trim()).toHexString() // TODO should derive a key per app/env
                else -> value.trim()
            }
            if (processedValue.isNotBlank()) key to processedValue else null
        }.toMap()

        return ConfigEnvironment(teamId, appId, environment, processed).asSuccess()
    }
}