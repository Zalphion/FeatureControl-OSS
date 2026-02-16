package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.members.Member
import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.ApplicationCreateData
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.configs.Property
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.teams.Team
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.users.UserCreateData
import com.zalphion.featurecontrol.users.UserService
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.nulls.shouldNotBeNull
import org.http4k.core.Uri

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
) = core.applications.create(
    teamId = principal.team.teamId,
    data = ApplicationCreateData(appName, environments, extensions)
)
    .invoke(core, principal.user)
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
) = core.features.create(
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
    .invoke(core, principal.user)
    .shouldBeSuccess()

fun CoreTestDriver.updateConfigSpec(
    principal: MemberDetails,
    application: Application,
    properties: Map<PropertyKey, Property>
) = core.configs.updateSpec(
    teamId = application.teamId,
    appId = application.appId,
    properties = properties
).invoke(core, principal.user).shouldBeSuccess()

fun CoreTestDriver.updateConfigEnvironment(
    principal: MemberDetails,
    application: Application,
    environmentName: EnvironmentName,
    values: Map<PropertyKey, String>
) = core.configs.updateEnvironment(
    teamId = application.teamId,
    appId = application.appId,
    environmentName = environmentName,
    data = values
).invoke(core, principal.user).shouldBeSuccess()

fun UserService.create(
    emailAddress: EmailAddress,
    userName: String? = null,
    photoUrl: Uri? = null,
) = create(UserCreateData(
    emailAddress = emailAddress,
    userName = userName,
    photoUrl = photoUrl
))

fun User.getMyTeam(core: Core): MemberDetails = core.memberStorage.list(userId)
    .find { it.invitedBy == null }
    .shouldNotBeNull()
    .let { MemberDetails(
        member = it,
        user = this,
        team = core.teamStorage[it.teamId].shouldNotBeNull()
    ) }

fun User.addTo(core: Core, team: Team) = Member(
    teamId = team.teamId,
    userId = userId,
    invitedBy = null,
    invitationExpiresOn = null,
    extensions = emptyMap()
).also(core.memberStorage::plusAssign)

fun <T: Any> ServiceAction<T>.invoke(core: Core, user: User): Result4k<T, AppError> {
    val permissions = core.permissions.create(core, user.userId).shouldNotBeNull()
    return invoke(permissions)
}