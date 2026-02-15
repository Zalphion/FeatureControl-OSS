package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.StorageTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.ApplicationStorage
import com.zalphion.featurecontrol.booleanProperty
import com.zalphion.featurecontrol.crypto.AppSecret
import com.zalphion.featurecontrol.crypto.Encryption
import com.zalphion.featurecontrol.crypto.aesGcm
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.devName
import com.zalphion.featurecontrol.numberProperty
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.prodName
import com.zalphion.featurecontrol.secretProperty
import com.zalphion.featurecontrol.staging
import com.zalphion.featurecontrol.strProperty
import com.zalphion.featurecontrol.teams.TeamId
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ConfigStorageTest {

    private val driver = StorageTestDriver()
    private val encryption = Encryption.aesGcm(AppSecret.of("secret"), "encryption", driver.random, null)

    private val applications = driver.create(ApplicationStorage)

    private val application1 = Application(
        teamId = TeamId.random(driver.random),
        appId = AppId.random(driver.random),
        appName = appName1,
        environments = listOf(dev, prod),
        extensions = emptyMap()
    ).also(applications::plusAssign)

    private val application2 = Application(
        teamId = TeamId.random(driver.random),
        appId = AppId.random(driver.random),
        appName = appName2,
        environments = listOf(dev, staging, prod),
        extensions = emptyMap()
    ).also(applications::plusAssign)

    private val specs = driver.create(ConfigSpecStorage)
    private val environments = driver.create(ConfigEnvironmentStorage)

    @Test
    fun `get properties - missing`() {
        specs[application1.teamId, application1.appId] shouldBe null
    }

    @Test
    fun `get properties - found`() {
        val config1 = ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = mapOf(strProperty, numberProperty, booleanProperty, secretProperty)
        ).also(specs::plusAssign)

        val config2 = ConfigSpec(
            teamId = application2.teamId,
            appId = application2.appId,
            properties = mapOf(strProperty)
        ).also(specs::plusAssign)

        specs[application1.teamId, application1.appId] shouldBe config1
        specs[application2.teamId, application2.appId] shouldBe config2
    }

    @Test
    fun `get properties - found, empty`() {
        val properties = ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = emptyMap()
        )
        specs += properties

        specs[application1.teamId, application1.appId] shouldBe properties
    }

    @Test
    fun `update properties`() {
        val properties = ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = mapOf(strProperty)
        ).also(specs::plusAssign)

        val updated = properties.copy(
            properties = mapOf(strProperty, numberProperty, booleanProperty, secretProperty),
        ).also(specs::plusAssign)

        specs[application1.teamId, application1.appId] shouldBe updated
    }

    @Test
    fun `get values`() {
        val values = ConfigEnvironment(
            teamId = application1.teamId,
            appId = application1.appId,
            name = devName,
            values = mapOf(
                strProperty.first to "foo",
                numberProperty.first to "123",
                booleanProperty.first to "true",
                secretProperty.first to encryption.encrypt("lolcats").decodeToString(),
            )
        ).also(environments::plusAssign)

        environments[application1.appId, devName] shouldBe values
        environments[application1.appId, prodName] shouldBe null
    }

    @Test
    fun `get values - not found`() {
        environments[application1.appId, devName] shouldBe null
    }

    @Test
    fun `update values`() {
        val original = ConfigEnvironment(
            teamId = application1.teamId,
            appId = application1.appId,
            name = devName,
            values = mapOf(
                strProperty.first to "foo",
            )
        ).also(environments::plusAssign)

        val updated = original.copy(
            values = mapOf(
                numberProperty.first to "123",
                booleanProperty.first to "true",
            )
        ).also(environments::plusAssign)

        environments[application1.appId, devName] shouldBe updated
    }

    @Test
    fun `delete properties - not found`() {
        specs.delete(application1.teamId, application1.appId)
    }

    @Test
    fun `delete properties - doesn't delete values`() {
        ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = mapOf(strProperty)
        ).also(specs::plusAssign)

        ConfigEnvironment(
            teamId = application1.teamId,
            appId = application1.appId,
            name = devName,
            values = mapOf(
                strProperty.first to "foo"
            )
        ).also(environments::plusAssign)

        specs.delete(application1.teamId, application1.appId)

        specs[application1.teamId, application1.appId].shouldBeNull()
        environments[application1.appId, devName].shouldNotBeNull()
    }

    @Test
    fun `delete values - not found`() {
        environments.delete(application1.appId, devName)
    }

    @Test
    fun `delete values - leaves properties intact`() {
        val config = ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = mapOf(strProperty)
        ).also(specs::plusAssign)

        ConfigEnvironment(
            teamId = application1.teamId,
            appId = application1.appId,
            name = devName,
            values = mapOf(
                strProperty.first to "foo"
            )
        ).also(environments::plusAssign)

        environments.delete(application1.appId, devName)
        environments[application1.appId, devName] shouldBe null
        specs[application1.teamId, application1.appId] shouldBe config
    }
}