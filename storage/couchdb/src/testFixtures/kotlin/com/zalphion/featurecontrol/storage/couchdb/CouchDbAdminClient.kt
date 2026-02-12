package com.zalphion.featurecontrol.storage.couchdb

import com.zalphion.featurecontrol.couchdb.CouchDbDatabaseClient
import org.http4k.client.JavaHttpClient
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ClientFilters

class CouchDbAdminClient(
    private val host: Uri,
    private val credentials: Credentials,
    private val internet: HttpHandler = JavaHttpClient()
) {
    private val http = ClientFilters.SetHostFrom(host)
        .then(ClientFilters.BasicAuth(credentials))
        .then(internet)

    fun createDatabase(databaseName: String) {
        CouchDbDatabaseClient(
            databaseUri = host.path(databaseName),
            credentials = credentials,
            internet = internet
        ).createTable()
    }

    fun createUser(user: CouchDbUser) {
        Request.Companion(Method.PUT, "_users/${user.id}")
            .with(CouchDbUser.lens of user)
            .let(http)
            // 409 is fine; just means the user already exists
            .also { if (!it.status.successful && it.status != Status.CONFLICT) error(it) }
    }

    fun getSecurity(databaseName: String): CouchDbSecurity {
        return Request.Companion(Method.GET, "$databaseName/_security")
            .let(http)
            .also { if (!it.status.successful) error(it) }
            .let(CouchDbSecurity.lens)
    }

    fun updateSecurity(databaseName: String, security: CouchDbSecurity) {
        Request.Companion(Method.PUT, "$databaseName/_security")
            .with(CouchDbSecurity.lens of security)
            .let(http)
            .also { if (!it.status.successful) error(it) }
    }

    fun updateSecurity(databaseName: String, securityFn: (CouchDbSecurity) -> CouchDbSecurity) {
        val existing = getSecurity(databaseName)
        updateSecurity(databaseName, securityFn(existing))
    }
}