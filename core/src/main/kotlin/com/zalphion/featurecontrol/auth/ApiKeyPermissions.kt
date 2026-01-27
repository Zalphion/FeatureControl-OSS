package com.zalphion.featurecontrol.auth

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.teams.TeamId

fun Permissions.Companion.apiKey(data: EnginePrincipal) = object : ZeroTrustPermissions() {
    override fun featureRead(teamId: TeamId, appId: AppId, featureKey: FeatureKey, environment: EnvironmentName) =
        data.appId == appId && data.environmentName == environment
}