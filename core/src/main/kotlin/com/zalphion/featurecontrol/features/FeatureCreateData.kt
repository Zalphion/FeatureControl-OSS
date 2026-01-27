package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.applications.Application
import com.zalphion.featurecontrol.plugins.Extendable

data class FeatureCreateData(
    val featureKey: FeatureKey,
    val variants: Map<Variant, String>,
    val defaultVariant: Variant,
    val environments: Map<EnvironmentName, FeatureEnvironment>,
    val description: String,
    override val extensions: Extensions
): Extendable<FeatureCreateData> {
    override fun with(extensions: Extensions) = copy(extensions = this.extensions + extensions)
}

fun FeatureCreateData.toFeature(application: Application) = Feature(
    teamId = application.teamId,
    appId = application.appId,
    key = featureKey,
    variants = variants,
    defaultVariant = defaultVariant,
    environments = environments,
    description = description,
    extensions = extensions
)