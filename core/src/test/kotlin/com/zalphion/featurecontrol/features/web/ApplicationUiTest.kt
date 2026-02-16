package com.zalphion.featurecontrol.features.web

import org.http4k.playwright.Http4kBrowser
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.appName2
import com.zalphion.featurecontrol.appName3
import com.zalphion.featurecontrol.applications.Environment
import com.zalphion.featurecontrol.black
import com.zalphion.featurecontrol.create
import com.zalphion.featurecontrol.createApplication
import com.zalphion.featurecontrol.dev
import com.zalphion.featurecontrol.devName
import com.zalphion.featurecontrol.idp1Email1
import com.zalphion.featurecontrol.invoke
import com.zalphion.featurecontrol.prod
import com.zalphion.featurecontrol.prodName
import com.zalphion.featurecontrol.stagingName
import com.zalphion.featurecontrol.web.asUser
import com.zalphion.featurecontrol.web.playwright
import com.zalphion.featurecontrol.white
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class ApplicationUiTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member = core.users.create(idp1Email1).shouldBeSuccess()

    @Test
    fun `no applications`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page ->
            page.uriTeamId shouldBe member.team.teamId
            page.applications.list.shouldBeEmpty()
            page.applications.selected.shouldBeNull()
        }
    }

    @Test
    fun `create application`(browser: Http4kBrowser) {
        browser.asUser(core, member.user) { page ->
            page.applications.new { form ->
                form.name = appName1
                form.add { env ->
                    env.name = devName
                    env.description = "dev stuff"
                    env.colour = white
                }
                form.add { env ->
                    env.name = prodName
                    env.description = "prod stuff"
                    env.colour = black
                }
            }.submit { page ->
                page.applications.list.shouldContainExactly(appName1)
                page.applications.selected shouldBe appName1

                page.application.name shouldBe appName1

                page.application.more().update { created ->
                    created.name shouldBe appName1
                    created.options.map { it.name }.shouldContainExactlyInAnyOrder(devName, prodName)
                    created[devName] shouldNotBeNull {
                        colour shouldBe white
                        description shouldBe "dev stuff"
                    }
                    created[prodName] shouldNotBeNull {
                        colour shouldBe black
                        description shouldBe "prod stuff"
                    }
                }
            }
        }
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
                page.application.name shouldBe app1.appName
            }
        }
    }

    @Test
    fun `delete application`(browser: Http4kBrowser) {
        val app1 = createApplication(member, appName1)
        val app2 = createApplication(member, appName2)

        browser.asUser(core, member.user) { page ->
            page.applications.select(app2.appName)
                .application.more()
                .delete().confirm { page ->
                    page.applications.list.shouldContainExactly(app1.appName)
                    page.applications.selected.shouldBeNull()
                }
        }
    }

    @Test
    fun `edit application`(browser: Http4kBrowser) {
        val app1 = createApplication(member, appName1, listOf(dev, prod))
        val app2 = createApplication(member, appName2, listOf(dev, prod))

        browser.asUser(core, member.user) { page ->
            page.applications.select(app2.appName)
                .application.more().update { app ->
                    app.name = appName3

                    app[dev.name] shouldNotBeNull {
                        description = "cool stuff happens here"
                    }

                    app.add { env ->
                        env.name = stagingName
                    }
                }.submit { page ->
                    page.applications.list.shouldContainExactlyInAnyOrder(app1.appName, appName3)
                    page.applications.selected shouldBe appName3
                }
        }

        core.applications.get(app2.teamId, app2.appId).invoke(core, member.user) shouldBeSuccess app2.copy(
            appName = appName3,
            environments = listOf(
                dev.copy(description = "cool stuff happens here"),
                prod,
                Environment(
                    name = stagingName,
                    colour = white,
                    description = "",
                    extensions = emptyMap()
                )
            )
        )
    }
}