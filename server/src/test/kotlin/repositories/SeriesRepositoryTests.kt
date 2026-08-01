package repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.ReadRecordRepository
import dev.jovanni0.repositories.SeriesRepository
import dev.jovanni0.repositories.UniverseRepository
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid



@Testcontainers
class SeriesRepositoryTests
{
    private val author_repo = AuthorRepository()
    private val universe_repo = UniverseRepository()
    private val series_repo = SeriesRepository()


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
                exec("TRUNCATE TABLE \"$it\".read_record, \"$it\".book, \"$it\".series, \"$it\".universe, \"$it\".author RESTART IDENTITY CASCADE")
            }
        }
    }



    @Nested
    inner class FindAllByAuthorIdAndUniverseId
    {
        @Test
        fun `returns all series when no filters provided`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            series_repo.create(schema, author.id, null, "Zebra", false)
            series_repo.create(schema, author.id, null, "Apple", false)

            val result = series_repo.findAllByAuthorIdAndUniverseId(schema, null, null)

            assertEquals(2, result.size)
        }


        @Test
        fun `filters by author_id only`()
        {
            val schema = tenants[0]
            val author1 = author_repo.create(schema, "First", "One")
            val author2 = author_repo.create(schema, "First", "Two")

            val series1 = series_repo.create(schema, author1.id, null, "Book One", false)
            series_repo.create(schema, author2.id, null, "Book Two", false)

            val result = series_repo.findAllByAuthorIdAndUniverseId(schema, author1.id, null)

            assertEquals(1, result.size)
            assertEquals(series1.id, result.first().id)
        }


        @Test
        fun `filters by universe_id only`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val universe1 = universe_repo.create(schema, "Universe One", author.id)
            val universe2 = universe_repo.create(schema, "Universe Two", author.id)

            val series1 = series_repo.create(schema, author.id, universe1.id, "Series A", false)
            series_repo.create(schema, author.id, universe2.id, "Series B", false)

            val result = series_repo.findAllByAuthorIdAndUniverseId(schema, null, universe1.id)

            assertEquals(1, result.size)
            assertEquals(series1.id, result.first().id)
        }


        @Test
        fun `filters by both author_id and universe_id`()
        {
            val schema = tenants[0]
            val author1 = author_repo.create(schema, "First", "One")
            val author2 = author_repo.create(schema, "First", "Two")
            val universe1 = universe_repo.create(schema, "Shared Universe", author1.id)

            val target = series_repo.create(schema, author1.id, universe1.id, "Target Series", false)
            series_repo.create(schema, author1.id, null, "Author Only Series", false)
            series_repo.create(schema, author2.id, null, "Other Series", false)

            val result = series_repo.findAllByAuthorIdAndUniverseId(schema, author1.id, universe1.id)

            assertEquals(1, result.size)
            assertEquals(target.id, result.first().id)
        }


        @Test
        fun `returns empty list when filters match nothing`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            series_repo.create(schema, author.id, null, "Some Series", false)

            val unrelated_author_id = author_repo.create(schema, "Other", "Person").id

            val result = series_repo.findAllByAuthorIdAndUniverseId(schema, unrelated_author_id, null)

            assertEquals(0, result.size)
        }


        @Test
        fun `results are sorted alphabetically by name`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")

            series_repo.create(schema, author.id, null, "Zebra", false)
            series_repo.create(schema, author.id, null, "Apple", false)
            series_repo.create(schema, author.id, null, "Mango", false)

            val result = series_repo.findAllByAuthorIdAndUniverseId(schema, author.id, null)

            assertEquals(listOf("Apple", "Mango", "Zebra"), result.map { it.name })
        }


        @Test
        fun `series with null universe_id are included when filtering by author_id only`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val universe = universe_repo.create(schema, "Some Universe", author.id)

            series_repo.create(schema, author.id, universe.id, "In Universe", false)
            series_repo.create(schema, author.id, null, "No Universe", false)

            val result = series_repo.findAllByAuthorIdAndUniverseId(schema, author.id, null)

            assertEquals(2, result.size)
        }


        @Test
        fun `does not leak series across tenant schemas`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]

            val author1 = author_repo.create(schema1, "First", "Last")
            series_repo.create(schema1, author1.id, null, "Tenant One Series", false)

            val author2 = author_repo.create(schema2, "First", "Last")
            series_repo.create(schema2, author2.id, null, "Tenant Two Series", false)

            val result_schema1 = series_repo.findAllByAuthorIdAndUniverseId(schema1, null, null)
            val result_schema2 = series_repo.findAllByAuthorIdAndUniverseId(schema2, null, null)

            assertEquals(1, result_schema1.size)
            assertEquals("Tenant One Series", result_schema1.first().name)
            assertEquals(1, result_schema2.size)
            assertEquals("Tenant Two Series", result_schema2.first().name)
        }
    }



    @Nested
    inner class FindById
    {
        private val book_repo = BookRepository()
        private val read_record_repo = ReadRecordRepository()


        @Test
        fun `returns null when series does not exist`()
        {
            val schema = tenants[0]

            val result = series_repo.findById(schema, Uuid.random())

            assertNull(result)
        }


        @Test
        fun `returns series with empty books list when it has no books`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Empty Series", false)

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)
            assertEquals(series.id.toString(), result.id)
            assertEquals(0, result.books.size)
        }


        @Test
        fun `returns series-level fields from the series row, not a joined book row`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val universe = universe_repo.create(schema, "Some Universe", author.id)
            val series = series_repo.create(schema, author.id, universe.id, "My Series", true)

            val other_author = author_repo.create(schema, "Other", "Author")
            book_repo.create(schema, other_author.id, null, series.id, "Some Title")

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)
            assertEquals(series.id.toString(), result.id)
            assertEquals(author.id.toString(), result.author_id)
            assertEquals(universe.id.toString(), result.universe_id)
            assertEquals("My Series", result.name)
            assertEquals(true, result.is_finished)
        }


        @Test
        fun `book with no read records has UNREAD status and zero reread count`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)
            val book = book_repo.create(schema, author.id, null, series.id, "Book One")

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)
            assertEquals(1, result.books.size)

            val book_result = result.books.first()

            assertEquals(book.id.toString(), book_result.id)
            assertEquals(ReadStatus.UNREAD, book_result.read_status)
            assertEquals(0, book_result.reread_count)
        }


        @Test
        fun `single book with multiple read records is grouped correctly, not duplicated`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)
            val book = book_repo.create(schema, author.id, null, series.id, "Book One")

            read_record_repo.create(schema, book.id, null, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 10), false, false, null, null)
            read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)
            assertEquals(1, result.books.size, "book should appear once despite multiple joined read records")

            val book_result = result.books.first()

            assertEquals(ReadStatus.READ, book_result.read_status)
            assertEquals(2, book_result.reread_count)
        }


        @Test
        fun `multiple books each get their own independent read status, no cross-contamination`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)

            val book_unread = book_repo.create(schema, author.id, null, series.id, "Unread Book")
            val book_in_progress = book_repo.create(schema, author.id, null, series.id, "In Progress Book")
            val book_read = book_repo.create(schema, author.id, null, series.id, "Read Book")

            read_record_repo.create(schema, book_in_progress.id, null, LocalDate.of(2024, 1, 1), null, false, false, null, null)
            read_record_repo.create(schema, book_read.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)
            assertEquals(3, result.books.size)

            val statuses = result.books.associate { it.id to it.read_status }

            assertEquals(ReadStatus.UNREAD, statuses[book_unread.id.toString()])
            assertEquals(ReadStatus.IN_PROGRESS, statuses[book_in_progress.id.toString()])
            assertEquals(ReadStatus.READ, statuses[book_read.id.toString()])
        }


        @Test
        fun `handles null universe_id on the series without throwing`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "No Universe Series", false)

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)
            assertNull(result.universe_id)
        }


        @Test
        fun `handles all-null nullable book fields without throwing`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)
            val book = book_repo.create(schema, author.id, null, series.id, "Book With Nulls")

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)

            val book_result = result.books.first()

            assertEquals(book.id.toString(), book_result.id)
            assertNull(book_result.universe_id)
            assertNull(book_result.cover_id)
            assertNull(book_result.volume_number)
        }


        @Test
        fun `does not return books belonging to a different series`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series1 = series_repo.create(schema, author.id, null, "Series One", false)
            val series2 = series_repo.create(schema, author.id, null, "Series Two", false)

            book_repo.create(schema, author.id, null, series1.id, "Book In Series One")
            book_repo.create(schema, author.id, null, series2.id, "Book In Series Two")

            val result = series_repo.findById(schema, series1.id)

            assertNotNull(result)
            assertEquals(1, result.books.size)
            assertEquals("Book In Series One", result.books.first().title)
        }


        @Test
        fun `book with historical non-dnf record only has READ status and reread_count 1`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)
            val book = book_repo.create(schema, author.id, null, series.id, "Book One")

            read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)

            val book_result = result.books.first()

            assertEquals(ReadStatus.READ, book_result.read_status)
            assertEquals(1, book_result.reread_count)
        }


        @Test
        fun `book with historical dnf record only has DNF status and reread_count 0`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)
            val book = book_repo.create(schema, author.id, null, series.id, "Book One")

            read_record_repo.create(schema, book.id, null, null, null, true, true, null, null)

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)

            val book_result = result.books.first()

            assertEquals(ReadStatus.DNF, book_result.read_status)
            assertEquals(0, book_result.reread_count)
        }


        @Test
        fun `book with historical non-dnf record plus a dated reread is READ with count 2, not double-counted or dropped`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)
            val book = book_repo.create(schema, author.id, null, series.id, "Book One")

            read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)
            read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)

            val book_result = result.books.first()

            assertEquals(ReadStatus.READ, book_result.read_status)
            assertEquals(2, book_result.reread_count)
        }


        @Test
        fun `historical dnf record does not corrupt status when a dated non-dnf reread exists`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)
            val book = book_repo.create(schema, author.id, null, series.id, "Book One")

            read_record_repo.create(schema, book.id, null, null, null, true, true, null, null)
            read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)

            val book_result = result.books.first()

            assertEquals(ReadStatus.READ, book_result.read_status)
            assertEquals(1, book_result.reread_count)
        }


        @Test
        fun `two separate books each with their own historical record are not cross-contaminated`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Series", false)
            val book_a = book_repo.create(schema, author.id, null, series.id, "Book A")
            val book_b = book_repo.create(schema, author.id, null, series.id, "Book B")

            read_record_repo.create(schema, book_a.id, null, null, null, false, true, null, null) // historical, not dnf
            read_record_repo.create(schema, book_b.id, null, null, null, true, true, null, null)  // historical, dnf

            val result = series_repo.findById(schema, series.id)

            assertNotNull(result)

            val statuses = result.books.associate { it.id to it.read_status }

            assertEquals(ReadStatus.READ, statuses[book_a.id.toString()])
            assertEquals(ReadStatus.DNF, statuses[book_b.id.toString()])
        }
    }


    @Nested
    inner class Create
    {
        @Test
        fun `creates series with all fields and persists them`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val universe = universe_repo.create(schema, "Some Universe", author.id)

            val result = series_repo.create(schema, author.id, universe.id, "My Series", true)

            assertEquals(author.id, result.author_id)
            assertEquals(universe.id, result.universe_id)
            assertEquals("My Series", result.name)
            assertEquals(true, result.is_finished)
        }


        @Test
        fun `creates series with null universe_id`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")

            val result = series_repo.create(schema, author.id, null, "Standalone Series", false)

            assertNull(result.universe_id)
        }


        @Test
        fun `persisted row matches returned row when independently re-fetched`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")

            val created = series_repo.create(schema, author.id, null, "Series", true)
            val fetched = series_repo.findAllByAuthorIdAndUniverseId(schema, author.id, null).first()

            assertEquals(created.id, fetched.id)
            assertEquals(created.author_id, fetched.author_id)
            assertEquals(created.universe_id, fetched.universe_id)
            assertEquals(created.name, fetched.name)
            assertEquals(created.is_finished, fetched.is_finished)
        }


        @Test
        fun `is_finished true is actually persisted, not left as default`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")

            series_repo.create(schema, author.id, null, "Finished Series", true)
            val fetched = series_repo.findAllByAuthorIdAndUniverseId(schema, author.id, null).first()

            assertTrue(fetched.is_finished)
        }


        @Test
        fun `is_finished false is persisted correctly`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")

            series_repo.create(schema, author.id, null, "Unfinished Series", false)
            val fetched = series_repo.findAllByAuthorIdAndUniverseId(schema, author.id, null).first()

            assertFalse(fetched.is_finished)
        }


        @Test
        fun `generates a unique id per series`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")

            val first = series_repo.create(schema, author.id, null, "Series A", false)
            val second = series_repo.create(schema, author.id, null, "Series B", false)

            assertNotEquals(first.id, second.id)
        }


        @Test
        fun `created series does not leak into a different tenant schema`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val author = author_repo.create(schema1, "First", "Last")

            series_repo.create(schema1, author.id, null, "Tenant One Series", false)

            val result_other_tenant = series_repo.findAllByAuthorIdAndUniverseId(schema2, null, null)

            assertEquals(0, result_other_tenant.size)
        }
    }
}
