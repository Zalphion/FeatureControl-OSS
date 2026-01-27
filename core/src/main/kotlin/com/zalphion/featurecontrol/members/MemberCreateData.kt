package com.zalphion.featurecontrol.members

import com.zalphion.featurecontrol.plugins.Extensions
import com.zalphion.featurecontrol.users.EmailAddress

class MemberCreateData(
    val emailAddress: EmailAddress,
    val extensions: Extensions
)