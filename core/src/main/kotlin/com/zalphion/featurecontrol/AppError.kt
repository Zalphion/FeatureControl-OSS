package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.auth.Entitlement
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserId

data class AppError(
    val messageCode: String,
    val messageArguments: Map<String, String> = emptyMap()
)

fun applicationNotFound(appId: AppId) = AppError(
    messageCode = "applicationNotFound",
    messageArguments = mapOf(
        "applicationId" to appId.value
    )
)

fun applicationNotEmpty(appId: AppId) = AppError(
    messageCode = "applicationNotEmpty",
    messageArguments = mapOf(
        "applicationId" to appId.value
    )
)

fun environmentNotFound(appId: AppId, environmentName: EnvironmentName) = AppError(
    messageCode = "environmentNotFound",
    messageArguments = mapOf(
        "applicationId" to appId.value,
        "environmentName" to environmentName.value
    )
)

fun featureAlreadyExists(appId: AppId, featureKey: FeatureKey) = AppError(
    messageCode = "toggleAlreadyExists",
    messageArguments = mapOf(
        "applicationId" to appId.value,
        "featureKey" to featureKey.value
    )
)

fun featureNotFound(appId: AppId, featureKey: FeatureKey) = AppError(
    messageCode = "toggleNotFound",
    messageArguments = mapOf(
        "applicationId" to appId.value,
        "featureKey" to featureKey.value
    )
)

fun teamNotFound(teamId: TeamId) = AppError(
    messageCode = "teamNotFound",
    messageArguments = mapOf(
        "teamId" to teamId.value
    )
)

fun teamNotEmpty(team: Team) = AppError(
    messageCode = "teamNotEmpty",
    messageArguments = mapOf(
        "teamId" to team.teamId.value,
        "teamName" to team.teamName.value
    )
)

fun invitationNotFound(teamId: TeamId, userId: UserId) = AppError(
    messageCode = "invitationNotFound",
    messageArguments = mapOf(
        "teamId" to teamId.value,
        "userId" to userId.value,
    )
)

fun memberAlreadyExists(member: Member) = AppError(
    messageCode = "memberAlreadyExists",
    messageArguments = mapOf(
        "teamId" to member.teamId.value,
        "userId" to member.userId.value,
    )
)

fun memberNotFound(teamId: TeamId, userId: UserId) = AppError(
    messageCode = "memberNotFound",
    messageArguments = mapOf(
        "teamId" to teamId.value,
        "userId" to userId.value,
    )
)

fun missingEntitlements(vararg entitlements: Entitlement) = missingEntitlements(entitlements.toList())

fun missingEntitlements(entitlements: Collection<Entitlement>) = AppError(
    messageCode = "missingEntitlement",
    messageArguments = entitlements.associate { it.toString() to "true" }
)

val forbidden = AppError("forbidden")