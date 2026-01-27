package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.applications.web.ApplicationsPage
import com.zalphion.featurecontrol.applications.web.render
import com.zalphion.featurecontrol.web.environmentNameLens
import com.zalphion.featurecontrol.web.featureKeyLens
import com.zalphion.featurecontrol.features.CreateFeature
import com.zalphion.featurecontrol.features.DeleteFeature
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.features.UpdateFeature
import com.zalphion.featurecontrol.lib.Update
import com.zalphion.featurecontrol.web.htmlLens
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.flash.messages
import com.zalphion.featurecontrol.web.appIdLens
import com.zalphion.featurecontrol.web.applicationUri
import com.zalphion.featurecontrol.web.permissionsLens
import com.zalphion.featurecontrol.web.samePageError
import com.zalphion.featurecontrol.web.toIndex
import com.zalphion.featurecontrol.web.flash.withMessage
import com.zalphion.featurecontrol.web.teamIdLens
import com.zalphion.featurecontrol.web.uri
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.recover
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.location

internal fun Core.httpPostFeature(): HttpHandler = { request ->
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val principal = permissionsLens(request)
    val data = createFeatureCreateDataLens()(request)

    CreateFeature(teamId, appId, data)
        .invoke(principal, this)
        .map { Response(Status.SEE_OTHER).location(it.uri()) }
        .recover { request.toIndex().withMessage(it) }
}

internal fun Core.httpGetFeature(): HttpHandler = fn@{ request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val featureKey = featureKeyLens(request)

    ApplicationsPage.forFeature(this, principal, teamId, appId, featureKey)
        .map { model -> model.render(
            core = this,
            messages = request.messages(),
            selectedFeature = featureKey,
            content = {
                featureNavbar(model.selectedApplication, model.selectedItem, null)
                featureContent(this, model.selectedApplication, model.selectedItem)
            }
        ) }
        .map { Response(Status.OK).with(htmlLens of it) }
        .recover { request.toIndex().withMessage(it) }
}

internal fun Core.httpDeleteFeature(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val featureKey = featureKeyLens(request)

    DeleteFeature(teamId, appId, featureKey)
        .invoke(principal, this)
        .map { Response(Status.SEE_OTHER).location(applicationUri(teamId, appId)) }
        .recover { request.toIndex().withMessage(it) }
}

internal fun Core.httpPutFeature(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val featureKey = featureKeyLens(request)
    val data = createFeatureUpdateDataLens()(request)

    UpdateFeature(teamId, appId, featureKey, data)
        .invoke(principal, this)
        .map { Response(Status.SEE_OTHER)
            .location(it.uri())
            .withMessage("Feature updated", FlashMessageDto.Type.Success)
        }
        .recover { request.toIndex().withMessage(it) }
}

internal fun Core.httpGetFeatureEnvironment(): HttpHandler = fn@{ request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val featureKey = featureKeyLens(request)
    val environmentName = environmentNameLens(request)

    val pageModel = ApplicationsPage.forFeatureEnvironment(this, principal, teamId, appId, featureKey, environmentName)
        .onFailure { return@fn request.samePageError(it.reason) }

    val page = pageModel.render(
        core = this,
        messages = request.messages(),
        selectedFeature = featureKey,
        content = {
            featureNavbar(pageModel.selectedApplication, pageModel.selectedItem, environmentName)
            featureEnvironmentContent(this, pageModel.selectedApplication, pageModel.selectedItem, environmentName, pageModel.selectedEnvironment)
        }
    )

    Response(Status.OK).with(htmlLens of page)
}

internal fun Core.httpPostFeatureEnvironment(): HttpHandler = fn@{ request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val appId = appIdLens(request)
    val featureKey = featureKeyLens(request)
    val environmentName = environmentNameLens(request)
    val environment = createFeatureEnvironmentDataLens()(request)

    val data = FeatureUpdateData.empty.copy(
        environmentsToUpdate = Update(mapOf(environmentName to environment))
    )

    UpdateFeature(teamId, appId, featureKey, data)
        .invoke(principal, this)
        .map { Response(Status.SEE_OTHER)
            .location(it.uri(environmentName))
            .withMessage("Environment updated", FlashMessageDto.Type.Success)
        }
        .recover { request.toIndex().withMessage(it) }
}