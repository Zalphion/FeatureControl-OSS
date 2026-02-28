package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.plugins.Extendable
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.teams.TeamId
import dev.forkhandles.result4k.asResultOr

data class ConfigSpec(
    val teamId: TeamId,
    val appId: AppId,
    val properties: Map<PropertyKey, Property>,
    override val extensions: Extensions
): Extendable<ConfigSpec> {
    fun getOrFail(key: PropertyKey) = properties[key].asResultOr { propertyNotFound(appId, key) }

    override fun with(extensions: Extensions) = copy(
        extensions = this.extensions + extensions
    )
}