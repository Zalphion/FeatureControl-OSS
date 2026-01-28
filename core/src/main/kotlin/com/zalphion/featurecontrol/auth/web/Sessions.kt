package com.zalphion.featurecontrol.auth.web

import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.users.UserId
import com.zalphion.featurecontrol.web.INDEX_PATH
import com.zalphion.featurecontrol.web.SESSION_COOKIE_NAME
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import java.time.Instant

interface Sessions {
    fun create(userId: UserId): Pair<String, Instant>
    fun verify(sessionId: String): UserId?

    companion object
}

fun Core.createSessionCookie(userId: UserId): Cookie {
    val (sessionId, expires) = sessions.create(userId)
    return Cookie(
        name = SESSION_COOKIE_NAME,
        value = sessionId,
        secure = config.secureCookies,
        sameSite = SameSite.Lax,
        httpOnly = true,
        expires = expires,
        path = INDEX_PATH
    )
}