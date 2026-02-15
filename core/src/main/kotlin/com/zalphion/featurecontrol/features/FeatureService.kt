package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.ApplicationStorage
import com.zalphion.featurecontrol.featureAlreadyExists
import com.zalphion.featurecontrol.lib.filterItem
import com.zalphion.featurecontrol.preAuth
import com.zalphion.featurecontrol.teams.TeamId
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek

class FeatureService(
    private val features: FeatureStorage,
    private val applications: ApplicationStorage
) {

    fun list(teamId: TeamId, appId: AppId) = preAuth {
        it.applicationRead(teamId, appId)
    }.map {
        features.list(appId)
    }.after { permissions, _, features ->
        features.filterItem { permissions.featureRead(teamId, appId, it.key) }
    }

    fun get(teamId: TeamId, appId: AppId, featureKey: FeatureKey) = preAuth {
        it.featureRead(teamId, appId, featureKey)
    }.flatMap {
        applications.getOrFail(teamId, appId)
            .flatMap { features.getOrFail(appId, featureKey) }
    }

    fun create(teamId: TeamId, appId: AppId, data: FeatureCreateData) = preAuth {
        it.featureCreate(teamId, appId)
    }.checkEntitlements(teamId) {
        it.getRequirements(data)
    }.failIf(
        predicate = {features[appId, data.featureKey] != null},
        toError = { featureAlreadyExists(appId, data.featureKey) }
    ).flatMap {
        applications.getOrFail(teamId, appId)
            .map { data.toFeature(it) }
            .peek(features::plusAssign)
    }

    fun update(teamId: TeamId, appId: AppId, featureKey: FeatureKey, data: FeatureUpdateData) = preAuth {
        it.featureUpdate(teamId, appId, featureKey)
    }.checkEntitlements(teamId) {
        it.getRequirements(data)
    }.flatMap {
        applications.getOrFail(teamId, appId)
            .flatMap { features.getOrFail(appId, featureKey) }
            .map { it.update(data) }
            .peek(features::plusAssign)
    }

    fun delete(teamId: TeamId, appId: AppId, featureKey: FeatureKey) = preAuth {
        it.featureDelete(teamId, appId, featureKey)
    }.flatMap {
        applications.getOrFail(teamId, appId)
            .flatMap { features.getOrFail(appId, featureKey) }
            .peek(features::minusAssign)
    }
}