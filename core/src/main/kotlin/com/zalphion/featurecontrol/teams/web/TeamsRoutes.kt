package com.zalphion.featurecontrol.teams.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.teams.TeamCreateUpdateData
import com.zalphion.featurecontrol.teams.TeamName
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.membersUri
import com.zalphion.featurecontrol.web.permissionsLens
import com.zalphion.featurecontrol.web.samePage
import com.zalphion.featurecontrol.web.teamIdLens
import com.zalphion.featurecontrol.web.flash.toFlashMessage
import com.zalphion.featurecontrol.web.toIndex
import com.zalphion.featurecontrol.web.flash.withMessage
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.recover
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.location
import org.http4k.lens.value
import org.http4k.lens.webForm

private object CreateUpdateTeamForm {
    val teamName = FormField.value(TeamName).required("teamName")
    val form = Body.webForm(Validator.Strict, teamName).toLens()
}

internal fun Core.createTeam(): HttpHandler = fn@{ request ->
    val permissions = permissionsLens(request)
    val form = CreateUpdateTeamForm.form(request)
    val data = TeamCreateUpdateData(
        teamName = CreateUpdateTeamForm.teamName(form)
    )

    val team = teams.create(permissions.principal.userId, data)
        .invoke(permissions)
        .onFailure { return@fn request.toIndex().withMessage(it.reason) }

    Response(Status.SEE_OTHER).location(membersUri(team.teamId))
}

internal fun Core.updateTeam(): HttpHandler = fn@ { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)

    val form = CreateUpdateTeamForm.form(request)
    val data = TeamCreateUpdateData(
        teamName = CreateUpdateTeamForm.teamName(form)
    )

    val result = teams.update(teamId, data)
        .invoke(principal)
        .map { FlashMessageDto(FlashMessageDto.Type.Success, "Team Updated") }
        .recover { it.toFlashMessage() }

    request.samePage(result)
}