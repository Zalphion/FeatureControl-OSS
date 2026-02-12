package com.zalphion.featurecontrol.storage.couchdb

import com.zalphion.featurecontrol.couchdb.couchDb
import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.StorageDriverContract
import org.http4k.client.Java8HttpClient
import org.http4k.core.Credentials
import org.http4k.core.Uri
import org.http4k.filter.debug
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

private const val PORT = 5984
private const val SERVER_ADMIN_USERNAME = "admin" // must be admin to create databases
private const val SERVER_ADMIN_PASSWORD = "password"

@Testcontainers
class CouchDbStorageDriverTest: StorageDriverContract({ pageSize ->
    val databaseHost = Uri.of("http://${couchDb.host}").port(couchDb.getMappedPort(PORT))
    val databaseName = "db-${UUID.randomUUID()}"
    val internet = Java8HttpClient().debug()

    // create a new user to be the admin for just this database
    val adminClient = CouchDbAdminClient(
        host = databaseHost,
        credentials = Credentials(SERVER_ADMIN_USERNAME, SERVER_ADMIN_PASSWORD),
        internet = internet
    )
    val dbAdmin = CouchDbUser(
        name = "user-$databaseName",
        password = "password",
        roles = emptyList()
    )
    adminClient.createDatabase("_users") // table may not exist yet
    adminClient.createUser(dbAdmin)
    adminClient.createDatabase(databaseName)
    adminClient.updateSecurity(databaseName) { existing ->
        existing.copy(
            admins = existing.admins.copy(
                names = existing.admins.names + dbAdmin.name
            )
        )
    }

    StorageDriver.couchDb(
        databaseUri = databaseHost.path(databaseName),
        credentials = Credentials(dbAdmin.name, dbAdmin.password),
        internet = internet,
        pageSize = pageSize
    )
}) {
    companion object {
        @Container
        @JvmStatic
        val couchDb: GenericContainer<*> = GenericContainer("couchdb:3.5")
            .withExposedPorts(PORT)
            .withEnv("COUCHDB_USER", SERVER_ADMIN_USERNAME)
            .withEnv("COUCHDB_PASSWORD", SERVER_ADMIN_PASSWORD)
            .withEnv("TANGO_BOOTSTRAP", "1") // ensure the node starts in a "ready" single-node state
            .waitingFor(Wait.forHttp("/").forStatusCode(200))
    }
}