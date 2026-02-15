package com.zalphion.featurecontrol.auth.web

import com.zalphion.featurecontrol.web.pageSkeleton
import com.zalphion.featurecontrol.Core
import com.zalphion.featurecontrol.FeatureControl

fun FeatureControl.loginView() = pageSkeleton(emptyList(), "Login") {
    socialLogin(config)
}