package com.zalphion.featurecontrol.configs.web

import org.http4k.playwright.Http4kBrowser
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.booleanProperty
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
import com.zalphion.featurecontrol.updateConfigEnvironment
import com.zalphion.featurecontrol.updateConfigSpec
import com.zalphion.featurecontrol.web.asUser
import com.zalphion.featurecontrol.web.playwright
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class ConfigEnvironmentUiTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member = core.users.create(idp1Email1).shouldBeSuccess()
    private val app1 = createApplication(
        principal = member,
        appName = appName1,
        environments = listOf(dev, prod)
    )

    init {
        updateConfigSpec(
            principal = member,
            application = app1,
            properties =  mapOf(strProperty, numberProperty, booleanProperty, secretProperty)
        )

        updateConfigEnvironment(
            principal = member,
            application = app1,
            environmentName = dev.name,
            values = mapOf(
                strProperty.first to "lol",
                numberProperty.first to "123"
            )
        )
    }

    @Test
    fun `show environment`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page -> page
            .applications.select(app1.appName)
            .environments.select(devName) { page ->
                page.environments.options.shouldContainExactly(devName, prodName)
                page.environments.selected shouldBe devName
                page.values.shouldContainExactly(
                    booleanProperty.first to "",
                    numberProperty.first to "123",
                    secretProperty.first to "",
                    strProperty.first to "lol"
                )
            }
        }
    }

    @Test
    fun `update environment`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page -> page
            .applications.select(app1.appName)
            .environments.select(devName)
            .update { form ->
                form.values.find { it.key == booleanProperty.first }.shouldNotBeNull().booleanValue = true
                form.values.find { it.key == strProperty.first }.shouldNotBeNull().textValue = null
                form.values.find { it.key == numberProperty.first }.shouldNotBeNull().numberValue = null
                form.values.first { it.key == secretProperty.first }.textValue = "new secret"
            }.submit().values.shouldContainExactly(
                booleanProperty.first to "TRUE",
                numberProperty.first to "",
                secretProperty.first to "********",
                strProperty.first to ""
            )
        }
    }
}