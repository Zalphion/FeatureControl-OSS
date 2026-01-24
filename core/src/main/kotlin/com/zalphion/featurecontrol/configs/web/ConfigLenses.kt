package com.zalphion.featurecontrol.configs.web

import com.zalphion.featurecontrol.configs.Property
import com.zalphion.featurecontrol.configs.PropertyKey
import com.zalphion.featurecontrol.lib.asBiDiMapping
import org.http4k.core.Body
import org.http4k.format.AutoMarshalling
import org.http4k.lens.BodyLens
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.map
import org.http4k.lens.webForm

private object ConfigLenses {
    fun properties(json: AutoMarshalling) = FormField
        .map(json.asBiDiMapping<Array<ConfigPropertyDto>>())
        .map { it.associate { prop -> prop.toModel() } }
        .required("properties")

    fun values(json: AutoMarshalling) = FormField.map(json.asBiDiMapping<Map<String, String>>())
        .map { it.mapKeys { (k, _) -> PropertyKey.parse(k) } }
        .required("values")
}

internal fun createCoreConfigSpecDataLens(json: AutoMarshalling): BodyLens<Map<PropertyKey, Property>> {
    val properties = ConfigLenses.properties(json)
    return Body
        .webForm(Validator.Strict, properties)
        .map(properties)
        .toLens()
}

internal fun createCoreConfigEnvironmentDataLens(json: AutoMarshalling): BodyLens<Map<PropertyKey, String>> {
    val values = ConfigLenses.values(json)
    return Body
        .webForm(Validator.Strict, values)
        .map(values)
        .toLens()
}