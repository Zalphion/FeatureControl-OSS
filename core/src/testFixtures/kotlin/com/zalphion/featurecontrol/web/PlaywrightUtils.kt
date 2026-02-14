package com.zalphion.featurecontrol.web

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.Cookie
import com.microsoft.playwright.options.LoadState
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.applications.web.ApplicationsUi
import com.zalphion.featurecontrol.auth.web.createSessionCookie
import com.zalphion.featurecontrol.users.User
import io.kotest.matchers.nulls.shouldNotBeNull
import org.http4k.playwright.LaunchPlaywrightContext
import java.time.Duration
import java.time.Instant

private val ci = System.getenv("CI")?.toBoolean() == true

fun BrowserContext.asUser(
    core: Core,
    user: User,
    waitTimeout: Duration = Duration.ofSeconds(5),
    block: (ApplicationsUi) -> Unit = {}
) {
    val sessionCookie = core.createSessionCookie(user.userId)

    setDefaultTimeout(waitTimeout.toMillis().toDouble())
    addCookies(
        listOf(
            Cookie(sessionCookie.name, sessionCookie.value)
                .setDomain("localhost")
                .setPath("/")
        )
    )

    newPage().use {
        block(ApplicationsUi(it))
    }
}

fun Page.getElement(role: AriaRole, name: String): Locator {
    return getByRole(role, Page.GetByRoleOptions().setName(name))
}

fun Locator.getElement(role: AriaRole, name: String): Locator {
    return getByRole(role, Locator.GetByRoleOptions().setName(name))
}

@Deprecated("use getControlled instead")
fun Page.getModal(name: String): Locator {
    return getByRole(AriaRole.DIALOG).filter(
        Locator.FilterOptions().setHas(getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(name)))
    )
}

fun CoreTestDriver.playwright() = LaunchPlaywrightContext(
    http = core.getRoutes(),
    launchOptions = BrowserType.LaunchOptions().setHeadless(ci)
)

/**
 * all() returns without waiting, which can introduce a race condition
 */
fun Locator.waitForAll(): List<Locator> {
    page().waitForReady()
    return all()
}

fun Page.waitForNextAlpineTick() {
    evaluate("() => new Promise(resolve => window.Alpine.nextTick(resolve))")
}

fun Page.waitForReady() = apply {
    waitForLoadState(LoadState.NETWORKIDLE)
    waitForSelector("body:not([x-cloak])") // Wait for Alpine to finish any initial DOM mutations.
}

fun Locator.getControlled(): Locator {
    val targetId = getAttribute("aria-controls").shouldNotBeNull()
    return page().locator("#$targetId")
}

fun Locator.getTime(): Instant = Instant.parse(getAttribute("datetime").shouldNotBeNull())

fun Locator.assertEnabled(enabled: Boolean) {
    PlaywrightAssertions.assertThat(this).apply {
        if (enabled) isEnabled() else isDisabled()
    }
}