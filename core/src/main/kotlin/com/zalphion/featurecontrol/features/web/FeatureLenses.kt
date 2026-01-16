package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.features.SubjectId
import com.zalphion.featurecontrol.features.Variant
import com.zalphion.featurecontrol.features.Weight
import com.zalphion.featurecontrol.lib.Update
import com.zalphion.featurecontrol.lib.asBiDiMapping
import org.http4k.core.Body
import org.http4k.format.AutoMarshalling
import org.http4k.lens.BodyLens
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.map
import org.http4k.lens.string
import org.http4k.lens.value
import org.http4k.lens.webForm
import kotlin.collections.associate
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.map

object FeatureForm {
    val featureKey = FormField.value(FeatureKey).required("featureKey")
    val defaultVariant = FormField.value(Variant).required("defaultVariant")
    val description = FormField.string().defaulted("description", "")

    fun variants(json: AutoMarshalling) = FormField.map(json.asBiDiMapping<Array<VariantDto>>())
        .map { it.associate { variant -> variant.name to variant.description } }
        .required("variants")
}

internal fun createCoreFeatureCreateDataLens(json: AutoMarshalling): BodyLens<FeatureCreateData> {
    val variants =  FeatureForm.variants(json)
    return Body
        .webForm(Validator.Strict, FeatureForm.featureKey, variants, FeatureForm.defaultVariant, FeatureForm.description)
        .map { form ->
            FeatureCreateData(
                featureKey = FeatureForm.featureKey(form),
                variants = variants(form),
                defaultVariant = FeatureForm.defaultVariant(form),
                description = FeatureForm.description(form),
                environments = emptyMap(),
                extensions = emptyMap()
            )
        }.toLens()
}

internal fun createCoreFeatureUpdateDataLens(json: AutoMarshalling): BodyLens<FeatureUpdateData> {
    val variants = FeatureForm.variants(json)
    return Body
        .webForm(Validator.Strict, variants, FeatureForm.defaultVariant, FeatureForm.description)
        .map { form ->
            FeatureUpdateData(
                variants = Update(variants(form)),
                defaultVariant = Update(FeatureForm.defaultVariant(form)),
                description = Update(FeatureForm.description(form)),
                environmentsToUpdate = null,
                extensions = null
            )
        }
        .toLens()
}


object FeatureEnvironmentForm {
    fun weights(json: AutoMarshalling) = FormField
        .map { json.asA<Map<String, Int>>(it) }
        .map { it.mapKeys { (k, _) -> Variant.parse(k) } }
        .map { it.mapValues { (_, v) -> Weight.of(v) } }
        .required("weights")
    fun overrides(json: AutoMarshalling) = FormField
        .map { json.asA<Map<String, List<String>>>(it) }
        .map { it.mapKeys { (k, _) -> Variant.parse(k) } }
        .map { it.mapValues { (_, v) -> v.map(SubjectId::parse) } }
        .map { it.flatMap { (variantName, subjectIds) -> subjectIds.map { id -> id to variantName } }.toMap() }
        .required("overrides")
}

internal fun createCoreFeatureEnvironmentLens(json: AutoMarshalling): BodyLens<FeatureEnvironment> {
    val weights = FeatureEnvironmentForm.weights(json)
    val overrides = FeatureEnvironmentForm.overrides(json)
    return Body
        .webForm(Validator.Strict, weights, overrides)
        .map { form ->
            FeatureEnvironment(
                weights = weights(form),
                overrides = overrides(form),
                extensions = emptyMap()
            )
        }.toLens()
}