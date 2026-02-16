package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.alwaysOn
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.applicationNotFound
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.createFeature
import com.zalphion.featurecontrol.devName
import com.zalphion.featurecontrol.featureAlreadyExists
import com.zalphion.featurecontrol.featureKey1
import com.zalphion.featurecontrol.featureKey2
import com.zalphion.featurecontrol.featureKey3
import com.zalphion.featurecontrol.featureNotFound
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.lib.Update
import com.zalphion.featurecontrol.mostlyOff
import com.zalphion.featurecontrol.off
import com.zalphion.featurecontrol.oldNewData
import com.zalphion.featurecontrol.on
import com.zalphion.featurecontrol.prodName
import com.zalphion.featurecontrol.toCreate
import dev.andrewohara.utils.pagination.Page
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration

class FeatureServiceTest: CoreTestDriver() {

    private val user = core.users.create(idp1Email1).shouldBeSuccess()

    private val application1 = createApplication(user, appName1)
    private val application2 = createApplication(user, appName2)

    // TODO test create/update toggle using environment not in application

    @Test
    fun `create feature - application not found`() {
        val appId = AppId.random(core.random)
        core.features.create(user.team.teamId, appId, oldNewData.toCreate(featureKey1))
            .invoke(core, user.user)
            .shouldBeFailure(applicationNotFound(appId))
    }

    @Test
    fun `create feature - already exists`() {
        val existing = createFeature(user, application1, featureKey1)

        core.features.create(application1.teamId, application1.appId, oldNewData.toCreate(featureKey1))
            .invoke(core, user.user)
            .shouldBeFailure(featureAlreadyExists(existing.appId, existing.key))
    }

    @Test
    fun `create feature - success`() {
        val expected = Feature(
            teamId = application1.teamId,
            appId = application1.appId,
            key = featureKey1,
            variants = mapOf(off to "off", on to "on"),
            environments = mapOf(
                devName to alwaysOn,
                prodName to mostlyOff
            ),
            defaultVariant = off,
            description = "cool stuff",
            extensions = mapOf("foo" to "bar")
        )

        core.features.create(
            teamId = application1.teamId,
            appId = application1.appId,
            data = FeatureCreateData(
                featureKey = featureKey1,
                variants = mapOf(off to "off", on to "on"),
                defaultVariant = off,
                environments = mapOf(
                    devName to alwaysOn,
                    prodName to mostlyOff
                ),
                description = "cool stuff",
                extensions = mapOf("foo" to "bar")
            )
        ).invoke(core, user.user) shouldBeSuccess expected

        core.features.list(application1.teamId, application1.appId)
            .invoke(core, user.user)
            .shouldBeSuccess()
            .toList().shouldContainExactlyInAnyOrder(expected)
    }

    @Test
    fun `create feature - success, duplicate key in other application`() {
        val toggle1 = createFeature( user, application1, featureKey1)
        val toggle2 = createFeature( user, application2, featureKey1)

        core.features.list(application1.teamId, application1.appId)
            .invoke(core, user.user)
            .shouldBeSuccess()
            .toList().shouldContainExactlyInAnyOrder(toggle1)

        core.features.list(application2.teamId, application2.appId)
            .invoke(core, user.user)
            .shouldBeSuccess()
            .toList().shouldContainExactlyInAnyOrder(toggle2)
    }

    @Test
    fun `list features - paged, success`() {
        val toggle1 = createFeature(user, application1, featureKey1)
        val toggle2 = createFeature(user, application1, featureKey2)
        val toggle3 = createFeature(user, application1, featureKey3)
        val toggle4 = createFeature(user, application2, featureKey1)

        val paginator = core.features.list(application1.teamId, application1.appId)
            .invoke(core, user.user)
            .shouldBeSuccess()

        paginator[null] shouldBe Page(
            items = listOf(toggle1, toggle2),
            next = toggle2.key
        )

        paginator[toggle2.key] shouldBe Page(
            items = listOf(toggle3),
            next = null
        )

        core.features.list(application2.teamId, application2.appId)
            .invoke(core, user.user)
            .shouldBeSuccess()
            .toList()
            .shouldContainExactlyInAnyOrder(toggle4)
    }

