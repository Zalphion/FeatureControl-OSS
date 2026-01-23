package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.teams.TeamId

data class ConfigEnvironment(
    val teamId: TeamId,
    val appId: AppId,
    val environmentName: EnvironmentName,
    val values: Map<PropertyKey, PropertyValue>
)

data class PropertyValue(
    val type: PropertyType,
    val value: String
)