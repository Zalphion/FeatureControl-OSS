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
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

// TODO contribute back to http4k
class LaunchPlaywrightBrowser @JvmOverloads constructor(
    http: HttpHandler,
    private val browserType: Playwright.() -> BrowserType = Playwright::chromium,
    private val launchOptions: LaunchOptions = LaunchOptions(),
    private val createPlaywright: () -> Playwright = ::create,
    serverFn: (Int) -> ServerConfig = ::SunHttp
) : ParameterResolver, AfterEachCallback {

    override fun supportsParameter(pc: ParameterContext, ec: ExtensionContext) =
        pc.parameter.parameterizedType.typeName == Browser::class.java.name ||
            pc.parameter.parameterizedType.typeName == Http4kBrowser::class.java.name

    // lazy, so we don't start the server unless we need it
    private val server by lazy {
        http.asServer(serverFn(0)).start()
    }

    override fun resolveParameter(pc: ParameterContext, ec: ExtensionContext): Http4kBrowser {
        val store = ec.getStore(ExtensionContext.Namespace.create(LaunchPlaywrightBrowser::class.java, ec.requiredTestClass))

        // share a single browser across all tests using this extension
        val browser = store.computeIfAbsent("browser") {
            val playwright = createPlaywright()
            val browser = browserType(playwright).launch(launchOptions)

            // ensure playwright is closed when no longer needed
            store.put("cleanup", ExtensionContext.Store.CloseableResource {
                browser.close()
                playwright.close()
            })
            browser
        } as Browser

        return Http4kBrowser(browser, Uri.of("http://localhost:${server.port()}"))
    }

    override fun afterEach(context: ExtensionContext) {
        server.stop()
    }
}
