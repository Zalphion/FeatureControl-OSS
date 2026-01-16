package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.lib.failIfExists
import com.zalphion.featurecontrol.ServiceAction
import com.zalphion.featurecontrol.featureAlreadyExists
import com.zalphion.featurecontrol.members.UserRole
import com.zalphion.featurecontrol.ActionAuth
import com.zalphion.featurecontrol.teams.TeamId
import dev.andrewohara.utils.pagination.Paginator
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.begin
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek

class ListFeatures(val teamId: TeamId, val appId: AppId): ServiceAction<Paginator<Feature, FeatureKey>>(
    auth = ActionAuth.byApplication(teamId, appId)
) {
    override fun execute(core: Core) = core
        .features.list(appId, core.config.pageSize)
        .asSuccess()
}

class GetFeature(val teamId: TeamId, val appId: AppId, val featureKey: FeatureKey): ServiceAction<Feature>(
    auth = ActionAuth.byApplication(teamId, appId),
) {
    override fun execute(core: Core) = core
        .features.getOrFail(appId, featureKey)
}

class CreateFeature(val teamId: TeamId, val appId: AppId, val data: FeatureCreateData): ServiceAction<Feature>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Developer) { getRequirements(data) }
) {
    override fun execute(core: Core) = begin
        .failIfExists(
            test = { core.features[appId, data.featureKey] },
            toFail = { _, feature -> featureAlreadyExists(appId, feature.key) }
        )
        .map { data.toFeature(appId) }
        .peek(core.features::plusAssign)
}

class UpdateFeature(
    val teamId: TeamId,
    val appId: AppId,
    val featureKey: FeatureKey,
    val data: FeatureUpdateData
): ServiceAction<Feature>(
    auth = ActionAuth.byApplication(teamId, appId) { getRequirements(data) }
) {
    override fun execute(core: Core) = core
        .features.getOrFail(appId, featureKey)
        .map { it.update(data) }
        .peek(core.features::plusAssign)
}

class DeleteFeature(val teamId: TeamId, val appId: AppId, val featureKey: FeatureKey): ServiceAction<Feature>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Developer)
) {
    override fun execute(core: Core) = core
        .features.getOrFail(appId, featureKey)
        .peek(core.features::minusAssign)
}