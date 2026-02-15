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
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.numberProperty
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.secretProperty
import com.zalphion.featurecontrol.stagingName
import com.zalphion.featurecontrol.strProperty
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import org.junit.jupiter.api.Test

class ConfigServiceTest: CoreTestDriver() {

    private val user1 = core.users.create(idp1Email1).shouldBeSuccess()
    private val app1 = createApplication(user1, appName1, listOf(dev, prod))

    private val user2 = core.users.create(idp1Email2).shouldBeSuccess()
    private val app2 = createApplication(user2, appName2, listOf(dev, prod))

    @Test
    fun `get config - not found`() {
        val id = AppId.random(core.random)
        core.configs.getSpec(user1.team.teamId,id)
            .invoke(user1.user, app)
            .shouldBeFailure(applicationNotFound(id))
    }

    @Test
    fun `get config - not on team`() {
        core.configs.getSpec(app1.teamId, app1.appId)
            .invoke(user2.user, app)
            .shouldBeFailure(forbidden)
    }

    @Test
    fun `get config - empty`() {
        core.configs.getSpec(app2.teamId, app2.appId)
            .invoke(user2.user, app)
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
        val expected = core.configs.updateSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty, numberProperty)
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.getSpec(app1.teamId, app1.appId)
            .invoke(user1.user, app)
            .shouldBeSuccess(expected)
    }

    @Test
    fun `update config properties - not found`() {
        val id = AppId.random(core.random)
        core.configs.updateSpec(user1.team.teamId, id, mapOf(strProperty, numberProperty))
            .invoke(user1.user, app)
            .shouldBeFailure(applicationNotFound(id))
    }

    @Test
    fun `update config properties - success`() {
        val expected = ConfigSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty, numberProperty)
        )

        core.configs.updateSpec(app1.teamId, app1.appId, mapOf(strProperty, numberProperty))
            .invoke(user1.user, app)
            .shouldBeSuccess(expected)

        core.configs.getSpec(app1.teamId, app1.appId)
            .invoke(user1.user, app)
            .shouldBeSuccess(expected)
    }

    @Test
    fun `update config values - application not found`() {
        val id = AppId.random(core.random)
        core.configs.updateEnvironment(
            user1.team.teamId, id, devName, mapOf(
                PropertyKey.parse("str") to "foo"
            )
        ).invoke(user1.user, app) shouldBeFailure applicationNotFound(id)
    }

    @Test
    fun `update config values - environment not found`() {
        core.configs.updateEnvironment(
            app1.teamId, app1.appId, stagingName, mapOf(
                PropertyKey.parse("str") to "foo"
            )
        )
            .invoke(user1.user, app)
            .shouldBeFailure(environmentNotFound(app1.appId, stagingName))
    }

    @Test
    fun `update config values - property not found`() {
        core.configs.updateEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(
                PropertyKey.parse("str") to "foo"
            )
        )
            .invoke(user1.user, app)
            .shouldBeFailure(propertyNotFound(app1.appId, PropertyKey.parse("str")))
    }

    @Test
    fun `update config values - success`() {
        core.configs.updateSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty, numberProperty)
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.updateEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(
                PropertyKey.parse("str") to "lolcats",
                PropertyKey.parse("num") to "123",
            )
        )
            .invoke(user1.user, app)
            .shouldBeSuccess(
                ConfigEnvironment(
                    teamId = app1.teamId,
                    appId = app1.appId,
                    name = devName,
                    values = mapOf(
                        PropertyKey.parse("str") to "lolcats",
                        PropertyKey.parse("num") to "123"
                    )
                )
            )

        core.configs.getEnvironment(app1.teamId, app1.appId, devName)
            .invoke(user1.user, app)
            .shouldBeSuccess(ConfigEnvironment(
                teamId = app1.teamId,
                appId = app1.appId,
                name = devName,
                values = mapOf(
                    PropertyKey.parse("str") to "lolcats",
                    PropertyKey.parse("num") to "123"
                )
            ))
    }

    @Test
    fun `update config values - replaces omitted values`() {
        core.configs.updateSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty, numberProperty)
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.updateEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(
                PropertyKey.parse("str") to "foo",
                PropertyKey.parse("num") to "123"
            )
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.updateEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(
                PropertyKey.parse("num") to "456"
            )
        )
            .invoke(user1.user, app)
            .shouldBeSuccess(
                ConfigEnvironment(
                    teamId = app1.teamId,
                    appId = app1.appId,
                    name = devName,
                    values = mapOf(
                        PropertyKey.parse("num") to "456"
                    )
                )
            )

        core.configs.getEnvironment(app1.teamId, app1.appId, devName)
            .invoke(user1.user, app)
            .shouldBeSuccess(ConfigEnvironment(
                teamId = app1.teamId,
                appId = app1.appId,
                name = devName,
                values = mapOf(
                    PropertyKey.parse("num") to "456"
                )
            ))
    }

    @Test
    fun `update config values - blank values omitted`() {
        core.configs.updateSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty)
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.updateEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(strProperty.first to "  ")
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.getEnvironment(app1.teamId, app1.appId, devName)
            .invoke(user1.user, app)
            .shouldBeSuccess(ConfigEnvironment(
                teamId = app1.teamId,
                appId = app1.appId,
                name = devName,
                values = emptyMap()
            ))
    }

    @Test
    fun `update config values - values trimmed`() {
        core.configs.updateSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(strProperty)
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.updateEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(strProperty.first to " lol ")
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.getEnvironment(app1.teamId, app1.appId, devName)
            .invoke(user1.user, app)
            .shouldBeSuccess(ConfigEnvironment(
                teamId = app1.teamId,
                appId = app1.appId,
                name = devName,
                values = mapOf(
                    strProperty.first to "lol"
                )
            ))
    }

    @Test
    fun `update config values - secrets encrypted`() {
        core.configs.updateSpec(
            teamId = app1.teamId,
            appId = app1.appId,
            properties = mapOf(secretProperty)
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.updateEnvironment(
            teamId = app1.teamId,
            appId = app1.appId,
            environmentName = devName,
            data = mapOf(secretProperty.first to "lol")
        ).invoke(user1.user, app).shouldBeSuccess()

        core.configs.getEnvironment(app1.teamId, app1.appId, devName)
            .invoke(user1.user, app)
            .shouldBeSuccess(ConfigEnvironment(
                teamId = app1.teamId,
                appId = app1.appId,
                name = devName,
                values = mapOf(
                    secretProperty.first to "80c39b4246a0310e7a82df20883b9ec82dbcc13705f7e1889482a8a210e1aa"
                )
            ))
    }
}