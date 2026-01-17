package com.zalphion.featurecontrol.users

import com.zalphion.featurecontrol.storage.EmptyKey
import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.storage.Storage
import com.zalphion.featurecontrol.lib.asBiDiMapping
import com.zalphion.featurecontrol.lib.toBiDiMapping
import dev.forkhandles.result4k.asResultOr
import org.http4k.core.Uri
import org.http4k.format.AutoMarshalling
import se.ansman.kotshi.JsonSerializable
import java.net.URI

class UserStorage private constructor(private val repository: Repository<StoredUser, UserId, EmptyKey>) {
    operator fun get(userId: UserId) = repository[userId, EmptyKey.INSTANCE]?.toModel()
    operator fun get(userIds: Collection<UserId>) = repository[userIds.map { it to EmptyKey.INSTANCE }].map { it.toModel() }
    operator fun get(emailAddress: EmailAddress) = repository[emailAddress.toUserId(), EmptyKey.INSTANCE]?.toModel()
    operator fun plusAssign(user: User) = repository.save(user.userId, EmptyKey.INSTANCE, user.toStored())

    fun getOrFail(emailAddress: EmailAddress) =
        get(emailAddress).asResultOr { userNotFoundByEmail(emailAddress) }

    fun getOrFail(userId: UserId) =
        get(userId).asResultOr { userNotFound(userId) }

    companion object {
        fun create(storage: Storage, json: AutoMarshalling) = UserStorage(storage.create(
            name = "users",
            groupIdMapper = UserId.toBiDiMapping(),
            itemIdMapper = EmptyKey.toBiDiMapping(),
            documentMapper = json.asBiDiMapping()
        ))
    }
}

@JsonSerializable
data class StoredUser(
    val userId: UserId,
    val userName: String?,
    val photoUrl: Uri?
)

private fun User.toStored() = StoredUser(userId, userName, photoUrl)
private fun StoredUser.toModel() = User(userId, userName, photoUrl)