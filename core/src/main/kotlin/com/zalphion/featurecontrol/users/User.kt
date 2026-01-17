package com.zalphion.featurecontrol.users

import dev.forkhandles.values.Base64StringValueFactory
import dev.forkhandles.values.ComparableValue
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import org.http4k.core.Uri
import java.util.Base64

data class User(
    val userId: UserId,
    val userName: String?,
    val photoUrl: Uri?
) {
    val emailAddress = userId.toEmailAddress()

    fun fullName() = if (userName.isNullOrBlank()) emailAddress.value else {
        "$userName ($emailAddress)"
    }
}

class UserId private constructor(value: String): StringValue(value), ComparableValue<UserId, String> {
    companion object: Base64StringValueFactory<UserId>(::UserId) {
        private val decoder = Base64.getUrlDecoder()
    }

    fun toEmailAddress() = EmailAddress.parse(decoder.decode(value).decodeToString())
}

class EmailAddress private constructor(value: String): StringValue(value), ComparableValue<EmailAddress, String> {
    companion object: StringValueFactory<EmailAddress>(::EmailAddress) {
        private val encoder = Base64.getUrlEncoder().withoutPadding()
    }

    fun toUserId() = UserId.parse(encoder.encodeToString(value.toByteArray()))
}