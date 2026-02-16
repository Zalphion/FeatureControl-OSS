package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applicationNotEmpty
import com.zalphion.featurecontrol.features.FeatureStorage
import com.zalphion.featurecontrol.lib.filterItem
import com.zalphion.featurecontrol.preAuth
import com.zalphion.featurecontrol.teams.TeamId
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek

class ApplicationService(
    private val core: Core,
    private val applications: ApplicationStorage,
    private val features: FeatureStorage
) {

    fun list(teamId: TeamId) = preAuth {
        it.teamRead(teamId)
    }.map {
        applications.list(teamId)
    }.after { permissions, applications ->
        applications.filterItem { permissions.applicationRead(teamId, it.appId) }
    }

    fun create(teamId: TeamId, data: ApplicationCreateData) = preAuth {
        it.applicationCreate(teamId)
    }.checkEntitlements(core, teamId) {
        it.getRequirements(data)
    }.map {
        data.toModel(teamId, core.random).also(applications::plusAssign)
    }

    fun get(teamId: TeamId, appId: AppId) = preAuth {
        it.applicationRead(teamId, appId)
    }.flatMap {
        applications.getOrFail(teamId, appId)
    }

    fun update(teamId: TeamId, appId: AppId, data: ApplicationUpdateData) = preAuth {
        it.applicationUpdate(teamId, appId)
    }.checkEntitlements(core, teamId) {
        it.getRequirements(data)
    }.flatMap {
        applications.getOrFail(teamId, appId)
            .map { it.update(data) }
            .peek(applications::plusAssign)
    }

    fun delete(teamId: TeamId, appId: AppId) = preAuth {
        it.applicationDelete(teamId, appId)
    }.flatMap {
        applications.getOrFail(teamId, appId)
        .failIf(
            // FIXME this is kind of messy; maybe should let services access other storages
            cond = { features.list(appId).any() },
            f = { applicationNotEmpty(it.appId) }
        )
        // TODO delete all api keys
        .peek(applications::minusAssign)
    }
}