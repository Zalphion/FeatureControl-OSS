package com.zalphion.featurecontrol.configs

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.applicationNotFound
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.devName
import com.zalphion.featurecontrol.environmentNotFound
import com.zalphion.featurecontrol.forbidden
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.idp1Email2
import com.zalphion.featurecontrol.members.UserRole
import com.zalphion.featurecontrol.numberProperty
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.secretProperty
import com.zalphion.featurecontrol.setRole
import com.zalphion.featurecontrol.stagingName
import com.zalphion.featurecontrol.strProperty
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ConfigServiceTest: CoreTestDriver() {

    private val user1 = users.create(idp1Email1).shouldBeSuccess()
    private val app1 = createApplication(user1, appName1, listOf(dev, prod))

    private val user2 = users.create(idp1Email2).shouldBeSuccess()
    private val app2 = createApplication(user2, appName2, listOf(dev, prod))

    @Test
    fun `get config - not found`() {
        val id = AppId.random(core.random)
        GetConfigSpec(user1.team.teamId,id)
            .invoke(user1.user, core)
            .shouldBeFailure(applicationNotFound(id))
    }

    @Test
    fun `get config - not on team`() {
        GetConfigSpec(app1.teamId, app1.appId)
            .invoke(user2.user, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `get config - not testers allowed`() {
        user1.member.setRole(core, UserRole.Tester)

        GetConfigSpec(app1.teamId, app1.appId)
            .invoke(user1.user, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `get config - empty`() {
        GetConfigSpec(app2.teamId, app2.appId)
            .invoke(user2.user, core)
            .shouldBeSuccess(
                ConfigSpec(
                    teamId = app2.teamId,
                    appId = app2.appId,
                    properties = emptyMap()
                )
            )
    }

    @Test
    fun `get config - not empty`() {
        val expected = ConfigSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty, numberProperty)
        ).also(core.configs::plusAssign)

        GetConfigSpec(app1.teamId, app1.appId)
            .invoke(user1.user, core)
            .shouldBeSuccess(expected)
    }

    @Test
    fun `update config properties - not found`() {
        val id = AppId.random(core.random)
        UpdateConfigSpec(user1.team.teamId, id, mapOf(strProperty, numberProperty))
            .invoke(user1.user, core)
            .shouldBeFailure(applicationNotFound(id))
    }

    @Test
    fun `update config properties - no testers allowed`() {
        user1.member.setRole(core, UserRole.Tester)
        UpdateConfigSpec(app1.teamId, app1.appId, mapOf(strProperty, numberProperty))
            .invoke(user1.user, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `update config properties - success`() {
        val expected = ConfigSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty, numberProperty)
        )

        UpdateConfigSpec(app1.teamId, app1.appId, mapOf(strProperty, numberProperty))
            .invoke(user1.user, core)
            .shouldBeSuccess(expected)

        core.configs[app1.teamId, app1.appId] shouldBe expected
    }

    @Test
    fun `update config values - application not found`() {
        val id = AppId.random(core.random)
        UpdateConfigEnvironment(
            user1.team.teamId, id, devName, mapOf(
                PropertyKey.parse("str") to "foo"
            )
        )
    }

    @Test
    fun `update config values - environment not found`() {
        UpdateConfigEnvironment(
            app1.teamId, app1.appId, stagingName, mapOf(
                PropertyKey.parse("str") to "foo"
            )
        )
            .invoke(user1.user, core)
            .shouldBeFailure(environmentNotFound(app1.appId, stagingName))
    }

    @Test
    fun `update config values - no testers allowed`() {
        user1.member.setRole(core, UserRole.Tester)
        UpdateConfigEnvironment(
            app1.teamId, app1.appId, devName, mapOf(
                PropertyKey.parse("str") to "foo"
            )
        )
            .invoke(user1.user, core)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `update config values - property not found`() {
        UpdateConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(
                PropertyKey.parse("str") to "foo"
            )
        )
            .invoke(user1.user, core)
            .shouldBeFailure(propertyNotFound(app1.appId, PropertyKey.parse("str")))
    }

    @Test
    fun `update config values - success`() {
        core.configs += ConfigSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty, numberProperty)
        )

        UpdateConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(
                PropertyKey.parse("str") to "lolcats",
                PropertyKey.parse("num") to "123",
            )
        )
            .invoke(user1.user, core)
            .shouldBeSuccess(
                ConfigEnvironment(
                    teamId = app1.teamId,
                    appId = app1.appId,
                    environmentName = devName,
                    values = mapOf(
                        PropertyKey.parse("str") to PropertyValue(PropertyType.String,"lolcats"),
                        PropertyKey.parse("num") to PropertyValue(PropertyType.Number, "123")
                    )
                )
            )

        core.configs[app1.appId, devName] shouldBe ConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            values = mapOf(
                PropertyKey.parse("str") to PropertyValue(PropertyType.String,"lolcats"),
                PropertyKey.parse("num") to PropertyValue(PropertyType.Number, "123")
            )
        )
    }

    @Test
    fun `update config values - replaces omitted values`() {
        core.configs += ConfigSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty, numberProperty)
        )

        core.configs += ConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            values = mapOf(
                PropertyKey.parse("str") to PropertyValue(PropertyType.String,"foo"),
                PropertyKey.parse("num") to PropertyValue(PropertyType.Number, "123")
            )
        )

        UpdateConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(
                PropertyKey.parse("num") to "456"
            )
        )
            .invoke(user1.user, core)
            .shouldBeSuccess(
                ConfigEnvironment(
                    teamId = app1.teamId,
                    appId = app1.appId,
                    environmentName = devName,
                    values = mapOf(
                        PropertyKey.parse("num") to PropertyValue(PropertyType.Number, "456")
                    )
                )
            )

        core.configs[app1.appId, devName] shouldBe ConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            values = mapOf(
                PropertyKey.parse("num") to PropertyValue(PropertyType.Number, "456")
            )
        )
    }

    @Test
    fun `update config values - blank values omitted`() {
        core.configs += ConfigSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty)
        )

        UpdateConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(strProperty.first to "  ")
        ).invoke(user1.user, core).shouldBeSuccess()

        core.configs[app1.appId, devName] shouldBe ConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            values = emptyMap()
        )
    }

    @Test
    fun `update config values - values trimmed`() {
        core.configs += ConfigSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty)
        )

        UpdateConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(strProperty.first to " lol ")
        ).invoke(user1.user, core).shouldBeSuccess()

        core.configs[app1.appId, devName] shouldBe ConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            values = mapOf(
                strProperty.first to PropertyValue(PropertyType.String, "lol")
            )
        )
    }

    @Test
    fun `update config values - secrets encrypted`() {
        core.configs += ConfigSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(secretProperty)
        )

        UpdateConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(secretProperty.first to "lol")
        ).invoke(user1.user, core).shouldBeSuccess()

        core.configs[app1.appId, devName] shouldBe ConfigEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            values = mapOf(
                secretProperty.first to PropertyValue(PropertyType.Secret, "80c39b4246a0310e7a82df2078faa483f4069f8ab673797e7c9514d1be7c0b")
            )
        )
    }
}