package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.createFeature
import com.zalphion.featurecontrol.featureKey1
import com.zalphion.featurecontrol.featureKey2
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.new
import com.zalphion.featurecontrol.old
import com.zalphion.featurecontrol.web.asUser
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.http4k.playwright.Http4kBrowser
import org.http4k.playwright.LaunchPlaywrightBrowser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class FeaturesUiTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = LaunchPlaywrightBrowser(core.getRoutes())

    private val member = users.create(idp1Email1).shouldBeSuccess()
    private val app = createApplication(member, appName1)

    @Test
    fun `list features - empty`(browser: Http4kBrowser) {
        browser.asUser(core, member.user)
            .applications.select(app.appName)
            .application
            .shouldBeEmpty()
    }

    @Test
    fun `new feature`(browser: Http4kBrowser) {
        browser.asUser(core, member.user)
            .applications.select(app.appName)
            .application.newFeature { form ->
                form.key = featureKey1
                form.edit.description = "cool stuff"
                form.edit.forVariant(0) { row ->
                    row.name = old
                    row.description = "old stuff"
                    row.default = true
                }
                form.edit.newVariant { row ->
                    row.name = new
                    row.description = "new stuff"
                }
            }.submit { page ->
                page.featureKey shouldBe featureKey1
                page.featureEdit.description shouldBe "cool stuff"
                page.featureEdit.variants.map { it.name }.shouldContainExactly(old, new)
            }

        core.features[app.appId, featureKey1] shouldBe Feature(
            teamId = member.team.teamId,
            appId = app.appId,
            key = featureKey1,
            description = "cool stuff",
            variants = mapOf(old to "old stuff", new to "new stuff"),
            defaultVariant = old,
            environments = emptyMap(),
            extensions = emptyMap()
        )
    }

    @Test
    fun `edit feature`(browser: Http4kBrowser) {
        val feature1 = createFeature(member, app, featureKey1)
        val feature2 = createFeature(
            principal = member,
            application = app,
            featureKey = featureKey2,
            variants = mapOf(old to "old"),
            defaultVariant = old
        )

        browser.asUser(core, member.user)
            .applications.select(app.appName)
            .application.select(feature2.key) { page ->
                page.featureEdit.let { feature ->
                    feature.description = "really cool stuff"
                    feature.forVariant(old) { row ->
                        row.description = "legacy"
                    }
                    feature.newVariant { row ->
                        row.name = new
                        row.description = "modern"
                        row.default = true
                    }
                }
            }.update { page ->
                page.application.shouldContainExactly(feature1.key, feature2.key)
                page.featureKey shouldBe featureKey2
                page.featureEdit.description shouldBe "really cool stuff"
                page.featureEdit.variants.map { it.name }.shouldContainExactly(old, new)
            }

        core.features[app.appId, feature2.key] shouldBe feature2.copy(
            description = "really cool stuff",
            variants = mapOf(old to "legacy", new to "modern"),
            defaultVariant = new
        )
    }

    @Test
    fun `remove variant`(browser: Http4kBrowser) {
        val feature = createFeature(
            principal = member,
            application = app,
            featureKey = featureKey1,
            defaultVariant = old,
            variants = mapOf(old to "old", new to "new")
        )

        browser.asUser(core, member.user)
            .applications.select(app.appName)
            .application.select(feature.key) { page ->
                page.featureEdit.forVariant(old).remove()
                // 'new' variant should be selected automatically
            }.update { page ->
                page.featureEdit.variants.map { it.name }.shouldContainExactly(new)
            }

        core.features[app.appId, feature.key] shouldBe feature.copy(
            defaultVariant = new,
            variants = mapOf(new to "new")
        )
    }

    @Test
    fun `delete feature`(browser: Http4kBrowser) {
        val feature1 = createFeature(member, app, featureKey1)
        val feature2 = createFeature(member, app, featureKey2)

        browser.asUser(core, member.user)
            .applications.select(app.appName)
            .application.select(feature2.key)
            .more().delete().confirm { page ->
                page.application.name shouldBe app.appName
                page.applications.shouldContainExactly(feature1.key)
                page.applications.selected.shouldBeNull()
            }
    }
}