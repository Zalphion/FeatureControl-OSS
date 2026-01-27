package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applications.web.ApplicationsPage
import com.zalphion.featurecontrol.applications.web.render
import com.zalphion.featurecontrol.configs.UpdateConfigSpec
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.environmentNameLens
import com.zalphion.featurecontrol.web.flash.messages
import com.zalphion.featurecontrol.web.htmlLens
import com.zalphion.featurecontrol.web.permissionsLens
import com.zalphion.featurecontrol.web.appIdLens
import com.zalphion.featurecontrol.web.referrerLens
import com.zalphion.featurecontrol.web.samePageError
import com.zalphion.featurecontrol.web.flash.toFlashMessage
import com.zalphion.featurecontrol.web.toIndex
import com.zalphion.featurecontrol.web.flash.withMessage
import com.zalphion.featurecontrol.configs.UpdateConfigEnvironment
import com.zalphion.featurecontrol.web.teamIdLens
import com.zalphion.featurecontrol.web.uri
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.recover
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.location

internal fun Core.httpGetConfigSpec(): HttpHandler = fn@{ request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)

    ApplicationsPage.forConfigSpec(this, principal, teamId, appId)
        .map { model ->
            model.render(
                core = this,
                messages = request.messages(),
                selectedFeature = null
            ) {
                configSpecContent(this, model.selectedApplication, model.selectedItem)
            }
        }
        .map { Response(Status.OK).with(htmlLens of it) }
        .recover { request.toIndex().withMessage(it) }
}

internal fun Core.httpPostConfigSpec(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val data = createConfigSpecDataLens()(request)

    val result = UpdateConfigSpec(teamId, appId, data)
        .invoke(principal, this)
        .map { FlashMessageDto(FlashMessageDto.Type.Success, "Config Properties Updated") }
        .recover { it.toFlashMessage() }

    Response(Status.SEE_OTHER)
        .location(referrerLens(request))
        .withMessage(result)
}

internal fun Core.httpGetConfigEnvironment(): HttpHandler = fn@{ request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val environmentName = environmentNameLens(request)

    ApplicationsPage.forConfigEnvironment(this, principal, teamId, appId, environmentName)
        .map { model ->
            model.render(
                core = this,
                messages = request.messages(),
                selectedFeature = null
            ) {
                configEnvironmentContent(this, model.selectedApplication, model.selectedItem, model.selectedEnvironment)
            }
        }
        .map { Response(Status.OK).with(htmlLens of it) }
        .recover { request.toIndex().withMessage(it) }
}

internal fun Core.httpPostConfigEnvironment(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val environmentName = environmentNameLens(request)
    val data = createConfigEnvironmentDataLens()(request)

    UpdateConfigEnvironment(teamId, appId, environmentName, data)
        .invoke(principal, this)
        .map { Response(Status.SEE_OTHER)
            .location(it.uri())
            .withMessage("Config Values Updated", FlashMessageDto.Type.Success)
        }
        .recover { request.samePageError(it) }
}