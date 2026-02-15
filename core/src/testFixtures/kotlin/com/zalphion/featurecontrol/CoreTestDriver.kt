package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.auth.Entitlements
import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.events.localEventBus
import com.zalphion.featurecontrol.plugins.Plugin
import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.plugins.toFactory
import com.zalphion.featurecontrol.plugins.webjars
import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.h2db.h2DbInMemory
import com.zalphion.featurecontrol.teams.TeamId
import org.http4k.core.Uri
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.collections.orEmpty
import kotlin.random.Random

abstract class CoreTestDriver(
    additionalPlugins: List<PluginFactory<*>> = emptyList(),
    storageDriver: StorageDriver = StorageDriver.h2DbInMemory(PageSize.of(2)),
    val invitationRetention: Duration = Duration.ofHours(5),
    appSecret: AppSecret = AppSecret.of("secret")
) {
    var time: Instant = Instant.parse("2025-07-29T12:00:00Z")
    private val clock get() = object: Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?) = throw NotImplementedError()
        override fun instant() = time
    }

    var entitlements = mutableMapOf<TeamId, Entitlements>()

    private val plugins = listOf(
        *additionalPlugins.toTypedArray(),
        Plugin.webjars(),
        object : Plugin {
            override fun getEntitlements(teamId: TeamId) = entitlements[teamId].orEmpty()
        }.toFactory()
    )

    private val coreConfig = CoreConfig(
        staticUri = Uri.of("/static"),
        origin = Uri.of("http://fake"),
        appSecret = appSecret,
        teamsStorageName = "teams",
        usersStorageName = "users",
        membersStorageName = "members",
        applicationsStorageName = "applications",
        featuresStorageName = "features",
        configsStorageName = "configs",
        configEnvironmentsStorageName = "config_environments",
        apiKeysStorageName = "api_keys",
        invitationRetention = invitationRetention
    )

    val core = Core.build(
        storageDriver = storageDriver,
        clock = clock,
        random = Random(1337),
        config = coreConfig,
        plugins = plugins
    )

    val app = FeatureControl(
        appName = "Test Control",
        core = core,
        plugins = plugins.map { it.create(core) },
        eventBusFn = ::localEventBus,
        config = coreConfig
    )
}