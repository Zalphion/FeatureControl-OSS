package org.http4k.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Playwright.create
import com.microsoft.playwright.Request
import com.microsoft.playwright.Route
import org.http4k.core.Headers
import org.http4k.core.HttpHandler
import org.http4k.core.MemoryBody
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ResponseFilters
import org.http4k.lens.location
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.util.function.Consumer

class LaunchPlaywrightContext @JvmOverloads constructor(
    private val http: HttpHandler,
    private val browserType: Playwright.() -> BrowserType = Playwright::chromium,
    private val launchOptions: LaunchOptions = LaunchOptions(),
    private val createPlaywright: () -> Playwright = ::create
) : ParameterResolver, AfterEachCallback {

    private var playwright: Playwright? = null
    private var browser: Browser? = null

    private fun getBrowser(): Browser {
        val pw = playwright ?: createPlaywright().also { playwright = it }
        return browserType(pw).launch(launchOptions).also { browser = it }
    }

    override fun supportsParameter(pc: ParameterContext, ec: ExtensionContext) =
        pc.parameter.parameterizedType.typeName == BrowserContext::class.java.name

    override fun resolveParameter(pc: ParameterContext, ec: ExtensionContext): BrowserContext {
        return getBrowser().newContext().apply {
            route({ true }, http.toPlaywright())
        }
    }

    override fun afterEach(context: ExtensionContext) {
        browser?.close()
        playwright?.close()
    }
}

private fun HttpHandler.toPlaywright(): Consumer<Route> {
    val withRedirects = ResponseFilters.Modify( { response ->
        // playwright's route override doesn't support 3xx redirects, so return a script with the redirect
        if (response.status.redirection) {
            Response(Status.OK).body("<script>window.location.replace('${response.location()}')</script>")
        } else {
            response
        }
    }).then(this)

    return Consumer<Route> { route ->
        val response = withRedirects(route.request().toHttp4k())
        route.fulfill(response.toPlaywright())
    }
}

private fun Request.toHttp4k() = org.http4k.core.Request(
    method = Method.valueOf(method().uppercase()),
    uri = Uri.of(url())
)
    .headers(allHeaders().map { it.key to it.value })
    .body(MemoryBody(postDataBuffer() ?: byteArrayOf()))

private fun Response.toPlaywright() = Route.FulfillOptions()
    .setStatus(status.code)
    .setHeaders(headers.toPlaywright())
    .setBodyBytes(body.stream.readBytes())

// TODO test multiple headers in requests and responses
private fun Headers.toPlaywright() = groupBy({ it.first }, { it.second })
    .mapValues { it.value.filterNotNull().joinToString(",") }