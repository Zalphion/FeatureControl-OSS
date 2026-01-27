package com.zalphion.featurecontrol.features

import com.zalphion.featurecontrol.applications.AppId
import com.zalphion.featurecontrol.keyValidation
import com.zalphion.featurecontrol.plugins.Extendable
import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.teams.TeamId
import dev.forkhandles.values.ComparableValue
import dev.forkhandles.values.NonEmptyStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory

data class Feature(
    val teamId: TeamId,
    val appId: AppId,
    val key: FeatureKey,
    val variants: Map<Variant, String>,
    val environments: Map<EnvironmentName, FeatureEnvironment>,
    val defaultVariant: Variant,
    val description: String,
    override val extensions: Extensions
): Extendable<Feature> {
    operator fun get(environment: EnvironmentName) = environments[environment]
        ?: FeatureEnvironment(emptyMap(), emptyMap(), emptyMap())

    override fun with(extensions: Extensions) = copy(extensions = this.extensions + extensions)
}

class FeatureKey private constructor(value: String): StringValue(value), ComparableValue<FeatureKey, String> {
    companion object: StringValueFactory<FeatureKey>(::FeatureKey, keyValidation)
}

class Variant private constructor(value: String): StringValue(value), ComparableValue<Variant, String> {
    companion object: NonEmptyStringValueFactory<Variant>(::Variant)
}

