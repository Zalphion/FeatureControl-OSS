package com.zalphion.featurecontrol.storage.mariadb

import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.StorageDriverContract
import org.http4k.core.Credentials
import org.http4k.core.Uri
import org.http4k.core.query
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@Testcontainers
class MariaDbStorageDriverTest: StorageDriverContract({ pageSize ->
    StorageDriver.mariaDb(
        // Base URL usually looks like jdbc:mariadb://localhost:port/test
        // We want to replace the 'test' database with a unique database per test
        jdbcUrl = Uri.of(mariaDb.jdbcUrl.substringBeforeLast("/") + "/$${UUID.randomUUID()}")
            .query("createDatabaseIfNotExist", "true"),
        credentials = Credentials(mariaDb.username, mariaDb.password),
        pageSize = pageSize
    )
}) {
    companion object {
        @Container
        @JvmStatic
        val mariaDb: JdbcDatabaseContainer<*> = MariaDBContainer("mariadb:12.1-noble")
            // need to run as root for permission to create databases
            .withUsername("root")
    }
}