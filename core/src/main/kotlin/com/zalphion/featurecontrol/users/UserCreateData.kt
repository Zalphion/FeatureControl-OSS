package com.zalphion.featurecontrol.users

import java.net.URI

data class UserCreateData(
    val emailAddress: EmailAddress,
    val userName: String?,
    val photoUrl: URI?,
)

fun UserCreateData.toUser() = User(
    userId = emailAddress.toUserId(),
    userName = userName,
    photoUrl = photoUrl
)