    @Test
    fun `update feature - toggle not found`() {
        core.features.update(application1.teamId, application1.appId, featureKey1, oldNewData)
            .invoke(core, user.user)
            .shouldBeFailure(featureNotFound(application1.appId, featureKey1))
    }

    @Test
    fun `update feature - application not found`() {
        val appId = AppId.random(core.random)

        core.features.update(user.team.teamId, appId, featureKey1, oldNewData)
            .invoke(core, user.user)
            .shouldBeFailure(applicationNotFound(appId))
    }

    @Test
    fun `update feature - success`() {
        val toggle1 = createFeature(user, application1, featureKey1)

        val toggle2 = createFeature(
            principal = user,
            application = application1,
            featureKey = featureKey2,
            variants = mapOf(off to "off", on to "on"),
            defaultVariant = off,
            environments = mapOf(devName to mostlyOff, prodName to mostlyOff)
        )

        time += Duration.ofSeconds(5)

        val expected = toggle2.copy(
            environments = mapOf(devName to alwaysOn, prodName to mostlyOff),
            defaultVariant = on,
            description = "new description",
            extensions = emptyMap()
        )

        core.features.update(
            teamId = application1.teamId,
            appId = application1.appId,
            featureKey = featureKey2,
            data = FeatureUpdateData(
                variants = Update(mapOf(off to "off", on to "on")),
                environmentsToUpdate = Update(
                    mapOf(
                        devName to alwaysOn, prodName to mostlyOff
                    )
                ),
                defaultVariant = Update(on),
                description = Update("new description"),
                extensions = null
            )
        ).invoke(core, user.user) shouldBeSuccess expected

        core.features.list(application1.teamId, application1.appId)
            .invoke(core, user.user)
            .shouldBeSuccess()
            .toList()
            .shouldContainExactlyInAnyOrder(toggle1, expected)
    }

    @Test
    fun `get feature - application not found`() {
        val appId = AppId.random(core.random)
        core.features.get(user.team.teamId, appId, featureKey1)
            .invoke(core, user.user)
            .shouldBeFailure(applicationNotFound(appId))
    }

    @Test
    fun `get feature - feature not found`() {
        core.features.get(application1.teamId, application1.appId, featureKey1)
            .invoke(core, user.user)
            .shouldBeFailure(featureNotFound(application1.appId, featureKey1))
    }

    @Test
    fun `get feature - success`() {
        val toggle = createFeature(user, application1, featureKey1)

        core.features.get(application1.teamId, application1.appId, featureKey1)
            .invoke(core, user.user) shouldBeSuccess toggle
    }

    @Test
    fun `delete feature - application not found`() {
        val appId = AppId.random(core.random)
        core.features.delete(user.team.teamId, appId, featureKey1)
            .invoke(core, user.user)
            .shouldBeFailure(applicationNotFound(appId))
    }

    @Test
    fun `delete feature - feature not found`() {
        core.features.delete(application1.teamId, application1.appId, featureKey1)
            .invoke(core, user.user)
            .shouldBeFailure(featureNotFound(application1.appId, featureKey1))
    }

    @Test
    fun `delete feature - success`() {
        val feature1 = createFeature(user, application1, featureKey1)

        val feature2 = createFeature(user, application1, featureKey2)

        core.features.delete(application1.teamId, application1.appId, featureKey1)
            .invoke(core, user.user)
            .shouldBeSuccess(feature1)

        core.features.list(application1.teamId, application1.appId)
            .invoke(core, user.user)
            .shouldBeSuccess()
            .toList().shouldContainExactlyInAnyOrder(feature2)
    }
}