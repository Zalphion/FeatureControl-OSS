package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.crypto.Encryption
import com.zalphion.featurecontrol.crypto.aesGcm
import com.zalphion.featurecontrol.environmentNotFound
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.members.UserRole
import com.zalphion.featurecontrol.ActionAuth
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.ServiceAction
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.environmentExistsOrFail
import com.zalphion.featurecontrol.applications.environmentNames
import com.zalphion.featurecontrol.teams.TeamId
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.peek

class GetConfigSpec(val teamId: TeamId, val appId: AppId): ServiceAction<ConfigSpec>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Developer)
) {
    override fun execute(core: Core) = core
        .apps.getOrFail(teamId, appId)
        .map { core.configs.getOrEmpty(appId) }
}

class UpdateConfigSpec(
    val teamId: TeamId,
    val appId: AppId,
    val properties: Map<PropertyKey, Property>
): ServiceAction<ConfigSpec>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Developer)
) {
    override fun execute(core: Core) = core
        .apps.getOrFail(teamId, appId)
        .map { core.configs.getOrEmpty(appId) }
        .map { config -> config.copy(properties = properties) }
        .peek(core.configs::plusAssign)
}

/**
 * Secret values will not be decoded
 */
class GetConfigEnvironment(
    val teamId: TeamId,
    val appId: AppId,
    val environmentName: EnvironmentName
): ServiceAction<ConfigEnvironment>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Developer)
) {
    override fun execute(core: Core) = core
        .apps.getOrFail(teamId, appId)
        .failIf({ environmentName !in it.environmentNames }, { environmentNotFound(appId, environmentName)})
        .map { core.configs.getOrEmpty(appId, environmentName) }
}

class UpdateConfigEnvironment(
    val teamId: TeamId,
    val appId: AppId,
    val environmentName: EnvironmentName,
    val data: Map<PropertyKey, String>
): ServiceAction<ConfigEnvironment>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Developer)
) {
    override fun execute(core: Core) = this
        .processValues(core)
        .map { it.second }
        .peek(core.configs::plusAssign)

    fun processValues(core: Core): Result4k<Pair<Application, ConfigEnvironment>, AppError> {
        val application = core.apps
            .getOrFail(teamId, appId)
            .flatMap { it.environmentExistsOrFail(environmentName) }
            .onFailure { return it }

        val config = core.configs.getOrEmpty(appId)
        val encryption = core.encryption(appId, environmentName)

        val newValues = data.mapNotNull { (key, value) ->
            val property = config.getOrFail(key).onFailure { return it }
            val processedValue = when(property.type) {
                PropertyType.Secret -> encryption.encrypt(value.trim()).toHexString() // TODO should derive a key per app/env
                else -> value.trim()
            }
            if (processedValue.isNotBlank()) key to property.toValue(processedValue) else null
        }.toMap()

        return (application to ConfigEnvironment(appId, environmentName, newValues)).asSuccess()
    }
}

private fun Core.encryption(
    appId: AppId,
    environmentName: EnvironmentName
) = Encryption.aesGcm(
    appSecret = config.appSecret,
    keySalt = "$appId:$environmentName".encodeToByteArray(),
    usage = "configValues",
    random = random
)