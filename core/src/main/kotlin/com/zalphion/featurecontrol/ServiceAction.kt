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

fun preAuth(evaluate: (Permissions<*>) -> Boolean) = ServiceAction { _, _ -> Unit.asSuccess() }.preAuth(evaluate)

fun interface ServiceAction<T: Any> {
    operator fun invoke(permissions: Permissions<*>, app: FeatureControl): Result4k<T, AppError>

    fun before(before: (Permissions<*>, FeatureControl) -> Result4k<Unit, AppError>) = ServiceAction { permissions, app ->
        before(permissions, app).flatMap { this@ServiceAction(permissions, app) }
    }

    fun after(after: (Permissions<*>, FeatureControl, T) -> T) = ServiceAction { permissions, app ->
        this@ServiceAction(permissions, app).map { after(permissions, app, it) }
    }

    fun <O: Any> map(fn: (T) -> O) = ServiceAction { permissions, app ->
        this@ServiceAction(permissions, app).map { fn(it) }
    }

    fun <O: Any> flatMap(fn: (T) -> Result4k<O, AppError>) = ServiceAction { permissions, app ->
        this@ServiceAction(permissions, app).flatMap { fn(it) }
    }

    fun failIf(predicate: (T) -> Boolean, toError: (T) -> AppError) = flatMap { success ->
        if (predicate(success)) toError(success).asFailure() else success.asSuccess()
    }

    fun peek(fn: (T) -> Unit) = map { value -> fn(value); value }

    fun preAuth(evaluate: (Permissions<*>) -> Boolean): ServiceAction<T> = before { permissions, app ->
        begin.failIf({!evaluate(permissions)}, { forbidden })
    }

    fun checkEntitlements(teamId: TeamId, requirementsFn: (FeatureControl) -> Entitlements) = before { _, app ->
        val missing = requirementsFn(app) - app.getEntitlements(teamId)

        if (missing.isNotEmpty()) {
            missingEntitlements(missing).asFailure()
        } else {
            Unit.asSuccess()
        }
    }
}