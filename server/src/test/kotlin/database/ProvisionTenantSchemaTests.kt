package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.database.provisionTenantSchema
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.tables.tenant.AuthorTable
import dev.jovanni0.tables.User
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.assertEquals



@Testcontainers
class ProvisionTenantSchemaTests
{
    companion object
    {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")

        lateinit var data_source: HikariDataSource
    }


    @BeforeEach
    fun setUp()
    {
        val hikari_config = HikariConfig().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username
            password = postgres.password
            maximumPoolSize = 5
            schema = "public"
        }
        data_source = HikariDataSource(hikari_config)
        Database.connect(data_source)

        transaction {
            SchemaUtils.create(User)
        }
    }


    @AfterEach
    fun tearDown()
    {
        transaction {
            SchemaUtils.drop(User)
        }

        data_source.close()
    }


    @Test
    fun `provisionTenantSchema create a proper schema`()
    {
        val user_id = "john123"

        provisionTenantSchema(data_source, user_id)

        val row_count = tenantTransaction("user_$user_id") {
            AuthorTable
                .selectAll()
                .count()
        }

        assertEquals(0, row_count)
    }
}
