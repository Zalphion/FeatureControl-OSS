package com.zalphion.featurecontrol.features.web

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
import org.http4k.playwright.Http4kBrowser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class FeatureEnvironmentPageTest: CoreTestDriver() {

    @RegisterExtension
    val playwright = playwright()

    private val member = users.create(idp1Email1).shouldBeSuccess()
    private val app = createApplication(
        principal = member,
        appName = appName1,
        environments = listOf(dev, prod)
    )

    private val feature = createFeature(
        principal = member,
        application = app,
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
            application = app,
            featureKey = featureKey2,
            variants = emptyMap()
        )

        browser.asUser(core, member.user)
            .applications.select(app.appName)
            .application.select(emptyFeature.key)
            .featureNav.environments.shouldBeEmpty()
    }
}