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
import com.zalphion.featurecontrol.web.playwright
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.http4k.playwright.Http4kBrowser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ApplicationPageUiTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member = users.create(idp1Email1).shouldBeSuccess()

    @Test
    fun `no applications`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page ->
            page.teamId shouldBe member.team.teamId
            page.applications.list.shouldBeEmpty()
            page.applications.selected.shouldBeNull()
        }
    }

    @Test
    fun `create application`(browser: Http4kBrowser) {
        lateinit var createdId: AppId

        browser.asUser(core, member.user) { page ->
            page.applications.new { form ->
                form.setName(appName1)
                form.newEnvironment { env ->
                    env.setName(devName)
                    env.setDescription("dev stuff")
                    env.setColour(Colour.white)
                }
                form.newEnvironment { env ->
                    env.setName(prodName)
                    env.setDescription("prod stuff")
                    env.setColour(Colour.black)
                }
            }.submit { page ->
                page.applications.list.shouldContainExactly(appName1)
                page.applications.selected shouldBe appName1

                page.application.name shouldBe appName1
                createdId = page.uriAppId
            }
        }

        core.applications[member.team.teamId, createdId] shouldBe Application(
            teamId = member.team.teamId,
            appId = createdId,
            appName = appName1,
            extensions = emptyMap(),
            environments = listOf(
                Environment(
                    name = devName,
                    description = "dev stuff",
                    colour = Colour.white,
                    extensions = emptyMap()
                ),
                Environment(
                    name = prodName,
                    description = "prod stuff",
                    colour = Colour.black,
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
            page.applications.list.shouldContainExactlyInAnyOrder(app1.appName, app2.appName)
            page.applications.selected.shouldBeNull()

            page.applications.select(app1.appName) { page ->
                page.applications.list.shouldContainExactlyInAnyOrder(app1.appName, app2.appName)
                page.applications.selected shouldBe app1.appName
                page.uriAppId shouldBe app1.appId
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
                page.applications.list.shouldContainExactly(app1.appName)
                page.applications.selected.shouldBeNull()
            }
    }

    @Test
    fun `edit application`(browser: Http4kBrowser) {
        val app1 = createApplication(member, appName1, listOf(dev, prod))
        val app2 = createApplication(member, appName2, listOf(dev, prod))

        browser.asUser(core, member.user)
            .applications.select(app2.appName)
            .application.more().update { app ->
                app.setName(appName3)

                app.forEnvironment(dev.name) { env ->
                    env.setDescription("cool stuff happens here")
                }

                app.newEnvironment { env ->
                    env.setName(stagingName)
                }
            }.submit { page ->
                page.applications.list.shouldContainExactlyInAnyOrder(app1.appName, appName3)
                page.applications.selected shouldBe appName3
            }

        core.applications[app2.teamId, app2.appId] shouldBe app2.copy(
            appName = appName3,
            environments = listOf(
                dev.copy(description = "cool stuff happens here"),
                prod,
                Environment(
                    name = stagingName,
                    colour = Colour.white,
                    description = "",
                    extensions = emptyMap()
                )
            )
        )
    }
}