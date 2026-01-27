package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.ActionAuth
import com.zalphion.featurecontrol.ServiceAction
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applicationNotEmpty
import com.zalphion.featurecontrol.lib.filterItem
import com.zalphion.featurecontrol.teams.TeamId
import dev.andrewohara.utils.pagination.Paginator
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek

class ListApplications(val teamId: TeamId): ServiceAction<Paginator<Application, AppId>>(
    preAuth = ActionAuth.byTeam(teamId, {it.teamRead(teamId)}),
    postAuth = { applications, permissions -> applications.filterItem { permissions.applicationRead(teamId, it.appId) }}
) {
    override fun execute(core: Core) = core
        .applications.list(teamId).asSuccess()
}

class CreateApplication(
    val teamId: TeamId,
    val data: ApplicationCreateData
): ServiceAction<Application>(
    preAuth = ActionAuth.byTeam(teamId, {it.applicationCreate(teamId)}) {
        data.environments.flatMap(::getRequirements).toSet()
    }
) {
    override fun execute(core: Core) = data
        .toModel(teamId, core.random)
        .also(core.applications::plusAssign)
        .asSuccess()
}

class GetApplication(val teamId: TeamId, val appId: AppId): ServiceAction<Application>(
    preAuth = ActionAuth.byApplication(teamId, appId, {it.applicationRead(teamId, appId)})
) {
    override fun execute(core: Core) = core
        .applications.getOrFail(teamId, appId)
}

class UpdateApplication(val teamId: TeamId, val appId: AppId, val data: ApplicationUpdateData): ServiceAction<Application>(
    preAuth = ActionAuth.byApplication(teamId, appId, {it.applicationUpdate(teamId, appId)}) {
        data.environments.flatMap(::getRequirements).toSet()
    }
) {
    override fun execute(core: Core) = core
        .applications.getOrFail(teamId, appId)
        .map { it.update(data) }
        .peek(core.applications::plusAssign)
}

class DeleteApplication(val teamId: TeamId, val appId: AppId): ServiceAction<Application>(
    preAuth = ActionAuth.byApplication(teamId, appId, {it.applicationDelete(teamId, appId)})
) {
    override fun execute(core: Core) = core
        .applications.getOrFail(teamId, appId)
        .failIf(
            cond = { core.features.list(it.appId).any() },
            f= { applicationNotEmpty(it.appId) }
        )
        // TODO delete all api keys
        .peek(core.applications::minusAssign)
}