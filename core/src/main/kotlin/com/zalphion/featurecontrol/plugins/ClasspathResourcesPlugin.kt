package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.FeatureControl
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.CachingFilters
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import java.time.Duration

class ClasspathResourcesPlugin(
    val webPath: String,
    val resourcePath: String,
    val cacheTtl: Duration,
): Plugin {
    override fun getRoutes(app: FeatureControl) = routes(listOf(
        webPath bind Method.GET to CachingFilters
            .CacheResponse.MaxAge(cacheTtl)
            .then(static(Classpath(resourcePath)))
    ))
}

fun Plugin.Companion.webjars(
    webPath: String = "/static",
    resourcePath: String = "/META-INF/resources/webjars",
    cacheTtl: Duration = Duration.ofDays(7)
) = ClasspathResourcesPlugin(webPath, resourcePath, cacheTtl).toFactory()