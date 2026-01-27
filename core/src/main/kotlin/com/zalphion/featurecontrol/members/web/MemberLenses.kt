package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.members.MemberCreateData
import com.zalphion.featurecontrol.users.EmailAddress
import org.http4k.core.Body
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.value
import org.http4k.lens.webForm

object MemberLenses {
    val email = FormField.value(EmailAddress).required("emailAddress")

    fun coreCreate() = Body.webForm(Validator.Strict, email)
        .map { form -> MemberCreateData(
            emailAddress = email(form),
            extensions = emptyMap()
        ) }
        .toLens()
}