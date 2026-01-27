package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.lib.failIfExists
import com.zalphion.featurecontrol.ServiceAction
import com.zalphion.featurecontrol.featureAlreadyExists
import com.zalphion.featurecontrol.ActionAuth
import com.zalphion.featurecontrol.lib.filterItem
import com.zalphion.featurecontrol.teams.TeamId
import dev.andrewohara.utils.pagination.Paginator
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek

class ListFeatures(val teamId: TeamId, val appId: AppId): ServiceAction<Paginator<Feature, FeatureKey>>(
    preAuth = ActionAuth.byApplication(teamId, appId, {it.applicationRead(teamId, appId)}),
    postAuth = { features, permissions -> features.filterItem { permissions.featureRead(teamId, appId, it.key) }}
) {
    override fun execute(core: Core) = core
        .features.list(appId)
        .asSuccess()
}

class GetFeature(val teamId: TeamId, val appId: AppId, val featureKey: FeatureKey): ServiceAction<Feature>(
    preAuth = ActionAuth.byApplication(teamId, appId, {it.featureRead(teamId, appId, featureKey)})
) {
    override fun execute(core: Core) = core
        .features.getOrFail(appId, featureKey)
}

class CreateFeature(val teamId: TeamId, val appId: AppId, val data: FeatureCreateData): ServiceAction<Feature>(
    preAuth = ActionAuth.byApplication(teamId, appId, {it.featureCreate(teamId, appId)}) { getRequirements(data) }
) {
    override fun execute(core: Core) = core.applications
        .getOrFail(teamId, appId)
        .failIfExists(
            test = { core.features[appId, data.featureKey] },
            toFail = { _, feature -> featureAlreadyExists(appId, feature.key) }
        )
        .map { data.toFeature(it) }
        .peek(core.features::plusAssign)
}

class UpdateFeature(
    val teamId: TeamId,
    val appId: AppId,
    val featureKey: FeatureKey,
    val data: FeatureUpdateData
): ServiceAction<Feature>(
    preAuth = ActionAuth.byApplication(teamId, appId, {it.featureUpdate(teamId, appId, featureKey)}) { getRequirements(data) }
) {
    override fun execute(core: Core) = core
        .features.getOrFail(appId, featureKey)
        .map { it.update(data) }
        .peek(core.features::plusAssign)
}

class DeleteFeature(val teamId: TeamId, val appId: AppId, val featureKey: FeatureKey): ServiceAction<Feature>(
    preAuth = ActionAuth.byApplication(teamId, appId, { it.featureDelete(teamId, appId, featureKey)})
) {
    override fun execute(core: Core) = core
        .features.getOrFail(appId, featureKey)
        .peek(core.features::minusAssign)
}