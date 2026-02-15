package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.appName3
import com.zalphion.featurecontrol.applicationNotEmpty
import com.zalphion.featurecontrol.applicationNotFound
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.createFeature
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.featureKey1
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.staging
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

class ApplicationServiceTest: CoreTestDriver() {

    private val principal = core.users.create(idp1Email1).shouldBeSuccess()
    private val user = principal.user
    private val team = principal.team

    @Test
    fun `create application - success`() {
        val expected = core.applications.create(
            teamId = team.teamId,
            data = ApplicationCreateData(appName1, listOf(dev, prod), emptyMap())
        ).invoke(user, app).shouldBeSuccess()

        core.applications.list(team.teamId)
            .invoke(user, app).shouldBeSuccess()
            .toList().shouldContainExactlyInAnyOrder(expected)
    }

    @Test
    fun `create application - success, name already exists`() {
        val app1 = createApplication(principal, appName1)
        val app2 = createApplication(principal, appName1)

        core.applications.list(team.teamId)
            .invoke(user, app).shouldBeSuccess()
            .toList().shouldContainExactlyInAnyOrder(app1, app2)
    }

    @Test
    fun `list applications - empty`() {
        core.applications.list(team.teamId)
            .invoke(user, app)
            .shouldBeSuccess()
            .toList().shouldBeEmpty()
    }

    @Test
    fun `list applications - paged, success`() {
        val app1 = createApplication(principal, appName1)
        val app2 = createApplication(principal, appName2)
        val app3 = createApplication(principal, appName3)

        val page1 = core.applications.list(team.teamId).invoke(user, app).shouldBeSuccess()[null]
        page1.items.shouldHaveSize(2)
        page1.next.shouldNotBeNull()

        val page2 = core.applications.list(team.teamId).invoke(user, app).shouldBeSuccess()[page1.next]
        page2.items.shouldHaveSize(1)
        page2.next.shouldBeNull()

        page1.items.plus(page2.items).shouldContainExactlyInAnyOrder(app1, app2, app3)
    }

    @Test
    fun `delete application - not found`() {
        val appId = AppId.random(core.random)
        core.applications.delete(team.teamId, appId).invoke(user, app).shouldBeFailure(applicationNotFound(appId))
    }

    @Test
    fun `delete application - success`() {
        val app1 = createApplication(principal, appName1)
        val app2 = createApplication(principal, appName2)

        core.applications.delete(app1.teamId, app1.appId).invoke(user, app).shouldBeSuccess()

        core.applications.list(team.teamId)
            .invoke(user, app).shouldBeSuccess()
            .toList().shouldContainExactlyInAnyOrder(app2)
    }

    @Test
    fun `delete application - still has features`() {
        val app1 = createApplication(principal, appName1)

        createFeature(principal, app1, featureKey1)

        core.applications.delete(app1.teamId, app1.appId)
            .invoke(user, app)
            .shouldBeFailure(
                applicationNotEmpty(app1.appId)
        )
    }

    @Test
    fun `update application - doesn't delete features`() {
        val app1 = createApplication(principal, appName1)
        createFeature(principal, app1, featureKey1)
        core.features.list(app1.teamId, app1.appId)
            .invoke(user, app).shouldBeSuccess()
            .toList().shouldHaveSize(1)

        core.applications.update(
            teamId = team.teamId,
            appId = app1.appId,
            data = ApplicationUpdateData(
                appName = appName2,
                environments = listOf(dev, staging, prod),
                extensions = emptyMap()
            )
        ).invoke(user, app) shouldBeSuccess app1.copy(
            appName = appName2,
            environments = listOf(dev, staging, prod)
        )

        core.features.list(app1.teamId, app1.appId)
            .invoke(user, app).shouldBeSuccess()
            .toList().shouldHaveSize(1)
    }
}