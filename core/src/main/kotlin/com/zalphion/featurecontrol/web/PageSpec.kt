package com.zalphion.featurecontrol.web

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserId
import org.http4k.core.Uri
import org.http4k.core.appendToPath

data class PageSpec(val name: String, val icon: String) {
    companion object {
        val applications = PageSpec("Applications", "icon: album")
        val features = PageSpec("Features", "icon: cog")
        val config = PageSpec("Config", "icon: file-text")
        val members = PageSpec("Members", "icon: users")
        val invitations = PageSpec("Invitations", "icon: users")
    }
}
data class PageLink(
    val spec: PageSpec,
    val uri: Uri,
    val enabled: Boolean = true,
    val tooltip: String? = null,
)

fun teamUri(teamId: TeamId) = Uri.of("/teams/$teamId")
fun membersUri(teamId: TeamId) = teamUri(teamId).appendToPath("members")
fun membersUri(teamId: TeamId, userId: UserId) = membersUri(teamId).appendToPath(userId.value)
fun invitationsUri(teamId: TeamId) = teamUri(teamId).appendToPath("invitations")
fun invitationsUri(teamId: TeamId, userId: UserId) = invitationsUri(teamId).appendToPath(userId.value)
fun applicationsUri(teamId: TeamId) = teamUri(teamId).appendToPath("applications")
fun applicationUri(teamId: TeamId, appId: AppId) = teamUri(teamId).appendToPath("/applications/$appId")

fun featuresUri(teamId: TeamId, appId: AppId) = applicationUri(teamId, appId).appendToPath("features")
fun featureUri(teamId: TeamId, appId: AppId, featureKey: FeatureKey) = featuresUri(teamId, appId).appendToPath(featureKey.value)
fun featureUri(teamId: TeamId, appId: AppId, featureKey: FeatureKey, environmentName: EnvironmentName) =
    featureUri(teamId, appId, featureKey).appendToPath("environments/$environmentName")

fun configUri(teamId: TeamId, appId: AppId) = applicationUri(teamId, appId).appendToPath("config")
fun configUri(teamId: TeamId, appId: AppId, environmentName: EnvironmentName) = configUri(teamId, appId).appendToPath(environmentName.value)

fun Application.uri() = applicationUri(teamId, appId)
fun Application.featuresUri() = featuresUri(teamId, appId)
fun Feature.uri() = featureUri(teamId, appId, key)
fun Feature.uri(environmentName: EnvironmentName) = featureUri(teamId, appId, key, environmentName)
fun ConfigSpec.uri() = configUri(teamId, appId)
fun ConfigEnvironment.uri() = configUri(teamId, appId, name)