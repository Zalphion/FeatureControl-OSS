package com.zalphion.featurecontrol.users

import com.zalphion.featurecontrol.storage.EmptyKey
import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import dev.forkhandles.result4k.asResultOr
import org.http4k.format.AutoMarshalling

class UserStorage private constructor(private val repository: Repository<User, UserId, EmptyKey>) {
    operator fun get(userId: UserId) = repository[userId, EmptyKey.INSTANCE]
    operator fun get(userIds: Collection<UserId>) = repository[userIds.map { it to EmptyKey.INSTANCE }]
    operator fun get(emailAddress: EmailAddress) = repository[emailAddress.toUserId(), EmptyKey.INSTANCE]
    operator fun plusAssign(user: User) = repository.save(user.userId, EmptyKey.INSTANCE, user)

    fun getOrFail(emailAddress: EmailAddress) =
        get(emailAddress).asResultOr { userNotFoundByEmail(emailAddress) }

    fun getOrFail(userId: UserId) =
        get(userId).asResultOr { userNotFound(userId) }

    companion object {
        fun create(storage: Storage, json: AutoMarshalling) = UserStorage(storage.create(
            name = "users",
            groupIdMapper = UserId.toBiDiMapping(),
            itemIdMapper = EmptyKey.toBiDiMapping(),
            documentMapper = json.asBiDiMapping<User>() // TODO DTO
        ))
    }
}