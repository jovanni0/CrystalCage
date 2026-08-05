package repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.exceptions.not_found.AuthorNotFoundException
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.UniverseRepository
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid



@Testcontainers
class UniverseRepositoryTests
{
    private val author_repo = AuthorRepository()
    private val universe_repo = UniverseRepository()


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
                exec("TRUNCATE TABLE \"$it\".universe RESTART IDENTITY CASCADE")
            }
        }
    }


    @Test
    fun `'findAllByAuthor' retrieves universes successfully, sorted`()
    {
        val author_id = author_repo.create(tenants[0], "John", "Dow").id

        universe_repo.create(tenants[0], "uni2", author_id)
        universe_repo.create(tenants[0], "uni1", author_id)

        val result = universe_repo.findAllByAuthorId(tenants[0], author_id)

        assertEquals(2, result.size)
        assertEquals("uni1", result[0].name)
    }


    @Test
    fun `'findAllByAuthor' returns empty list when no universes exist`()
    {
        val author_id = author_repo.create(tenants[0], "John", "Doe").id

        val result = universe_repo.findAllByAuthorId(tenants[0], author_id)

        assertTrue(result.isEmpty())
    }


    @Test
    fun `'findAllByAuthor' returns empty list when author does not exist`()
    {
        val author1_id = Uuid.random()
        val author2_id = author_repo.create(tenants[0], "John", "Doe").id

        universe_repo.create(tenants[0], "Test", author2_id)

        val result = universe_repo.findAllByAuthorId(tenants[0], author1_id)

        assertTrue(result.isEmpty())
    }


    @Test
    fun `'findAllByAuthor' does not return universes from other tenants`()
    {
        val author_1_id = author_repo.create(tenants[0], "John", "Doe").id
        val author_2_id = author_repo.create(tenants[1], "Jane", "Doe").id

        universe_repo.create(tenants[0], "Test1", author_1_id)
        universe_repo.create(tenants[1], "Test2", author_2_id)

        val result = universe_repo.findAllByAuthorId(tenants[0], author_1_id)

        assertEquals(1, result.size)
        assertEquals("Test1", result[0].name)
    }


    @Test
    fun `'create' throws AuthorNotFoundException if author_id does not exist in AuthorTable`()
    {
        val author_id = Uuid.random()

        val exception = assertThrows<AuthorNotFoundException>()
        {
            universe_repo.create(tenants[0], "test", author_id)
        }

        assertEquals("author $author_id does not exist", exception.message)
    }


    @Test
    fun `'create' successfully create universe`()
    {
        val author_id = author_repo.create(tenants[0], "John", "Doe").id

        val result = universe_repo.create(tenants[0], "Test", author_id)

        assertEquals(result.name, "Test")
        assertNotNull(result.id)
    }


    @Test
    fun `'create' throws AuthorNotFoundException if author exists only in another schema`()
    {
        val author_id = author_repo.create(tenants[0], "John", "Doe").id

        val exception = assertThrows<AuthorNotFoundException>()
        {
            universe_repo.create(tenants[1], "Test", author_id)
        }

        assertEquals("author $author_id does not exist", exception.message)
    }


    @Nested
    inner class FindById
    {
        @Test
        fun `returns the correct owning author`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val universe = universe_repo.create(schema, "Some Universe", author.id)

            val result = universe_repo.findById(schema, universe.id)

            assertNotNull(result)
            assertEquals(author.id, result.author_id)
        }


        @Test
        fun `returns null for non-existent universe`()
        {
            val schema = tenants[0]

            val result = universe_repo.findById(schema, Uuid.random())

            assertNull(result)
        }
    }


    @Nested
    inner class Exists
    {
        @Test
        fun `check does not see universes from a different tenant`()
        {
            val author_schema1 = author_repo.create(tenants[0], "First", "Last")
            val universe_schema1 = universe_repo.create(tenants[0], "Universe", author_schema1.id)

            val result = universe_repo.exists(tenants[1], universe_schema1.id)

            assertFalse(result)
        }
    }
}
