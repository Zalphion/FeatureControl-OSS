package com.zalphion.featurecontrol.members.web

import com.zalphion.featurecontrol.members.MemberDetails
import com.zalphion.featurecontrol.plugins.Component
import kotlinx.html.h5

class RoleComponent(val member: MemberDetails) {

    companion object {
        fun core() = Component<RoleComponent> { flow, data ->
            flow.h5 {
                +"Member"
            }
        }
    }
}