package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.AppError
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.events.Event
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import org.http4k.routing.RoutingHttpHandler

interface Plugin {

    fun onEvent(event: Event): Result4k<Unit, AppError> = Unit.asSuccess()

    // Applied to the HTTP root (bring-your-own security)
    fun getRoutes(core: Core): RoutingHttpHandler? = null

    // Applied within the existing web routes, using its security
    fun getWebRoutes(core: Core): RoutingHttpHandler? = null

    companion object
}