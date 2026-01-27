package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.emails.EmailSender
import com.zalphion.featurecontrol.emails.email
import com.zalphion.featurecontrol.web.LOGIN_PATH
import com.zalphion.featurecontrol.emails.smtp
import com.zalphion.featurecontrol.events.localEventBus
import com.zalphion.featurecontrol.plugins.Plugin
import com.zalphion.featurecontrol.plugins.PluginFactory
import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.postgres
import org.http4k.config.Environment
import org.http4k.core.Credentials
import org.http4k.core.Uri
import org.http4k.filter.ServerFilters
import org.http4k.server.Undertow
import org.http4k.server.asServer
import java.security.SecureRandom
import java.time.Clock
import kotlin.random.asKotlinRandom

fun main() = hostedCoreMain()

fun hostedCoreMain(
    env: Environment = Environment.ENV,
    plugins: List<PluginFactory<*>> = emptyList()
) = createCore(
    clock = Clock.systemUTC(),
    random = SecureRandom().asKotlinRandom(),
    config = CoreConfig(
        appSecret = env[Settings.appSecret],
        staticUri = Uri.of("/"), // vendored in jar
        origin = env[Settings.origin],
        teamsStorageName = "teams",
        configEnvironmentsTableName = "config_environments",
        usersStorageName = "users",
        membersStorageName = "members",
        applicationsStorageName = "applications",
        featuresStorageName = "features",
        configsStorageName = "configs",
        apiKeysStorageName = "api_keys",
        googleClientId = env[Settings.googleClientId],
        csrfTtl = env[Settings.csrfTtl],
        sessionLength = env[Settings.sessionLength],
        invitationRetention = env[Settings.invitationsRetention]
    ),
    storageDriver = StorageDriver.postgres(
        uri = env[Settings.postgresDatabaseUri].scheme("jdbc:postgresql"),
        credentials = env[Settings.postgresDatabaseCredentials],
        pageSize = PageSize.of(100)
    ),
    eventBusFn = ::localEventBus,
    plugins = listOf(
        *plugins.toTypedArray(),
        Plugin.email(
            loginUri = env[Settings.origin].path(LOGIN_PATH),
            emails = EmailSender.smtp(
                fromName = env[Settings.smtpFromName],
                fromAddress = env[Settings.smtpFromAddress],
                authority = env[Settings.smtpAuthority],
                credentials = env[Settings.smtpUsername]?.let { username ->
                    Credentials(username, env[Settings.smtpPassword].use { it })
                },
                startTls = env[Settings.smtpStartTls],
            )
        )
    )
)
    .getRoutes()
    .withFilter(ServerFilters.GZip())
    .asServer(Undertow(env[Settings.port].value))
    .start()
    .also { println("Started on http://localhost:${it.port()}") }
    .block()