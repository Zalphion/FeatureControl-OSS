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
private const val USERNAME = "admin" // must be admin to create databases
private const val PASSWORD = "password"

@Testcontainers
class CouchDbStorageDriverTest: StorageDriverContract({ pageSize ->
    StorageDriver.couchDb(
        internet = Java8HttpClient().debug(),
        host = Uri.of("http://${couchDb.host}").port(couchDb.getMappedPort(PORT)),
        credentials = Credentials(USERNAME, PASSWORD),
        dbName = "db-${UUID.randomUUID()}",
        pageSize = pageSize
    )
}) {

    companion object {
        @Container
        @JvmStatic
        val couchDb: GenericContainer<*> = GenericContainer("couchdb:3.5")
            .withExposedPorts(PORT)
            .withEnv("COUCHDB_USER", USERNAME)
            .withEnv("COUCHDB_PASSWORD", PASSWORD)
            .withEnv("TANGO_BOOTSTRAP", "1") // ensure the node starts in a "ready" single-node state
            .waitingFor(Wait.forHttp("/").forStatusCode(200))
    }
}