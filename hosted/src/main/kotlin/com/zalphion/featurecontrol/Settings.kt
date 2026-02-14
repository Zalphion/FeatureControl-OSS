package com.zalphion.featurecontrol

import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.users.EmailAddress
import org.http4k.config.EnvironmentKey
import org.http4k.config.Port
import org.http4k.core.Credentials
import org.http4k.lens.authority
import org.http4k.lens.boolean
import org.http4k.lens.composite
import org.http4k.lens.duration
import org.http4k.lens.port
import org.http4k.lens.secret
import org.http4k.lens.string
import org.http4k.lens.uri
import org.http4k.lens.value
import java.time.Duration

object Settings {
    val port = EnvironmentKey.port().defaulted("PORT", Port(80))
    val appSecret = EnvironmentKey.value(AppSecret).required("APP_SECRET")
    val origin = EnvironmentKey.uri().required("ORIGIN")  // used to build redirect URIs and server binding
    val sessionLength = EnvironmentKey.duration().defaulted("SESSION_LENGTH", Duration.ofDays(7))
    val googleClientId = EnvironmentKey.string().optional("GOOGLE_CLIENT_ID")
    val invitationsRetention = EnvironmentKey.duration().defaulted("INVITATIONS_RETENTION", Duration.ofDays(7))
    val csrfTtl = EnvironmentKey.duration().defaulted("CSRF_TTL", Duration.ofHours(8))

    // postgres
    val postgresDatabaseUri = EnvironmentKey.uri().required("POSTGRES_URI")
    val postgresDatabaseUsername = EnvironmentKey.string().required("POSTGRES_USERNAME")
    val postgresDatabasePassword = EnvironmentKey.secret().required("POSTGRES_PASSWORD")

    // smtp
    val smtpAuthority = EnvironmentKey.authority().required("SMTP_AUTHORITY")
    val smtpUsername = EnvironmentKey.string().optional("SMTP_USERNAME")
    val smtpPassword = EnvironmentKey.secret().required("SMTP_PASSWORD")
    val smtpFromName = EnvironmentKey.string().defaulted("SMTP_FROM_NAME", APP_NAME)
    val smtpFromAddress = EnvironmentKey.value(EmailAddress).required("SMTP_FROM_ADDRESS")
    val smtpStartTls = EnvironmentKey.boolean().defaulted("SMTP_STARTTLS", true)
}