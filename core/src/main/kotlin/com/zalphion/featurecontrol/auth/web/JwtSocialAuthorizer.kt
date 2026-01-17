package com.zalphion.featurecontrol.auth.web

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.zalphion.featurecontrol.users.EmailAddress
import com.zalphion.featurecontrol.users.UserCreateData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Uri
import java.net.URI
import java.time.Clock
import java.util.Date

fun SocialAuthorizer.Companion.jwt(
    audience: List<String>,
    clock: Clock,
    issuer: String,
    algorithm: JWSAlgorithm = JWSAlgorithm.RS256,
    jwkSource: JWKSource<SecurityContext> // TODO replace with faked JWKS
): SocialAuthorizer {
    val logger = KotlinLogging.logger {  }

    val processor = DefaultJWTProcessor<SecurityContext>().apply {
        jwtClaimsSetVerifier = object: DefaultJWTClaimsVerifier<SecurityContext>(
            JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .build(),
            emptySet()
        ) {
            override fun currentTime() = Date.from(clock.instant())
        }
        jwsKeySelector = JWSVerificationKeySelector(algorithm, jwkSource)
    }

    return SocialAuthorizer { idToken ->
        val claims = try {
            processor.process(idToken, null)
        } catch (e: Exception) {
            logger.debug(e) { "Failed to process JWT" }
            return@SocialAuthorizer null
        }

        UserCreateData(
            userName = claims.getStringClaim("name") ?: claims.getStringClaim("email"),
            emailAddress = EmailAddress.parse(claims.getStringClaim("email")), // TODO gracefully handle null,
            photoUrl = claims.getStringClaim("picture")?.let(Uri::of)
        )
    }
}

fun SocialAuthorizer.Companion.google(clientId: String, clock: Clock) = jwt(
    audience = listOf(clientId),
    clock = clock,
    issuer = "https://accounts.google.com",
    jwkSource = JWKSourceBuilder
        .create<SecurityContext>(URI.create("https://www.googleapis.com/oauth2/v3/certs").toURL())
        .build()
)