package com.zalphion.featurecontrol.auth.web

import com.zalphion.featurecontrol.users.UserId
import java.time.Instant

interface Sessions {
    fun create(userId: UserId): Pair<String, Instant>
    fun verify(sessionId: String): UserId?

    companion object
}