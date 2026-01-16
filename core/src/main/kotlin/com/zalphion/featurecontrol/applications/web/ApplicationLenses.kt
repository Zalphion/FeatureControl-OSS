package com.zalphion.featurecontrol.applications.web

import com.zalphion.featurecontrol.applications.ApplicationCreateData
import com.zalphion.featurecontrol.applications.AppName
import com.zalphion.featurecontrol.applications.ApplicationUpdateData
import com.zalphion.featurecontrol.lib.asBiDiMapping
import org.http4k.core.Body
import org.http4k.format.AutoMarshalling
import org.http4k.lens.BodyLens
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.map
import org.http4k.lens.value
import org.http4k.lens.webForm

object ApplicationForm {
    val appName = FormField.value(AppName).required("name")
    val appNameOptional = FormField.value(AppName).optional("name")

    fun environments(json: AutoMarshalling) = FormField
        .map(json.asBiDiMapping<Array<EnvironmentDto>>())
        .required("environments")
}

internal fun createCoreApplicationCreateDataLens(json: AutoMarshalling): BodyLens<ApplicationCreateData> {
    val environments = ApplicationForm.environments(json)
    return Body
        .webForm(Validator.Strict, ApplicationForm.appName, environments)
        .map { form ->
            ApplicationCreateData(
                appName = ApplicationForm.appName(form),
                environments = environments(form).map { it.toModel() },
                extensions = emptyMap()
            )
        }
        .toLens()
}

internal fun createCoreApplicationUpdateDataLens(json: AutoMarshalling): BodyLens<ApplicationUpdateData> {
    val environments = ApplicationForm.environments(json)
    return Body
        .webForm(
            Validator.Strict, ApplicationForm.appNameOptional, environments)
        .map { form ->
            ApplicationUpdateData(
                appName = ApplicationForm.appNameOptional(form),
                environments = environments(form).map { it.toModel() },
                extensions = emptyMap()
            )
        }.toLens()
}