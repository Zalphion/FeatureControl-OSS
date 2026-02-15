package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.StorageTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.appName3
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.staging
import com.zalphion.featurecontrol.teams.TeamId
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.collections.plus

class ApplicationStorageTest {
    private val driver = StorageTestDriver()

    private val applications = driver.create(ApplicationStorage)
    private val teamId = TeamId.of("team1234")

    private val application1 = Application(
        teamId = teamId,
        appId = AppId.random(driver.random),
        appName = appName1,
        environments = listOf(dev, prod),
        extensions = emptyMap()
    ).also(applications::plusAssign)

    private val application2 = Application(
        teamId = teamId,
        appId = AppId.random(driver.random),
        appName = appName2,
        environments = listOf(dev, prod),
        extensions = emptyMap()
    ).also(applications::plusAssign)

    private val  application3 = Application(
        teamId = teamId,
        appId = AppId.random(driver.random),
        appName = appName3,
        environments = listOf(dev, prod),
        extensions = emptyMap()
    ).also(applications::plusAssign)

    @Test
    fun `list applications - all`() {
        applications.list(teamId).toList().shouldContainExactlyInAnyOrder(application1, application2, application3)
    }

    @Test
    fun `list applications - paged`() {
        val page1 = applications.list(teamId)[null]
        page1.items.shouldHaveSize(2)
        page1.next.shouldNotBeNull()

        val page2 = applications.list(teamId)[page1.next]
        page2.items.shouldHaveSize(1)
        page2.next.shouldBeNull()

        page1.items.plus(page2.items).shouldContainExactlyInAnyOrder(application1, application2, application3)
    }

    @Test
    fun `get application - found`() {
        applications[application2.teamId, application2.appId] shouldBe application2
    }

    @Test
    fun `get application - not found`() {
        applications[teamId, AppId.random(driver.random)] shouldBe null
    }

    @Test
    fun `delete - success`() {
        applications -= application2

        applications.list(teamId)
            .toList()
            .shouldContainExactlyInAnyOrder(application1, application3)
    }

    @Test
    fun `delete - not found`() {
        applications -= application2
        applications -= application2
    }

    @Test
    fun `save - can update`() {
        val updated = application1.copy(
            environments = application1.environments + staging
        )

        applications += updated

        applications.list(teamId).toList().shouldContainExactlyInAnyOrder(updated, application2, application3)
    }

    @Test
    fun `save and get extensions`() {
        val updated = application1.copy(extensions = mapOf("foo" to "bar"))
        applications += updated

        applications[updated.teamId, updated.appId] shouldBe updated
    }
}