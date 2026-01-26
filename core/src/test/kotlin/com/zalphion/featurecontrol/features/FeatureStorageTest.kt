package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.featureKey1
import com.zalphion.featurecontrol.featureKey2
import com.zalphion.featurecontrol.featureKey3
import com.zalphion.featurecontrol.new
import com.zalphion.featurecontrol.off
import com.zalphion.featurecontrol.oldNewData
import com.zalphion.featurecontrol.on
import com.zalphion.featurecontrol.onOffData
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.staging
import com.zalphion.featurecontrol.storage.PageSize
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.memory
import com.zalphion.featurecontrol.teams.TeamId
import com.zalphion.featurecontrol.toCreate
import dev.andrewohara.utils.pagination.Page
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FeatureStorageTest: CoreTestDriver(storageDriver = StorageDriver.memory(PageSize.of(2))) {

    private val teamId = TeamId.of("team1234")
    private val features = core.features

    private val app1 = Application(
        teamId = teamId,
        appId = AppId.random(core.random),
        appName = appName1,
        environments = listOf(dev, prod),
        extensions = emptyMap()
    ).also(core.applications::plusAssign)

    private val app2 = Application(
        teamId = teamId,
        appId = AppId.random(core.random),
        appName = appName2,
        environments = listOf(dev, staging, prod),
        extensions = emptyMap()
    ).also(core.applications::plusAssign)

    private val feature1 = oldNewData
        .toCreate(featureKey1)
        .toFeature(app1)
        .also(features::plusAssign)

    private val feature2 = onOffData
        .toCreate(featureKey2)
        .toFeature(app1)
        .also(features::plusAssign)

    private val feature3 = oldNewData
        .toCreate(featureKey3)
        .toFeature(app1)
        .also(features::plusAssign)

    private val feature4 = FeatureCreateData(
        featureKey = featureKey1,
        variants = mapOf(on to "on", off to "off"),
        defaultVariant = off,
        environments = emptyMap(),
        description = "",
        extensions = emptyMap()
    )
        .toFeature(app2)
        .also(features::plusAssign)

    @Test
    fun `list toggles - all`() {
        features.list(app1.appId)
            .toList()
            .shouldContainExactlyInAnyOrder(feature1, feature2, feature3)
    }

    @Test
    fun `list toggles - paged`() {
        features.list(app1.appId)[null] shouldBe Page(
            items = listOf(feature1, feature2),
            next = feature2.key
        )

        features.list(app1.appId)[feature2.key] shouldBe Page(
            items = listOf(feature3),
            next = null
        )
    }

    @Test
    fun `get toggle - found`() {
        features[app1.appId, featureKey1] shouldBe feature1
    }

    @Test
    fun `get toggle - empty environments`() {
        features[app2.appId, featureKey1] shouldBe feature4
    }

    @Test
    fun `get toggle - not found`() {
        features[app2.appId, featureKey2].shouldBeNull()
    }

    @Test
    fun `delete toggle - found`() {
        features -= feature1

        features.list(app1.appId)
            .toList()
            .shouldContainExactlyInAnyOrder(feature2, feature3)
    }

    @Test
    fun `delete toggle - not found`() {
        features -= feature2
        features -= feature2
    }

    @Test
    fun `save - can update`() {
        val updated = feature1.copy(
            variants = mapOf(new to "new", Variant.parse("legacy") to "legacy"),
            defaultVariant = new,
        )

        features += updated
        features[feature1.appId, feature1.key] shouldBe updated
    }
}