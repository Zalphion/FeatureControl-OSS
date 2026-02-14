package org.http4k.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.Playwright.create
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

class LaunchPlaywrightContext @JvmOverloads constructor(
    http: HttpHandler,
    private val browserType: Playwright.() -> BrowserType = Playwright::chromium,
    private val launchOptions: LaunchOptions = LaunchOptions(),
    private val createPlaywright: () -> Playwright = ::create
) : ParameterResolver, AfterEachCallback {

    private val server by lazy {
        http.asServer(SunHttp(0)).start()
    }

    private var playwright: Playwright? = null
    private var browser: Browser? = null

    private fun getBrowser(): Browser {
        val pw = playwright ?: createPlaywright().also { playwright = it }
        return browserType(pw).launch(launchOptions).also { browser = it }
    }

    override fun supportsParameter(pc: ParameterContext, ec: ExtensionContext) =
        pc.parameter.parameterizedType.typeName == BrowserContext::class.java.name

    override fun resolveParameter(pc: ParameterContext, ec: ExtensionContext): BrowserContext {
        val context = getBrowser().newContext()
        val baseUri = Uri.of("http://localhost:${server.port()}")

        return object: BrowserContext by context {
            override fun newPage() = context.newPage().also { it.navigate(baseUri.toString()) }
        }
    }

    override fun afterEach(context: ExtensionContext) {
        server.stop()
        browser?.close()
        playwright?.close()
    }
}