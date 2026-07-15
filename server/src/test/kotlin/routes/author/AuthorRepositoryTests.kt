package routes.author

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.repositories.AuthorRepository
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue



@Testcontainers
class AuthorRepositoryTests
{
    private val repo = AuthorRepository()


    companion object
    {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16")

        private lateinit var data_source: HikariDataSource
        private val tenants = listOf("user_test-user1", "user_test-user2")


        @BeforeAll
        @JvmStatic
        fun setUpAll()
        {
            val hikari_config = HikariConfig().apply {
                jdbcUrl = postgres.jdbcUrl
                username = postgres.username
                password = postgres.password
                maximumPoolSize = 5
                schema = "public"
            }
            data_source = HikariDataSource(hikari_config)

            tenants.forEach {
                Flyway
                    .configure()
                    .dataSource(data_source)
                    .schemas(it)
                    .createSchemas(true)
                    .locations("classpath:db/migration/tenant")
                    .load()
                    .migrate()
            }

            Database.connect(data_source)
        }


        @AfterAll
        @JvmStatic
        fun tearDownAll()
        {
            if (::data_source.isInitialized)
            {
                data_source.close()
            }
        }
    }


    @BeforeEach
    fun setUpEach()
    {
        transaction {
            tenants.forEach {
                exec("TRUNCATE TABLE \"$it\".author RESTART IDENTITY CASCADE")
            }
        }
    }


    @Test
    fun `insert author successfully`()
    {
        val created = repo.create(tenants[0], "John", "Doe")

        assertNotNull(created.id)
        assertEquals("Doe", created.last_name)
    }


    @Test
    fun `retrieve created authors successfully`()
    {
        repo.create(tenants[0], "John", "Doe")
        repo.create(tenants[0], "Mike", "Small")

        val result = repo.findAll(tenants[0])

        assertEquals(2, result.size)
        assertEquals("John", result[0].first_name)
        assertEquals("Small", result[1].last_name)
    }


    @Test
    fun `returns empty list when no authors exist`()
    {
        val result = repo.findAll(tenants[0])

        assertTrue(result.isEmpty())
    }


    @Test
    fun `does not return authors from other tenants`()
    {
        repo.create(tenants[0], "John", "Doe")
        repo.create(tenants[1], "Mike", "Small")

        val result = repo.findAll(tenants[0])

        assertEquals(1, result.size)
        assertEquals("John", result[0].first_name)
    }
}
