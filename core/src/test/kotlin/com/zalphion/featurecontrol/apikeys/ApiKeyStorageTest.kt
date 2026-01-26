package com.zalphion.featurecontrol.apikeys

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.auth.EnginePrincipal
import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.crypto.Base64String
import com.zalphion.featurecontrol.crypto.Encryption
import com.zalphion.featurecontrol.crypto.Signing
import com.zalphion.featurecontrol.crypto.aesGcm
import com.zalphion.featurecontrol.crypto.hMacSha256
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.devName
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.prodName
import com.zalphion.featurecontrol.teams.TeamId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ApiKeyStorageTest: CoreTestDriver() {

    private val apiKeys = core.apiKeys

    private val application = Application(
        teamId = TeamId.random(core.random),
        appId = AppId.random(core.random),
        appName = appName1,
        environments = listOf(dev, prod),
        extensions = emptyMap()
    ).also(core.applications::plusAssign)

    private val principal1 = EnginePrincipal.of(application.appId, devName)
    private val principal2 = EnginePrincipal.of(application.appId, prodName)

    private val signing = Signing.hMacSha256(AppSecret.of("secret"))
    private val encryption = Encryption.aesGcm(AppSecret.of("secret"), "encryption", core.random, null)

    private val apiKey1 = ApiKey.secureRandom()
    private val apiKey1Hash = signing(apiKey1)
    private val apiKey1Encrypted = Base64String.encode(encryption.encrypt(apiKey1))

    private val apiKey2 = ApiKey.secureRandom()
    private val apiKey2Hash = signing(apiKey2)
    private val apiKey2Encrypted = Base64String.encode(encryption.encrypt(apiKey2))

    @Test
    fun `authorize - success`() {
        apiKeys[principal1] = ApiKeyPair(hashed = apiKey1Hash, encrypted = apiKey1Encrypted)
        apiKeys[principal2] = ApiKeyPair(hashed = apiKey2Hash, encrypted = apiKey2Encrypted)

        apiKeys[apiKey1Hash] shouldBe principal1
        apiKeys[apiKey2Hash] shouldBe principal2
    }

    @Test
    fun `authorize - not found`() {
        apiKeys[apiKey1Hash] shouldBe null
    }

    @Test
    fun `retrieve - success`() {
        apiKeys[principal1] = ApiKeyPair(hashed = apiKey1Hash, encrypted = apiKey1Encrypted)
        apiKeys[principal2] = ApiKeyPair(hashed = apiKey2Hash, encrypted = apiKey2Encrypted)

        apiKeys[principal1] shouldBe apiKey1Encrypted
        apiKeys[principal2] shouldBe apiKey2Encrypted
    }

    @Test
    fun `retrieve - not found`() {
        apiKeys[principal1] shouldBe null
    }
}