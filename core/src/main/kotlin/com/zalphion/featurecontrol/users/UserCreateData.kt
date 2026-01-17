package com.zalphion.featurecontrol.users

import org.http4k.core.Uri

data class UserCreateData(
    val emailAddress: EmailAddress,
    val userName: String?,
    val photoUrl: Uri?,
)

fun UserCreateData.toUser() = User(
    userId = emailAddress.toUserId(),
    userName = userName,
    photoUrl = photoUrl
)