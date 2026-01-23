package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.Application
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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ConfigStorageTest: CoreTestDriver() {

    private val encryption = Encryption.aesGcm(AppSecret.of("secret"), "encryption", core.random, null)

    private val application1 = Application(
        teamId = TeamId.random(core.random),
        appId = AppId.random(core.random),
        appName = appName1,
        environments = listOf(dev, prod),
        extensions = emptyMap()
    ).also(core.apps::plusAssign)

    private val application2 = Application(
        teamId = TeamId.random(core.random),
        appId = AppId.random(core.random),
        appName = appName2,
        environments = listOf(dev, staging, prod),
        extensions = emptyMap()
    ).also(core.apps::plusAssign)

    private val testObj = core.configs

    @Test
    fun `get properties - missing`() {
        testObj[application1.teamId, application1.appId] shouldBe null
    }

    @Test
    fun `get properties - found`() {
        val config1 = ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = mapOf(strProperty, numberProperty, booleanProperty, secretProperty)
        ).also(testObj::plusAssign)

        val config2 = ConfigSpec(
            teamId = application2.teamId,
            appId = application2.appId,
            properties = mapOf(strProperty)
        ).also(testObj::plusAssign)

        testObj[application1.teamId, application1.appId] shouldBe config1
        testObj[application2.teamId, application2.appId] shouldBe config2
    }

    @Test
    fun `get properties - found, empty`() {
        val properties = ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = emptyMap()
        )
        testObj += properties

        testObj[application1.teamId, application1.appId] shouldBe properties
    }

    @Test
    fun `update properties`() {
        val properties = ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = mapOf(strProperty)
        ).also(testObj::plusAssign)

        val updated = properties.copy(
            properties = mapOf(strProperty, numberProperty, booleanProperty, secretProperty),
        ).also(testObj::plusAssign)

        testObj[application1.teamId, application1.appId] shouldBe updated
    }

    @Test
    fun `get values`() {
        val values = ConfigEnvironment(
            teamId = application1.teamId,
            appId = application1.appId,
            environmentName = devName,
            values = mapOf(
                strProperty.first to PropertyValue(PropertyType.String, "foo"),
                numberProperty.first to PropertyValue(PropertyType.Number, "123"),
                booleanProperty.first to PropertyValue(PropertyType.Boolean, "true"),
                secretProperty.first to PropertyValue(PropertyType.Secret, encryption.encrypt("lolcats").decodeToString()),
            )
        ).also(testObj::plusAssign)

        testObj[application1.appId, devName] shouldBe values
        testObj[application1.appId, prodName] shouldBe null
    }

    @Test
    fun `get values - not found`() {
        testObj[application1.appId, devName] shouldBe null
    }

    @Test
    fun `update values`() {
        val original = ConfigEnvironment(
            teamId = application1.teamId,
            appId = application1.appId,
            environmentName = devName,
            values = mapOf(
                strProperty.first to PropertyValue(PropertyType.String, "foo"),
            )
        ).also(testObj::plusAssign)

        val updated = original.copy(
            values = mapOf(
                numberProperty.first to PropertyValue(PropertyType.Number, "123"),
                booleanProperty.first to PropertyValue(PropertyType.Boolean, "true"),
            )
        ).also(testObj::plusAssign)

        testObj[application1.appId, devName] shouldBe updated
    }

    @Test
    fun `delete properties - not found`() {
        testObj.delete(application1.teamId, application1.appId)
    }

    @Test
    fun `delete properties - deletes values too`() {
        ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = mapOf(strProperty)
        ).also(testObj::plusAssign)

        ConfigEnvironment(
            teamId = application1.teamId,
            appId = application1.appId,
            environmentName = devName,
            values = mapOf(
                strProperty.first to PropertyValue(PropertyType.String, "foo")
            )
        )

        testObj.delete(application1.teamId, application1.appId)

        testObj[application1.teamId, application1.appId] shouldBe null
        testObj[application1.appId, devName] shouldBe null
    }

    @Test
    fun `delete values - not found`() {
        testObj.delete(application1.appId, devName)
    }

    @Test
    fun `delete values - leaves properties intact`() {
        val config = ConfigSpec(
            teamId = application1.teamId,
            appId = application1.appId,
            properties = mapOf(strProperty)
        ).also(testObj::plusAssign)

        ConfigEnvironment(
            teamId = application1.teamId,
            appId = application1.appId,
            environmentName = devName,
            values = mapOf(
                strProperty.first to PropertyValue(PropertyType.String, "foo")
            )
        ).also(testObj::plusAssign)

        testObj.delete(application1.appId, devName)
        testObj[application1.appId, devName] shouldBe null
        testObj[application1.teamId, application1.appId] shouldBe config
    }
}