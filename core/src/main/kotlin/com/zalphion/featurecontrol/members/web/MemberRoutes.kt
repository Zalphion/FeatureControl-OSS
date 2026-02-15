package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.FeatureControl
import com.zalphion.featurecontrol.teams.web.TeamPageComponent
import com.zalphion.featurecontrol.teams.web.teamPage
import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.members.MemberUpdateData
import com.zalphion.featurecontrol.web.flash.FlashMessageDto
import com.zalphion.featurecontrol.web.PageSpec
import com.zalphion.featurecontrol.web.flash.messages
import com.zalphion.featurecontrol.web.htmlLens
import com.zalphion.featurecontrol.web.permissionsLens
import com.zalphion.featurecontrol.web.applicationsUri
import com.zalphion.featurecontrol.web.referrerLens
import com.zalphion.featurecontrol.web.samePageError
import com.zalphion.featurecontrol.web.teamIdLens
import com.zalphion.featurecontrol.web.flash.toFlashMessage
import com.zalphion.featurecontrol.web.toIndex
import com.zalphion.featurecontrol.web.userIdLens
import com.zalphion.featurecontrol.web.flash.withMessage
import com.zalphion.featurecontrol.web.flash.withSuccess
import com.zalphion.featurecontrol.web.samePage
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.recover
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.location
import kotlin.collections.toList

internal fun FeatureControl.showMembers(): HttpHandler = fn@{ request ->
    val permissions = permissionsLens(request)
    val teamId = teamIdLens(request)

    val members = core.members.list(teamId)
        .invoke(permissions, this)
        .onFailure { return@fn request.toIndex().withMessage(it.reason) }
        .toList()

    val model = TeamPageComponent.create(this, permissions, teamId, PageSpec.members)
        .onFailure { return@fn request.samePageError(it) }

    Response(Status.OK).with(htmlLens of teamPage(
        model = model,
        messages = request.messages(),
        content = { membersView(this@showMembers, it.team.team, members, permissions) }
    ))
}

internal fun FeatureControl.acceptInvitation(): HttpHandler = { request ->
    val permissions = permissionsLens(request)
    val teamId = teamIdLens(request)

    core.members.acceptInvitation(teamId, permissions.principal.userId)
        .invoke(permissions, this)
        .map {
            Response(Status.SEE_OTHER)
                .location(applicationsUri(teamId))
                .withSuccess("Invitation accepted")
        }
        .recover {
            Response(Status.SEE_OTHER)
                .location(referrerLens(request))
                .withMessage(it)
        }
}

internal fun FeatureControl.updateMember(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val userId = userIdLens(request)

    core.members.update(
        teamId = teamId,
        userId = userId,
        data = extract<MemberUpdateData>(request)
    )
        .invoke(principal, this)
        .map { Response(Status.SEE_OTHER)
            .location(referrerLens(request))
            .withSuccess("Member Updated")
        }.recover {
            Response(Status.SEE_OTHER)
                .location(referrerLens(request))
                .withMessage(it)
        }
}

internal fun FeatureControl.deleteMember(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val userId = userIdLens(request)

    core.members.remove(teamId, userId)
        .invoke(principal, this)
        .map {
            Response(Status.SEE_OTHER)
                .location(referrerLens(request))
                .withSuccess("Removed ${it.user.fullName()} from ${it.team.teamName}")
        }.recover {
            Response(Status.SEE_OTHER)
                .location(referrerLens(request))
                .withMessage(it)
        }
}

internal fun FeatureControl.resendInvitation(): HttpHandler = { request ->
    val principal = permissionsLens(request)
    val teamId = teamIdLens(request)
    val userId = userIdLens(request)

    core.members.resendInvitation(teamId, userId).invoke(principal, this).map {
        Response(Status.SEE_OTHER)
            .location(referrerLens(request))
            .withSuccess("Invitation resent")
    }.recover {
        Response(Status.SEE_OTHER)
            .location(referrerLens(request))
            .withMessage(it)
    }
}

internal fun FeatureControl.createMember(): HttpHandler = { request ->
    val permissions = permissionsLens(request)
    val teamId = teamIdLens(request)

    val result = core.members.invite(
        teamId = teamId,
        sender = permissions.principal.userId,
        data = extract<MemberCreateData>(request)
    )
        .invoke(permissions, this)
        .map { FlashMessageDto(FlashMessageDto.Type.Success, "Invitation sent to ${it.member.userId.toEmailAddress()}") }
        .recover { it.toFlashMessage() }

    request.samePage(result)
}