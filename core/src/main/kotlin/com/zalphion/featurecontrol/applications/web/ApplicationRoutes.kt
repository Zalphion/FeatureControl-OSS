package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.FeatureControl
import com.zalphion.featurecontrol.web.htmlLens
import com.zalphion.featurecontrol.web.appIdLens
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.flash.messages
import com.zalphion.featurecontrol.web.permissionsLens
import com.zalphion.featurecontrol.web.samePage
import com.zalphion.featurecontrol.web.teamIdLens
import com.zalphion.featurecontrol.web.toIndex
import com.zalphion.featurecontrol.web.flash.withMessage
import com.zalphion.featurecontrol.web.flash.withSuccess
import com.zalphion.featurecontrol.web.uri
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.recover
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.location

fun FeatureControl.httpGetApplications(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)

    ApplicationsPage.forTeam(this, principal, teamId)
        .map {
            it.render(
                app = this,
                messages = request.messages(),
                selectedFeature = null
            )
        }
        .map { Response(Status.OK).with(htmlLens of it) }
        .recover { request.toIndex().withMessage(it) }
}

fun FeatureControl.httpPostApplications(): HttpHandler = { request ->
    core.applications.create(
        teamId = teamIdLens(request),
        data = extract(request)
    )
        .invoke(permissionsLens(request), this)
        .map {
            Response(Status.SEE_OTHER)
                .location(it.uri())
                .withSuccess("Created ${it.appName}")
        }
        .recover { request.toIndex().withMessage(it) }
}

internal fun FeatureControl.httpDeleteApplication(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val applicationId = appIdLens(request)

    core.applications.delete(teamId, applicationId)
        .invoke(principal, this)
        .map { request.samePage(FlashMessageDto(FlashMessageDto.Type.Success, "Deleted ${it.appName}")) }
        .onFailure { error(it.reason) }
}

internal fun FeatureControl.httpPostApplication(): HttpHandler = { request ->
    core.applications.update(
        teamId = teamIdLens(request),
        appId = appIdLens(request),
        data = extract(request)
    )
        .invoke(permissionsLens(request), this)
        .map { Response(Status.SEE_OTHER).location(it.uri()) }
        .onFailure { error(it.reason) }
}