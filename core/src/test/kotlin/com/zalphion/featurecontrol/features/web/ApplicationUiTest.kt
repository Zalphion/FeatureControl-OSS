package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.appName3
import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.devName
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.lib.Colour
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.prodName
import com.zalphion.featurecontrol.stagingName
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

abstract class ApplicationUiTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = LaunchPlaywrightBrowser(core.getRoutes())

    private val member = users.create(idp1Email1).shouldBeSuccess()

    @Test
    fun `no applications`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page ->
            page.teamId shouldBe member.team.teamId
            page.applications.shouldBeEmpty()
            page.applications.selected.shouldBeNull()
        }
    }

    @Test
    fun `create application`(browser: Http4kBrowser) {
        lateinit var createdId: AppId

        browser.asUser(core, member.user) { page ->
            page.applications.new { form ->
                form.setName(appName1)
                form.newEnvironment { row ->
                    row.setName(devName)
                    row.setDescription("dev stuff")
                    row.setColour(Colour.Companion.white)
                }
                form.newEnvironment { row ->
                    row.setName(prodName)
                    row.setDescription("prod stuff")
                    row.setColour(Colour.Companion.black)
                }
            }.submit { page ->
                page.applications.shouldContainExactly(appName1)
                page.applications.selected shouldBe appName1

                page.application.name shouldBe appName1
                createdId = page.appId
            }
        }

        core.apps[member.team.teamId, createdId] shouldBe Application(
            teamId = member.team.teamId,
            appId = createdId,
            appName = appName1,
            extensions = emptyMap(),
            environments = listOf(
                Environment(
                    name = devName,
                    description = "dev stuff",
                    colour = Colour.Companion.white,
                    extensions = emptyMap()
                ),
                Environment(
                    name = prodName,
                    description = "prod stuff",
                    colour = Colour.Companion.black,
                    extensions = emptyMap()
                )
            )
        )
    }

    @Test
    fun `select application`(browser: Http4kBrowser) {
        val app1 = createApplication(member, appName1)
        val app2 = createApplication(member, appName2)

        browser.asUser(core, member.user) { page ->
            page.applications.shouldContainExactly(app1.appName, app2.appName)
            page.applications.selected.shouldBeNull()

            page.applications.select(app1.appName) { page ->
                page.applications.shouldContainExactly(app1.appName, app2.appName)
                page.applications.selected shouldBe app1.appName
                page.appId shouldBe app1.appId
                page.application.name shouldBe app1.appName
            }
        }
    }

    @Test
    fun `delete application`(browser: Http4kBrowser) {
        val app1 = createApplication(member, appName1)
        val app2 = createApplication(member, appName2)

        browser.asUser(core, member.user)
            .applications.select(app2.appName)
            .application.more()
            .delete().confirm { page ->
                page.applications.shouldContainExactly(app1.appName)
                page.applications.selected.shouldBeNull()
            }
    }

    @Test
    fun `edit application`(browser: Http4kBrowser) {
        val app1 = createApplication(member, appName1, listOf(dev, prod))
        val app2 = createApplication(member, appName2, listOf(dev, prod))

        browser.asUser(core, member.user)
            .applications.select(app2.appName)
            .application.more().update { form ->
                form.setName(appName3)

                form.forEnvironment(dev.name) { row ->
                    row.setDescription("cool stuff happens here")
                }

                form.newEnvironment { row ->
                    row.setName(stagingName)
                }
            }.submit { page ->
                page.applications.shouldContainExactly(app1.appName, appName3)
                page.applications.selected shouldBe appName3
            }

        core.apps[app2.teamId, app2.appId] shouldBe app2.copy(
            appName = appName3,
            environments = listOf(
                dev.copy(description = "cool stuff happens here"),
                prod,
                Environment(
                    name = stagingName,
                    colour = Colour.Companion.white,
                    description = "",
                    extensions = emptyMap()
                )
            )
        )
    }
}