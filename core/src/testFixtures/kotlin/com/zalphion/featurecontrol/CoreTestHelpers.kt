package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.CreateFeature
import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.members.ListMembersForUser
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.members.UserRole
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.applications.CreateApplication
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.ApplicationCreateData
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.users.UserCreateData
import com.zalphion.featurecontrol.users.UserService
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.nulls.shouldNotBeNull
import java.net.URI

fun FeatureUpdateData.toCreate(featureKey: FeatureKey) = FeatureCreateData(
    featureKey = featureKey,
    variants = variants.shouldNotBeNull().value,
    defaultVariant = defaultVariant.shouldNotBeNull().value,
    environments = environmentsToUpdate?.value.orEmpty(),
    description = description?.value.orEmpty(),
    extensions = emptyMap()
)

fun CoreTestDriver.createApplication(
    principal: MemberDetails,
    appName: AppName,
    environments: List<Environment> = listOf(dev, prod),
    extensions: Extensions = emptyMap()
) = CreateApplication(
    teamId = principal.team.teamId,
    data = ApplicationCreateData(appName, environments, extensions)
)
    .invoke(principal.user, core)
    .shouldBeSuccess()

fun CoreTestDriver.createFeature(
    principal: MemberDetails,
    application: Application,
    featureKey: FeatureKey,
    variants: Map<Variant, String> = mapOf(off to "off", on to "on"),
    defaultVariant: Variant = off,
    description: String = "a new feature",
    environments: Map<EnvironmentName, FeatureEnvironment> = mapOf(
        devName to alwaysOn,
        prodName to mostlyOff
    ),
    extensions: Extensions = emptyMap()
) = CreateFeature(
    teamId = application.teamId,
    appId = application.appId,
    data = FeatureCreateData(
        featureKey = featureKey,
        variants = variants,
        environments = environments,
        defaultVariant = defaultVariant,
        description = description,
        extensions = extensions
    )
)
    .invoke(principal.user, core)
    .shouldBeSuccess()

fun UserService.create(
    emailAddress: EmailAddress,
    userName: String = emailAddress.value,
    photoUrl: URI? = null,
) = create(UserCreateData(
    emailAddress = emailAddress,
    userName = userName,
    photoUrl = photoUrl
))

fun User.getMyTeam(core: Core) = ListMembersForUser(userId)
    .invoke(this, core)
    .shouldBeSuccess()
    .minByOrNull { it.member.teamId }

fun Member.setRole(core: Core, newRole: UserRole) {
    core.members += copy(role = newRole)
}

fun User.addTo(core: Core, team: Team, role: UserRole = UserRole.Developer): Member {
    return Member(
        teamId = team.teamId,
        userId = userId,
        role = role,
        invitedBy = null,
        invitationExpiresOn = null
    ).also(core.members::plusAssign)
}