package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.events.localEventBus
import com.zalphion.featurecontrol.plugins.Plugin
import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.plugins.webjars
import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.memory
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.users.UserService
import org.http4k.core.Uri
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.collections.orEmpty
import kotlin.random.Random

abstract class CoreTestDriver(
    plugins: List<PluginFactory<*>> = emptyList(),
    storageDriver: StorageDriver = StorageDriver.memory(PageSize.of(2)),
    appSecret: AppSecret = AppSecret.of("secret")
) {
    var time: Instant = Instant.parse("2025-07-29T12:00:00Z")
    private val clock get() = object: Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?) = throw NotImplementedError()
        override fun instant() = time
    }

    var entitlements = mutableMapOf<TeamId, Entitlements>()

    val core = createCore(
        storageDriver = storageDriver,
        clock = clock,
        random = Random(1337),
        config = CoreConfig(
            staticUri = Uri.of("/"),
            origin = Uri.of("http://fake"),
            appSecret = appSecret,
            teamsStorageName = "teams",
            usersStorageName = "users",
            membersStorageName = "members",
            applicationsStorageName = "applications",
            featuresStorageName = "features",
            configsStorageName = "configs",
            configEnvironmentsTableName = "config_environments",
            apiKeysStorageName = "api_keys",
            invitationRetention = Duration.ofDays(1),
        ),

        plugins = listOf(
            *plugins.toTypedArray(),
            Plugin.webjars(),
            object : Plugin {
                override fun getEntitlements(teamId: TeamId) = entitlements[teamId].orEmpty()
            }.toFactory()
        ),
        eventBusFn = ::localEventBus
    )

    val users get() = UserService(core)
}