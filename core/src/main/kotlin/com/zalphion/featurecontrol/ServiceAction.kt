package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.teams.TeamId
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.begin
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map

abstract class ServiceAction<T>(
    val preAuth: ActionAuth,
    val postAuth: (T, Permissions<*>) -> T = { value, _ -> value }
) {
    operator fun invoke(permissions: Permissions<*>, core: Core) = this
        .preAuth(core, permissions)
        .flatMap { execute(core) }
        .map { postAuth(it, permissions) }

    abstract fun execute(core: Core): Result4k<T, AppError>
}

fun interface ActionAuth: (Core, Permissions<*>) -> Result4k<Unit, AppError> {

    companion object {
        fun byTeam(
            teamId: TeamId,
            evaluatePermissions: (Permissions<*>) -> Boolean,
            getRequirements: Core.() -> Entitlements = { emptySet() }
        ) = ActionAuth { core, permissions ->
            begin
                .failIf({!evaluatePermissions(permissions)}, { forbidden})
                .map { getRequirements(core) - core.getEntitlements(teamId) }
                .failIf({ it.isNotEmpty() }, ::missingEntitlements)
                .map {  }
        }

        fun byApplication(
            teamId: TeamId,
            appId: AppId,
            evaluatePermissions: (Permissions<*>) -> Boolean,
            getRequirements: Core.() -> Entitlements = { emptySet() }
        ) = ActionAuth { core, permissions ->
            core.applications.getOrFail(teamId, appId)
                .map { byTeam(it.teamId, evaluatePermissions, getRequirements) }
                .flatMap { it(core, permissions) }
        }
    }
}