package com.zalphion.featurecontrol.auth

import com.zalphion.featurecontrol.members.MemberStorage
import com.zalphion.featurecontrol.users.User
import com.zalphion.featurecontrol.users.UserId
import com.zalphion.featurecontrol.users.UserStorage

interface PermissionsFactory {
    fun create(userId: UserId): Permissions<User>?

    companion object {
        fun teamMembership(users: UserStorage, members: MemberStorage) = object: PermissionsFactory {
            override fun create(userId: UserId): Permissions<User>? {
                val user = users[userId] ?: return null
                val memberships = members.list(userId)
                return Permissions.teamMembership(user, memberships.map { it.teamId })
            }
        }
    }
}