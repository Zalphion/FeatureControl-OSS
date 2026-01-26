package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.web.REDIRECT_PATH
import org.http4k.core.Uri
import java.time.Duration

data class CoreConfig(
    val origin: Uri,
    val staticUri: Uri,
    val appSecret: AppSecret,
    val googleClientId: String? = null,
    val csrfTtl: Duration = Duration.ofHours(8),
    val sessionLength: Duration = Duration.ofDays(7),
    val secureCookies: Boolean = origin.scheme == "https",
    var invitationRetention: Duration = Duration.ofDays(7),
    val redirectUri: Uri = origin.path(REDIRECT_PATH),

    val teamsStorageName: String,
    val usersStorageName: String,
    val membersStorageName: String,
    val applicationsStorageName: String,
    val featuresStorageName: String,
    val configsStorageName: String,
    val configEnvironmentsTableName: String,
    val apiKeysStorageName: String,
)