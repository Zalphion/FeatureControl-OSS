package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.teams.TeamId
import dev.andrewohara.utils.result.failIf
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.begin
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map

fun preAuth(evaluate: (Permissions<*>) -> Boolean) = ServiceAction { Unit.asSuccess() }.preAuth(evaluate)

fun interface ServiceAction<T: Any> {
    operator fun invoke(permissions: Permissions<*>): Result4k<T, AppError>

    fun before(before: (Permissions<*>) -> Result4k<Unit, AppError>) = ServiceAction { permissions ->
        before(permissions).flatMap { this@ServiceAction(permissions) }
    }

    fun after(after: (Permissions<*>, T) -> T) = ServiceAction { permissions ->
        this@ServiceAction(permissions).map { after(permissions, it) }
    }

    fun <O: Any> map(fn: (T) -> O) = ServiceAction { permissions ->
        this@ServiceAction(permissions).map { fn(it) }
    }

    fun <O: Any> flatMap(fn: (T) -> Result4k<O, AppError>) = ServiceAction { permissions ->
        this@ServiceAction(permissions).flatMap { fn(it) }
    }

    fun failIf(predicate: (T) -> Boolean, toError: (T) -> AppError) = flatMap { success ->
        if (predicate(success)) toError(success).asFailure() else success.asSuccess()
    }

    fun peek(fn: (T) -> Unit) = map { value -> fn(value); value }

    fun preAuth(evaluate: (Permissions<*>) -> Boolean): ServiceAction<T> = before { permissions ->
        begin.failIf({!evaluate(permissions)}, { forbidden })
    }

    fun checkEntitlements(core: Core, teamId: TeamId, requirementsFn: (Core) -> Entitlements) = before { _ ->
        val missing = requirementsFn(core) - core.getEntitlements(teamId)

        if (missing.isNotEmpty()) {
            missingEntitlements(missing).asFailure()
        } else {
            Unit.asSuccess()
        }
    }
}