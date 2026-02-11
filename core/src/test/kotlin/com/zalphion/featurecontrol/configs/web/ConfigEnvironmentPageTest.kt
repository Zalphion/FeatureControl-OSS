package com.zalphion.featurecontrol.configs.web

import com.microsoft.playwright.BrowserContext
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.booleanProperty
import com.zalphion.featurecontrol.configs.ConfigEnvironment
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.devName
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.numberProperty
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.prodName
import com.zalphion.featurecontrol.secretProperty
import com.zalphion.featurecontrol.strProperty
import com.zalphion.featurecontrol.web.asUser
import com.zalphion.featurecontrol.web.playwright
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class ConfigEnvironmentPageTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member = users.create(idp1Email1).shouldBeSuccess()
    private val app = createApplication(
        principal = member,
        appName = appName1,
        environments = listOf(dev, prod)
    )

    init {
        core.configs += ConfigSpec(
            teamId = app.teamId,
            appId = app.appId,
            properties = mapOf(
                strProperty,
                numberProperty,
                booleanProperty,
                secretProperty
            )
        )

        core.configs += ConfigEnvironment(
            teamId = app.teamId,
            appId = app.appId,
            name = dev.name,
            values = mapOf(
                strProperty.first to "lol",
                numberProperty.first to "123"
            )
        )
    }

    @Test
    fun `show environment`(context: BrowserContext) {
        context.asUser(core, member.user) { page ->
            page.applications.select(app.appName)
                .environments.select(devName) { page ->
                    page.environments.options.shouldContainExactly(devName, prodName)
                    page.environments.selected shouldBe devName

                    page.values.shouldHaveSize(4)
                    page.values[0].also { value ->
                        value.key shouldBe booleanProperty.first
                        value.booleanValue shouldBe null
                    }
                    page.values[1].also { value ->
                        value.key shouldBe numberProperty.first
                        value.numberValue shouldBe 123.toBigDecimal()
                    }
                    page.values[2].also { value ->
                        value.key shouldBe secretProperty.first
                        value.textValue shouldBe null
                    }
                    page.values[3].also { value ->
                        value.key shouldBe strProperty.first
                        value.textValue shouldBe "lol"
                    }
                }
        }
    }

    @Test
    fun `update environment`(context: BrowserContext) {
        context.asUser(core, member.user) { page ->
            page.applications.select(app.appName)
                .environments.select(devName) { page ->
                    page.values.find { it.key == booleanProperty.first }.shouldNotBeNull().booleanValue = true
                    page.values.find { it.key == strProperty.first }.shouldNotBeNull().textValue = null
                    page.values.find { it.key == numberProperty.first }.shouldNotBeNull().numberValue = null
                    page.values.first { it.key == secretProperty.first }.textValue = "new secret"
                }.update { result ->
                    result.values.shouldHaveSize(4)

                    result.values.find { it.key == booleanProperty.first }.shouldNotBeNull().booleanValue shouldBe true
                    result.values.find { it.key == strProperty.first }.shouldNotBeNull().textValue shouldBe null
                    result.values.find { it.key == numberProperty.first }.shouldNotBeNull().numberValue shouldBe null
                    result.values.find { it.key == secretProperty.first }
                        .shouldNotBeNull().textValue shouldBe "********"
                }
        }
    }

    @Test
    fun `reset environment`(context: BrowserContext) {
        context.asUser(core, member.user) { page ->
            page.applications.select(app.appName)
                .environments.select(devName) { page ->
                    page.values.find { it.key == strProperty.first }.shouldNotBeNull().textValue = "foobar"
                }.reset { result ->
                    result.values.find { it.key == strProperty.first }.shouldNotBeNull().textValue shouldBe "lol"
                }
        }
    }
}