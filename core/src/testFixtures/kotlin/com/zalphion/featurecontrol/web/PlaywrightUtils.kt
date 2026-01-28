package com.zalphion.featurecontrol.web

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.Cookie
import com.microsoft.playwright.options.LoadState
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.CoreTestDriver
import com.zalphion.featurecontrol.applications.web.ApplicationsPageUi
import com.zalphion.featurecontrol.auth.web.createSessionCookie
import com.zalphion.featurecontrol.users.User
import org.http4k.playwright.Http4kBrowser
import org.http4k.playwright.LaunchPlaywrightBrowser
import java.util.concurrent.TimeUnit

private val ci = System.getenv("CI")?.toBoolean() == true

fun Http4kBrowser.asUser(core: Core, user: User, block: (ApplicationsPageUi) -> Unit = {}): ApplicationsPageUi {
    val sessionCookie = core.createSessionCookie(user.userId)

    val context = newContext().apply {
        setDefaultTimeout(TimeUnit.SECONDS.toMillis(5).toDouble())
        addCookies(
            listOf(
                Cookie(SESSION_COOKIE_NAME, sessionCookie.value)
                    .setDomain("localhost")
                    .setPath("/")
            )
        )
    }

    return try {
        context.newPage()
            .apply { navigate(baseUri.toString()) }
            .let(::ApplicationsPageUi)
            .also(block)
    } catch (e: Exception) {
        e.printStackTrace(System.err)
        if (!ci) Thread.sleep(10_000)
        throw e
    }
}

fun Page.getElement(role: AriaRole, name: String): Locator {
    return getByRole(role, Page.GetByRoleOptions().setName(name))
}

fun Locator.getElement(role: AriaRole, name: String): Locator {
    return getByRole(role, Locator.GetByRoleOptions().setName(name))
}

fun Page.getModal(name: String): Locator {
    return getByRole(AriaRole.DIALOG).filter(
        Locator.FilterOptions().setHas(getByRole(AriaRole.HEADING, Page.GetByRoleOptions().setName(name)))
    )
}

fun CoreTestDriver.playwright() = LaunchPlaywrightBrowser(
    http = core.mergeRoutes(),
    launchOptions = BrowserType.LaunchOptions().setHeadless(ci)
)

/**
 * all() returns without waiting, which can introduce a race condition
 */
fun Locator.waitForAll(): List<Locator> {
    page().waitForLoadState(LoadState.NETWORKIDLE)
    page().waitForSelector("body:not([x-cloak])") // Wait for Alpine to finish any initial DOM mutations.
    return all()
}