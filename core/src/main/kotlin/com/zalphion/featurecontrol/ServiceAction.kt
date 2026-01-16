package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.members.UserRole
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.User
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map

abstract class ServiceAction<T>(
    val auth: ActionAuth
) {
    operator fun invoke(principal: User, core: Core) = this
        .auth(core, principal)
        .flatMap { execute(core) }

    abstract fun execute(core: Core): Result4k<T, AppError>
}

fun interface ActionAuth: (Core, User) -> Result4k<Unit, AppError> {

    companion object {
        fun byTeam(
            teamId: TeamId,
            minimumRole: UserRole = UserRole.Tester,
            getRequirements: Core.() -> Entitlements = { emptySet() }
        ) = ActionAuth { core, user ->
            val missingEntitlements = getRequirements(core) - core.getEntitlements(teamId)

            core.members[teamId, user.userId]
                ?.takeIf { it.role >= minimumRole }
                .asResultOr { forbidden }
                .failIf(
                    cond = { missingEntitlements.isNotEmpty() },
                    f = { missingEntitlements(missingEntitlements) }
                )
                .map {  }
        }

        fun byApplication(
            teamId: TeamId,
            appId: AppId,
            minimumRole: UserRole = UserRole.Tester,
            getRequirements: Core.() -> Entitlements = { emptySet() }
        ) = ActionAuth { core, user ->
            core.apps.getOrFail(teamId, appId)
                .map { byTeam(it.teamId, minimumRole, getRequirements) }
                .flatMap { it(core, user) }
        }
    }
}