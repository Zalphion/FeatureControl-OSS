package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.emails.EmailSender
import com.zalphion.featurecontrol.emails.email
import com.zalphion.featurecontrol.web.LOGIN_PATH
import com.zalphion.featurecontrol.emails.smtp
import com.zalphion.featurecontrol.events.localEventBus
import com.zalphion.featurecontrol.plugins.Plugin
import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.plugins.webjars
import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.postgres.postgres
import org.http4k.config.Environment
import org.http4k.core.Credentials
import org.http4k.core.Uri
import org.http4k.filter.ServerFilters
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.security.SecureRandom
import java.time.Clock
import kotlin.random.asKotlinRandom

fun main() = hostedMain()

fun hostedMain(
    appName: String = "Feature Control",
    env: Environment = Environment.ENV,
    additionalPlugins: List<PluginFactory<*>> = emptyList()
) {
    val plugins = listOf(
        *additionalPlugins.toTypedArray(),
        Plugin.webjars(),
        Plugin.email(
            loginUri = env[Settings.origin].path(LOGIN_PATH),
            appName = appName,
            emails = EmailSender.smtp(
                fromName = env[Settings.smtpFromName] ?: appName,
                fromAddress = env[Settings.smtpFromAddress],
                authority = env[Settings.smtpAuthority],
                credentials = env[Settings.smtpUsername]?.let { username ->
                    Credentials(username, env[Settings.smtpPassword].use { it })
                },
                startTls = env[Settings.smtpStartTls],
            )
        )
    )

    val coreConfig = CoreConfig(
        appSecret = env[Settings.appSecret],
        staticUri = Uri.of("/static"), // provided by webjars
        origin = env[Settings.origin],
        googleClientId = env[Settings.googleClientId],
        csrfTtl = env[Settings.csrfTtl],
        sessionLength = env[Settings.sessionLength],
        invitationRetention = env[Settings.invitationsRetention]
    )

    val core = Core.build(
        clock = Clock.systemUTC(),
        random = SecureRandom().asKotlinRandom(),
        storageDriver = StorageDriver.postgres(
            // TODO may want to add some validation to the original url (e.g. verify it was postgresql)
            jdbcUrl = env[Settings.postgresDatabaseUri].scheme("jdbc:postgresql"),
            credentials = Credentials(
                user = env[Settings.postgresDatabaseUsername],
                password = env[Settings.postgresDatabasePassword].use { it }
            ),
            pageSize = PageSize.of(100)
        ),
        plugins = plugins,
        config = coreConfig
    )

    FeatureControl(
        appName = appName,
        core = core,
        plugins = plugins.map { it.create(core) },
        eventBusFn = ::localEventBus,
        config = coreConfig
    )
        .getRoutes()
        .withFilter(ServerFilters.GZip())
        .asServer(Undertow(env[Settings.port].value))
        .start()
        .also { println("Started on http://localhost:${it.port()}") }
        .block()
}