package com.zalphion.featurecontrol.web

import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.auth.Permissions
import com.zalphion.featurecontrol.features.EnvironmentName
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.users.UserId
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.lens.Header
import org.http4k.lens.Path
import org.http4k.lens.RequestKey
import org.http4k.lens.string
import org.http4k.lens.uri
import org.http4k.lens.value

val teamIdLens = Path.value(TeamId).of("teamId")
val appIdLens = Path.value(AppId).of("app_id")
val featureKeyLens = Path.value(FeatureKey).of("toggle_key")
val environmentNameLens = Path.value(EnvironmentName).of("environment_name")
val userIdLens = Path.value(UserId).of("user_id")

val htmlLens = Body.string(ContentType.TEXT_HTML)
    .map({it}, { "<!DOCTYPE html>\n$it" })
    .toLens()

val permissionsLens = RequestKey.required<Permissions<User>>("principal")

val referrerLens = Header.uri().required("Referer")

const val INDEX_PATH = "/"
const val LOGIN_PATH = "/login"
const val REDIRECT_PATH = "/oauth2/redirect"
const val LOGOUT_PATH = "/logout"
const val USER_SETTINGS_PATH = "/user-settings"

// TODO replace this with a lens
const val SESSION_COOKIE_NAME = "${APP_SLUG}_session"
