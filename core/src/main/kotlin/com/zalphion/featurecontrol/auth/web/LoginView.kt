package com.zalphion.featurecontrol.auth.web

import com.zalphion.featurecontrol.web.pageSkeleton
import com.zalphion.featurecontrol.Core

fun Core.loginView() = pageSkeleton(emptyList(), "Login") {
    socialLogin(config)
}