package com.zalphion.featurecontrol.applications

import com.zalphion.featurecontrol.plugins.Extensions

data class ApplicationUpdateData(
    val appName: AppName?,
    val environments: List<Environment>?,
    val extensions: Extensions?
)

fun Application.update(data: ApplicationUpdateData) = Application(
    teamId = teamId,
    appId = appId,
    appName = data.appName ?: appName,
    environments = data.environments ?: environments,
    extensions = data.extensions ?: extensions
)