package com.zalphion.featurecontrol.features.web

import org.http4k.playwright.Http4kBrowser
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.createFeature
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.devName
import com.zalphion.featurecontrol.featureKey1
import com.zalphion.featurecontrol.featureKey2
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.Weight
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.new
import com.zalphion.featurecontrol.old
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.prodName
import com.zalphion.featurecontrol.subject1
import com.zalphion.featurecontrol.subject2
import com.zalphion.featurecontrol.web.asUser
import com.zalphion.featurecontrol.web.playwright
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class FeatureEnvironmentUiTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member = core.users.create(idp1Email1).shouldBeSuccess()
    private val app1 = createApplication(
        principal = member,
        appName = appName1,
        environments = listOf(dev, prod)
    )

    private val feature = createFeature(
        principal = member,
        application = app1,
        featureKey = featureKey1,
        variants = mapOf(old to "old", new to "new"),
        environments = mapOf(
            devName to FeatureEnvironment(
                weights = mapOf(new to Weight.of(1)),
                overrides = emptyMap(),
                extensions = emptyMap()
            ),
            prodName to FeatureEnvironment(
                weights = mapOf(
                    old to Weight.of(1),
                    new to Weight.of(2)
                ),
                overrides = mapOf(
                    subject1 to old,
                    subject2 to new
                ),
                extensions = emptyMap()
            )
        )
    )

    @Test
    fun `no variants`(browser: Http4kBrowser) {
        val emptyFeature = createFeature(
            principal = member,
            application = app1,
            featureKey = featureKey2,
            variants = emptyMap()
        )

        browser.asUser(core, member.user) { page -> page
            .applications.select(app1.appName)
            .application.select(emptyFeature.key)
            .edit.variants.shouldHaveSize(1).first()
            .name.shouldBeNull()
        }
    }

    @Test
    fun `show environment`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page ->
            page.applications.select(app1.appName)
            .application.select(feature.key)
            .environments.select(devName) { page ->
                page.environments.selected shouldBe devName
                page.variants.map { it.name }.shouldContainExactly(old, new)

                page.variants.find { it.name == old }.shouldNotBeNull().also { variant ->
                    variant.name shouldBe old
                    variant.weight shouldBe null
                    variant.subjectIds {
                        it.subjectIds.shouldBeEmpty()
                    }
                }

                page.variants.find { it.name == new }.shouldNotBeNull().also { variant ->
                    variant.name shouldBe new
                    variant.weight shouldBe Weight.of(1)
                    variant.subjectIds {
                        it.subjectIds.shouldBeEmpty()
                    }
                }
            }.environments.select(prodName) { page ->
                page.environments.selected shouldBe prodName
                page.variants.map { it.name }.shouldContainExactly(old, new)

                page.variants.find { it.name == old }.shouldNotBeNull().also { variant ->
                    variant.name shouldBe old
                    variant.weight shouldBe Weight.of(1)
                    variant.subjectIds {
                        it.subjectIds.shouldContainExactlyInAnyOrder(subject1)
                    }
                }

                page.variants.find { it.name == new }.shouldNotBeNull().also { variant ->
                    variant.name shouldBe new
                    variant.weight shouldBe Weight.of(2)
                    variant.subjectIds {
                        it.subjectIds.shouldContainExactlyInAnyOrder(subject2)
                    }
                }
            }
            }
    }

    @Test
    fun `update environment`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page ->
            page.applications.select(app1.appName)
                .application.select(feature.key)
                .environments.select(devName) { page ->
                    page.variants.find { it.name == old }.shouldNotBeNull().also { variant ->
                        variant.name shouldBe old
                        variant.weight = Weight.of(9001)
                        variant.subjectIds { modal ->
                            modal.subjectIds = listOf(subject1, subject2)
                        }
                    }
                }.update { result ->
                    result.environments.selected shouldBe devName
                    result.variants.find { it.name == old }.shouldNotBeNull().also { variant ->
                        variant.name shouldBe old
                        variant.weight shouldBe Weight.of(9001)
                        variant.subjectIds { modal ->
                            modal.subjectIds.shouldContainExactlyInAnyOrder(subject1, subject2)
                        }
                    }
                }
        }
    }

    @Test
    fun `reset environment`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page ->
            page.applications.select(app1.appName)
                .application.select(feature.key)
                .environments.select(prodName) { page ->
                    page.variants.find { it.name == old }.shouldNotBeNull().also { variant ->
                        variant.weight = Weight.of(9001)
                        variant.subjectIds { modal ->
                            modal.subjectIds = listOf(subject1, subject2)
                        }
                    }
                }.reset { result ->
                    result.environments.selected shouldBe prodName
                    result.variants.find { it.name == old }.shouldNotBeNull().also { variant ->
                        variant.weight shouldBe Weight.of(1)
                        variant.subjectIds { modal ->
                            modal.subjectIds.shouldContainExactlyInAnyOrder(subject1)
                        }
                    }
                }
        }
    }
}