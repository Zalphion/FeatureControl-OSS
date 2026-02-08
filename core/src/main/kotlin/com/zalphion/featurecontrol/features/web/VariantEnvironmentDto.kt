package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.SubjectId
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.features.Weight
import se.ansman.kotshi.JsonSerializable

interface VariantEnvironmentDto {
    val name: Variant
    val weight: Weight?
    val subjectIds: Set<SubjectId>
}

@JsonSerializable
data class CoreVariantEnvironmentDto(
    override val name: Variant,
    override val weight: Weight?,
    override val subjectIds: Set<SubjectId>
): VariantEnvironmentDto

fun FeatureEnvironment.toCoreDto(variant: Variant) = CoreVariantEnvironmentDto(
    name = variant,
    weight = weights[variant],
    subjectIds = overrides.filter { it.value == variant }.keys.toSet()
)