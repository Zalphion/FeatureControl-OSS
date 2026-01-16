package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.web.htmlLens
import com.zalphion.featurecontrol.applications.CreateApplication
import com.zalphion.featurecontrol.applications.DeleteApplication
import com.zalphion.featurecontrol.web.appIdLens
import com.zalphion.featurecontrol.applications.UpdateApplication
import com.zalphion.featurecontrol.web.applicationUri
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.flash.messages
import com.zalphion.featurecontrol.web.principalLens
import com.zalphion.featurecontrol.web.samePage
import com.zalphion.featurecontrol.web.teamIdLens
import com.zalphion.featurecontrol.web.toIndex
import com.zalphion.featurecontrol.web.flash.withMessage
import com.zalphion.featurecontrol.web.flash.withSuccess
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.recover
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.location

fun Core.showApplications(): HttpHandler = { request ->
    val principal = principalLens(request)
    val teamId = teamIdLens(request)

    ApplicationsPage.forTeam(this, principal, teamId)
        .map {
            it.render(
                core = this,
                messages = request.messages(),
                selectedFeature = null
            )
        }
        .map { Response(Status.OK).with(htmlLens of it) }
        .recover { request.toIndex().withMessage(it) }
}

fun Core.createApplication(): HttpHandler = { request ->
    val principal = principalLens(request)
    val teamId = teamIdLens(request)
    val data = createApplicationCreateDataLens()(request)

    CreateApplication(teamId, data)
        .invoke(principal, this)
        .map {
            Response(Status.SEE_OTHER)
                .location(applicationUri(it.appId))
                .withSuccess("Created ${it.appName}")
        }
        .recover { request.toIndex().withMessage(it) }
}

internal fun Core.deleteApplication(): HttpHandler = { request ->
    val principal = principalLens(request)
    val teamId = teamIdLens(request)
    val applicationId = appIdLens(request)

    DeleteApplication(teamId, applicationId)
        .invoke(principal, this)
        .map { request.samePage(FlashMessageDto(FlashMessageDto.Type.Success, "Deleted ${it.appName}")) }
        .onFailure { error(it.reason) }
}

internal fun Core.updateApplication(): HttpHandler = { request ->
    val principal = principalLens(request)
    val teamId = teamIdLens(request)
    val applicationId = appIdLens(request)
    val data = this@updateApplication.createApplicationUpdateDataLens()(request)

    UpdateApplication(teamId, applicationId, data)
        .invoke(principal, this)
        .map { Response(Status.SEE_OTHER).location(applicationUri(it.appId)) }
        .onFailure { error(it.reason) }
}