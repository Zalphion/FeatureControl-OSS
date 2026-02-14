package com.zalphion.featurecontrol.configs.web

import com.microsoft.playwright.BrowserContext
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.appName1
import com.zalphion.featurecontrol.booleanProperty
import com.zalphion.featurecontrol.configs.ConfigSpec
import com.zalphion.featurecontrol.configs.PropertyKey
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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@Tag("playwright")
class ConfigSpecUiTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member = users.create(idp1Email1).shouldBeSuccess()
    private val app = createApplication(
        principal = member,
        appName = appName1,
        environments = listOf(dev, prod)
    )

    @Test
    fun `no properties`(context: BrowserContext) {
        context.asUser(core, member.user) { page ->
            page.applications.select(app.appName) { page ->
                page.environments.options.shouldContainExactly(devName, prodName)
                page.environments.selected.shouldBeNull()
                page.properties.shouldBeEmpty()
            }
        }
    }

    @Test
    fun `add properties`(context: BrowserContext) {
        context.asUser(core, member.user) { page ->
            page.applications.select(app.appName) { page ->
                page.newProperty { prop ->
                    prop.key = strProperty.first
                    prop.type = PropertyTypeDto.String
                    prop.description = "a string prop"
                }
                page.newProperty { prop ->
                    prop.key = secretProperty.first
                    prop.type = PropertyTypeDto.Secret
                }
                page.newProperty { prop ->
                    prop.key = numberProperty.first
                    prop.type = PropertyTypeDto.Number
                }
                page.newProperty { prop ->
                    prop.key = booleanProperty.first
                    prop.type = PropertyTypeDto.Boolean
                }

                page.update { result ->
                    result.properties.map { it.toDto() }.shouldContainExactlyInAnyOrder(
                        ConfigPropertyDto(strProperty.first, "a string prop", PropertyTypeDto.String),
                        ConfigPropertyDto(secretProperty.first, "", PropertyTypeDto.Secret),
                        ConfigPropertyDto(numberProperty.first, "", PropertyTypeDto.Number),
                        ConfigPropertyDto(booleanProperty.first, "", PropertyTypeDto.Boolean)
                    )
                }
            }
        }
    }

    @Test
    fun `edit properties`(context: BrowserContext) {
        core.configs += ConfigSpec(
            teamId = app.teamId,
            appId = app.appId,
            properties = mapOf(strProperty, secretProperty, numberProperty, booleanProperty)
        )

        context.asUser(core, member.user) { page ->
            page.applications.select(app.appName) { page ->
                page.properties
                    .find { it.key == secretProperty.first }
                    .shouldNotBeNull()
                    .remove()

                page.properties
                    .find { it.key == numberProperty.first }
                    .shouldNotBeNull()
                    .also { prop -> prop.description = "lolcats" }

                page.properties
                    .find { it.key == booleanProperty.first }
                    .shouldNotBeNull()
                    .also { prop -> prop.description = "" }

                page.update { result ->
                    result.properties.map { it.toDto() }.shouldContainExactlyInAnyOrder(
                        strProperty.second.toDto(strProperty.first),
                        numberProperty.second.toDto(numberProperty.first).copy(description = "lolcats"),
                        booleanProperty.second.toDto(booleanProperty.first).copy(description = "")
                    )
                }
            }
        }
    }

    @Test
    fun `reset properties`(context: BrowserContext) {
        core.configs += ConfigSpec(
            teamId = app.teamId,
            appId = app.appId,
            properties = mapOf(strProperty, secretProperty, numberProperty, booleanProperty)
        )

        context.asUser(core, member.user) { page ->
            page.applications.select(app.appName) { page ->
                page.properties
                    .find { it.key == secretProperty.first }
                    .shouldNotBeNull()
                    .type = PropertyTypeDto.String

                page.properties
                    .find { it.key == numberProperty.first }
                    .shouldNotBeNull()
                    .description = "foobar"

                page.properties
                    .find { it.key == booleanProperty.first }
                    .shouldNotBeNull()
                    .remove()

                page.newProperty { prop ->
                    prop.key = PropertyKey.parse("new-prop")
                }
            }.reset { result ->
                result.properties.map { it.toDto() }.shouldContainExactlyInAnyOrder(
                    strProperty.second.toDto(strProperty.first),
                    numberProperty.second.toDto(numberProperty.first),
                    secretProperty.second.toDto(secretProperty.first),
                    booleanProperty.second.toDto(booleanProperty.first)
                )
            }
        }
    }
}