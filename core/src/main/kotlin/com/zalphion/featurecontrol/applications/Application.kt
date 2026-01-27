package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.environmentNotFound
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.plugins.Extendable
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.teams.TeamId
import dev.forkhandles.result4k.asResultOr

data class Application(
    val teamId: TeamId,
    val appId: AppId,
    val appName: AppName,
    val environments: List<Environment>,
    override val extensions: Extensions
): Extendable<Application> {
    fun getOrFail(name: EnvironmentName) = environments
        .firstOrNull { it.name == name }
        .asResultOr { environmentNotFound(appId, name) }

    override fun with(extensions: Extensions) = copy(extensions = this.extensions + extensions)
}