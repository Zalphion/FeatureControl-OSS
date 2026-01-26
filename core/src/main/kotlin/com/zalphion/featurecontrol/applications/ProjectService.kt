package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.ActionAuth
import com.zalphion.featurecontrol.ServiceAction
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.members.UserRole
import com.zalphion.featurecontrol.applicationNotEmpty
import com.zalphion.featurecontrol.teams.TeamId
import dev.andrewohara.utils.pagination.Paginator
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek

class ListApplications(val teamId: TeamId): ServiceAction<Paginator<Application, AppId>>(
    auth = ActionAuth.byTeam(teamId)
) {
    override fun execute(core: Core) = core
        .applications.list(teamId).asSuccess()
}

class CreateApplication(
    val teamId: TeamId,
    val data: ApplicationCreateData
): ServiceAction<Application>(
    auth = ActionAuth.byTeam(teamId, UserRole.Developer) {
        data.environments.flatMap(::getRequirements).toSet()
    }
) {
    override fun execute(core: Core) = data
        .toModel(teamId, core.random)
        .also(core.applications::plusAssign)
        .asSuccess()
}

class GetApplication(val teamId: TeamId, val appId: AppId): ServiceAction<Application>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Tester)
) {
    override fun execute(core: Core) = core
        .applications.getOrFail(teamId, appId)
}

class UpdateApplication(val teamId: TeamId, val appId: AppId, val data: ApplicationUpdateData): ServiceAction<Application>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Developer) {
        data.environments.flatMap(::getRequirements).toSet()
    }
) {
    override fun execute(core: Core) = core
        .applications.getOrFail(teamId, appId)
        .map { it.update(data) }
        .peek(core.applications::plusAssign)
}

class DeleteApplication(val teamId: TeamId, val appId: AppId): ServiceAction<Application>(
    auth = ActionAuth.byApplication(teamId, appId, UserRole.Developer)
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