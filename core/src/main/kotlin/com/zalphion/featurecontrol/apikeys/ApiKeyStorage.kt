package com.zalphion.featurecontrol.apikeys

import com.zalphion.featurecontrol.storage.Repository
import com.zalphion.featurecontrol.crypto.Base64String
import com.zalphion.featurecontrol.auth.EnginePrincipal
import com.zalphion.featurecontrol.lib.toBiDiMapping
import com.zalphion.featurecontrol.storage.StorageCompanion
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class StoredApiKey(
    val enginePrincipal: EnginePrincipal,
    val encryptedApiKey: Base64String,
    val hashedApiKey: Base64String
)

class ApiKeyStorage private constructor(private val repository: Repository<StoredApiKey, EnginePrincipal, Base64String>) {

    operator fun get(hashedApiKey: Base64String): EnginePrincipal? = repository
        .listInverse(hashedApiKey)
        .firstOrNull()
        ?.enginePrincipal

    operator fun get(enginePrincipal: EnginePrincipal): Base64String? = repository
        .list(enginePrincipal)
        .firstOrNull()
        ?.encryptedApiKey

    operator fun set(enginePrincipal: EnginePrincipal, pair: ApiKeyPair) = repository.save(
        groupId = enginePrincipal,
        itemId = pair.hashed,
        doc = StoredApiKey(enginePrincipal, pair.encrypted, pair.hashed)
    )

    companion object: StorageCompanion<ApiKeyStorage, StoredApiKey, EnginePrincipal, Base64String>(
        documentType = StoredApiKey::class,
        groupIdMapping = EnginePrincipal.toBiDiMapping(),
        itemIdMapping = Base64String.toBiDiMapping(),
        createFn = ::ApiKeyStorage
    )
}