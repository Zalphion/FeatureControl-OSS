package com.zalphion.featurecontrol.storage.postgres

import com.zalphion.featurecontrol.storage.StorageDriver
import com.zalphion.featurecontrol.storage.StorageDriverContract
import org.http4k.core.Credentials
import org.http4k.core.Uri
import org.http4k.core.query
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.UUID

@Testcontainers
class PostgresStorageDriverTest: StorageDriverContract({ pageSize -> StorageDriver.postgres(
    jdbcUrl = Uri.of(postgres.jdbcUrl).query("currentSchema", UUID.randomUUID().toString()),
    credentials = Credentials(postgres.username, postgres.password),
    pageSize = pageSize
)}) {

    companion object {
        @Container
        @JvmStatic
        val postgres: JdbcDatabaseContainer<*> = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("feature-control")
            .withUsername("test")
            .withPassword("test")
    }
}