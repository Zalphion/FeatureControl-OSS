package com.zalphion.featurecontrol.features.web

import com.microsoft.playwright.BrowserContext
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.createFeature
import com.zalphion.featurecontrol.featureKey1
import com.zalphion.featurecontrol.featureKey2
import com.zalphion.featurecontrol.features.Feature
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.new
import com.zalphion.featurecontrol.old
import com.zalphion.featurecontrol.web.asUser
import com.zalphion.featurecontrol.web.playwright
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class FeatureUiTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member = core.users.create(idp1Email1).shouldBeSuccess()
    private val app1 = createApplication(member, appName1)

    @Test
    fun `list features - empty`(context: BrowserContext) {
        context.asUser(app, member.user) { page ->
            page.applications.select(app1.appName)
            .application.features
            .shouldBeEmpty()
        }
    }

    @Test
    fun `new feature`(context: BrowserContext) {
        context.asUser(app, member.user) { page ->
            page.applications.select(app1.appName)
            .application.newFeature { form ->
                form.key = featureKey1
                form.edit.description = "cool stuff"
                form.edit.variants.first().also { variant ->
                    variant.name = old
                    variant.description = "old stuff"
                    variant.default = true
                }
                form.edit.newVariant { variant ->
                    variant.name = new
                    variant.description = "new stuff"
                }
            }.submit { result ->
                result.application.selectedFeature shouldBe featureKey1
                result.featureKey shouldBe featureKey1
                result.edit.description shouldBe "cool stuff"
                result.edit.variants.map { it.name }.shouldContainExactly(old, new)
            }
        }

        core.features.get(app1.teamId, app1.appId, featureKey1).invoke(member.user, app) shouldBeSuccess Feature(
            teamId = member.team.teamId,
            appId = app1.appId,
            key = featureKey1,
            description = "cool stuff",
            variants = mapOf(old to "old stuff", new to "new stuff"),
            defaultVariant = old,
            environments = emptyMap(),
            extensions = emptyMap()
        )
    }

    @Test
    fun `edit feature`(context: BrowserContext) {
        val feature1 = createFeature(member, app1, featureKey1)
        val feature2 = createFeature(
            principal = member,
            application = app1,
            featureKey = featureKey2,
            variants = mapOf(old to "old"),
            defaultVariant = old
        )

        context.asUser(app, member.user) { page ->
            page.applications.select(app1.appName)
                .application.select(feature2.key) { page ->
                    page.edit.let { feature ->
                        feature.description = "really cool stuff"
                        feature.variants
                            .find { it.name == old }
                            .shouldNotBeNull()
                            .also { it.description = "legacy" }

                        feature.newVariant { variant ->
                            variant.name = new
                            variant.description = "modern"
                            variant.default = true
                        }
                    }
                }.update { page ->
                    page.application.features.shouldContainExactly(feature1.key, feature2.key)
                    page.featureKey shouldBe featureKey2
                    page.edit.description shouldBe "really cool stuff"
                    page.edit.variants.map { it.name }.shouldContainExactly(old, new)
                }
        }

        core.features.get(app1.teamId, app1.appId, feature2.key).invoke(member.user, app) shouldBeSuccess feature2.copy(
            description = "really cool stuff",
            variants = mapOf(old to "legacy", new to "modern"),
            defaultVariant = new
        )
    }

    @Test
    fun `remove variant`(context: BrowserContext) {
        val feature = createFeature(
            principal = member,
            application = app1,
            featureKey = featureKey1,
            defaultVariant = old,
            variants = mapOf(old to "old", new to "new")
        )

        context.asUser(app, member.user) { page ->
            page.applications.select(app1.appName)
            .application.select(feature.key) { page ->
                page.edit.variants
                    .find { it.name == old }
                    .shouldNotBeNull()
                    .remove() // 'new' variant should be selected automatically
            }.update { page ->
                page.edit.variants.map { it.name }.shouldContainExactly(new)
            }
        }

        core.features.get(app1.teamId, app1.appId, feature.key).invoke(member.user, app) shouldBeSuccess feature.copy(
            defaultVariant = new,
            variants = mapOf(new to "new")
        )
    }

    @Test
    fun `delete feature`(context: BrowserContext) {
        val feature1 = createFeature(member, app1, featureKey1)
        val feature2 = createFeature(member, app1, featureKey2)

        context.asUser(app, member.user) { page ->
            page.applications.select(app1.appName)
                .application.select(feature2.key)
                .environments.more().delete().confirm { page ->
                    page.application.name shouldBe app1.appName
                    page.application.selectedFeature.shouldBeNull()
                    page.application.features.shouldContainExactly(feature1.key)
                }
        }
    }

    @Test
    fun `reset feature`(context: BrowserContext) {
        val feature = createFeature(
            principal = member,
            application = app1,
            featureKey = featureKey1,
            description = "lolcats",
            variants = mapOf(old to "old", new to "new")
        )

        context.asUser(app, member.user) { page ->
            page.applications.select(app1.appName)
                .application.select(feature.key) { page ->
                    page.edit.description = "foobar"
                    page.edit.newVariant { variant ->
                        variant.name = Variant.parse("new-stuff")
                    }
                    page.edit.variants
                        .find { it.name == old }
                        .shouldNotBeNull()
                        .description = "old stuff"
                }.reset { result ->
                    result.edit.description shouldBe "lolcats"
                    result.edit.variants
                        .map { it.name to it.description }
                        .shouldContainExactly(old to "old", new to "new")
                }
        }
    }
}