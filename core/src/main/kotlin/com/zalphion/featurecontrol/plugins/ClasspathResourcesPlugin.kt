package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.Core
import org.http4k.core.then
import org.http4k.filter.CachingFilters
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.static
import java.time.Duration

// TODO optionally nest the handler (e.g. /static)
class ClasspathResourcesPlugin(
    val path: String,
    val cacheTtl: Duration,
): Plugin {
    override fun getRoutes(core: Core) = CachingFilters
        .CacheResponse.MaxAge(cacheTtl)
        .then(static(Classpath(path)))
}

fun Plugin.Companion.webjars(
    path: String = "/META-INF/resources/webjars",
    cacheTtl: Duration = Duration.ofDays(7)
) = ClasspathResourcesPlugin(path, cacheTtl).toFactory()