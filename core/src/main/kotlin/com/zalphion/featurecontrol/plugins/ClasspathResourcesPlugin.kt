package com.zalphion.featurecontrol.plugins

import com.zalphion.featurecontrol.Core
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.static

// TODO optionally nest the handler (e.g. /webjars)
class ClasspathResourcesPlugin(val path: String): Plugin {
    override fun getRoutes() = static(Classpath(path))
}

fun Plugin.Companion.webjars(path: String = "/META-INF/resources/webjars") = object: PluginFactory<ClasspathResourcesPlugin>() {
    override fun createInternal(core: Core) = ClasspathResourcesPlugin(path)
}