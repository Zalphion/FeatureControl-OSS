package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.web.REDIRECT_PATH
import org.http4k.core.Uri
import java.time.Duration

data class CoreConfig(
    val origin: Uri,
    val staticUri: Uri, // point to static assets (e.g. css/js/img)
    val appSecret: AppSecret,
    val googleClientId: String? = null,
    val csrfTtl: Duration = Duration.ofHours(8),
    val sessionLength: Duration = Duration.ofDays(7),
    val secureCookies: Boolean = origin.scheme == "https",
    var invitationRetention: Duration = Duration.ofDays(7),
    val redirectUri: Uri = origin.path(REDIRECT_PATH),

    val teamsStorageName: String = "teams",
    val usersStorageName: String = "users",
    val membersStorageName: String = "members",
    val applicationsStorageName: String = "applications",
    val featuresStorageName: String = "features",
    val configsStorageName: String = "config_specs",
    val configEnvironmentsTableName: String = "config_environments",
    val apiKeysStorageName: String = "api_keys",
)