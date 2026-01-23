package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.lib.Update
import com.zalphion.featurecontrol.lib.effective
import com.zalphion.featurecontrol.plugins.Extensions

data class FeatureUpdateData(
    val variants: Update<Map<Variant, String>>?,
    val defaultVariant: Update<Variant>?,
    val environmentsToUpdate: Update<Map<EnvironmentName, FeatureEnvironment>>?,
    val description: Update<String>?,
    val extensions: Update<Extensions>?
) {
    companion object {
        val empty = FeatureUpdateData(null, null, null, null, null)
    }
}

fun Feature.update(data: FeatureUpdateData) = Feature(
    teamId = teamId,
    appId = appId,
    key = key,
    variants = data.variants.effective(variants),
    defaultVariant = data.defaultVariant.effective(defaultVariant),
    environments = if (data.environmentsToUpdate != null) {
        environments.plus(data.environmentsToUpdate.value)
    } else environments,
    description = data.description.effective(description),
    extensions = data.extensions.effective(extensions)
)