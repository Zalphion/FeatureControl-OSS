package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.plugins.Extendable
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.teams.TeamId

data class ConfigEnvironment(
    val teamId: TeamId,
    val appId: AppId,
    val name: EnvironmentName,
    val values: Map<PropertyKey, String>,
    override val extensions: Extensions
): Extendable<ConfigEnvironment> {
    override fun with(extensions: Extensions) = copy(
        extensions = this.extensions + extensions
    )
}