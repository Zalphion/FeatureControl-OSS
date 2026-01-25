package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.SubjectId
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.features.Weight
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class VariantEnvironmentDto(
    val name: Variant,
    val weight: Weight?,
    val subjectIds: Set<SubjectId>
)

fun FeatureEnvironment.toDto(variant: Variant) = VariantEnvironmentDto(
    name = variant,
    weight = weights[variant],
    subjectIds = overrides.filter { it.value == variant }.keys.toSet()
)