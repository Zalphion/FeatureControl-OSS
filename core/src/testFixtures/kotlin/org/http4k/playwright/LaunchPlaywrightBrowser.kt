package org.http4k.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Playwright.create
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.server.ServerConfig
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class LaunchPlaywrightBrowser @JvmOverloads constructor(
    http: HttpHandler,
    private val browserType: Playwright.() -> BrowserType = Playwright::chromium,
    private val launchOptions: LaunchOptions = LaunchOptions(),
    private val createPlaywright: () -> Playwright = ::create,
    serverFn: (Int) -> ServerConfig = ::SunHttp
) : ParameterResolver, AfterAllCallback {

    override fun supportsParameter(pc: ParameterContext, ec: ExtensionContext) =
        pc.parameter.parameterizedType.typeName == Browser::class.java.name ||
            pc.parameter.parameterizedType.typeName == Http4kBrowser::class.java.name

    private var playwright: Playwright? = null
    private val server by lazy {
        http.asServer(serverFn(0)).start()
    }

    private var browser: Browser? = null

    override fun resolveParameter(pc: ParameterContext, ec: ExtensionContext) = Http4kBrowser(
        delegate = browser ?: run {
            val playwright = createPlaywright().also { playwright = it }
            browserType(playwright).launch(launchOptions).also { browser = it }
        },
        baseUri = Uri.of("http://localhost:${server.port()}")
    )

    override fun afterAll(context: ExtensionContext) {
        server.stop()
        browser?.close() // in theory, closing playwright should be good enough, but this isn't always the case
        playwright?.close()
    }
}
