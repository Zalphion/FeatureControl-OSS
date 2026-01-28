package com.zalphion.featurecontrol.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.auth.web.csrfDoubleSubmitFilter
import dev.andrewohara.utils.http4k.logErrors
import dev.andrewohara.utils.http4k.logSummary
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.cookie.cookie
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.FlashAttributesFilter
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.lens.location
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes

fun Core.mergeRoutes(): RoutingHttpHandler {
    val sessionFilter = Filter { next ->
        { request ->
            request.cookie(SESSION_COOKIE_NAME)?.value
                ?.let(sessions::verify)
                ?.let(permissions::create)
                ?.let { request.with(permissionsLens of it) }
                ?.let(next)
                ?: Response(Status.FOUND).location(Uri.of(LOGIN_PATH))
        }
    }

    val webFilter = FlashAttributesFilter
        .then(sessionFilter)
        .then(csrfDoubleSubmitFilter(random, config.secureCookies, config.csrfTtl))

    return ResponseFilters
        .logSummary(clock = clock)
        .then(ServerFilters.logErrors())
        .then(routes(listOf(
            getRoutes(),
            webFilter.then(getWebRoutes())
        )))
}