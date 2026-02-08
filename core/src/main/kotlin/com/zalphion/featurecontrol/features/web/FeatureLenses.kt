package com.zalphion.featurecontrol.features.web

import com.zalphion.featurecontrol.features.FeatureCreateData
import com.zalphion.featurecontrol.features.FeatureEnvironment
import com.zalphion.featurecontrol.features.FeatureKey
import com.zalphion.featurecontrol.features.FeatureUpdateData
import com.zalphion.featurecontrol.features.Variant
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


internal fun createCoreFeatureEnvironmentLens(json: AutoMarshalling): BodyLens<FeatureEnvironment> {
    val variants = FormField
        .map { json.asA<Array<CoreVariantEnvironmentDto>>(it) }
        .required("variants")

    return Body
        .webForm(Validator.Strict, variants)
        .map(variants)
        .map { variants ->
            FeatureEnvironment(
                weights = variants
                    .filter { it.weight != null }
                    .associate { it.name to it.weight!! },
                overrides = variants
                    .flatMap { variant -> variant.subjectIds.map { it to variant.name } }
                    .toMap(),
                extensions = emptyMap()
            )
        }.toLens()
}