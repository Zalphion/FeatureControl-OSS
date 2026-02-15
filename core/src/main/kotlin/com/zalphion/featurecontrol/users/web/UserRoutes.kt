package com.zalphion.featurecontrol.users.web

import com.zalphion.featurecontrol.FeatureControl
import com.zalphion.featurecontrol.web.htmlLens
import com.zalphion.featurecontrol.web.MainNavBar
import com.zalphion.featurecontrol.web.flash.messages
import com.zalphion.featurecontrol.web.permissionsLens
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

internal fun FeatureControl.showUserSettings(): HttpHandler = fn@{ request ->
    val permissions = permissionsLens(request)

    val navBar = MainNavBar.get(this, permissions)

    Response(Status.OK).with(htmlLens of userPageComponent(
        navBar = navBar,
        messages = request.messages(),
        permissions = permissions
    ))
}

