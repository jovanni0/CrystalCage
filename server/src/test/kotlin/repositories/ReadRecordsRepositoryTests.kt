package repositories


import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.exceptions.ConfigurationException
import dev.jovanni0.exceptions.ConflictException
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.EditionRepository
import dev.jovanni0.repositories.ReadRecordRepository
import dev.jovanni0.tables.tenant.EditionTable
import dev.jovanni0.utils.Patch
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.nio.file.Files
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid



@Testcontainers
class ReadRecordsRepositoryTests
{
    private val author_repo = AuthorRepository()
    private val book_repo = BookRepository()
    private val edition_repo = EditionRepository(AssetStorage(Files.createTempDirectory("edition-repo-tests")))
    private val read_record_repo = ReadRecordRepository()


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
                exec("TRUNCATE TABLE \"$it\".read_record, \"$it\".book_edition, \"$it\".book, \"$it\".author RESTART IDENTITY CASCADE")
            }
        }
    }


    private fun insertEdition(
        schema: String,
        book_id: Uuid,
        label: String? = null,
        release_date: LocalDate? = null,
        format: String = "hardcover",
        added_at: LocalDate = LocalDate.now(),
    ): Uuid
    {
        return tenantTransaction(schema)
        {
            EditionTable.insert {
                it[EditionTable.book_id] = book_id
                it[EditionTable.label] = label
                it[EditionTable.release_date] = release_date
                it[EditionTable.format] = format
                it[EditionTable.added_at] = added_at
            }[EditionTable.id]
        }
    }


    @Nested
    inner class FindAllByBookId
    {
        @Test
        fun `returns empty list when book has no read records`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            val result = read_record_repo.findAllByBookId(schema, book.id)

            assertEquals(0, result.size)
        }


        @Test
        fun `returns all records for the book with correctly mapped fields`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            val created = read_record_repo.create(
                schema, book.id, null,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10),
                false, false, "page 42", "great read"
            )

            val result = read_record_repo.findAllByBookId(schema, book.id)

            assertEquals(1, result.size)

            val row = result.first()

            assertEquals(created.id, row.id)
            assertEquals(book.id, row.book_id)
            assertNull(row.edition_id)
            assertEquals(LocalDate.of(2024, 1, 1), row.start_date)
            assertEquals(LocalDate.of(2024, 1, 10), row.end_date)
            assertFalse(row.did_not_finish)
            assertFalse(row.is_historical)
            assertEquals("page 42", row.stop_position)
            assertEquals("great read", row.notes)
        }


        @Test
        fun `returns multiple records for the same book`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            read_record_repo.create(schema, book.id, null, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 10), false, false, null, null)
            read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = read_record_repo.findAllByBookId(schema, book.id)

            assertEquals(2, result.size)
        }


        @Test
        fun `does not return records belonging to a different book`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book1 = book_repo.create(schema, author.id, null, null, "Book One")
            val book2 = book_repo.create(schema, author.id, null, null, "Book Two")

            read_record_repo.create(schema, book1.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)
            read_record_repo.create(schema, book2.id, null, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 10), false, false, null, null)

            val result = read_record_repo.findAllByBookId(schema, book1.id)

            assertEquals(1, result.size)
            assertEquals(book1.id, result.first().book_id)
        }


        @Test
        fun `does not leak read records across tenant schemas`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val author1 = author_repo.create(schema1, "First", "Last")
            val book1 = book_repo.create(schema1, author1.id, null, null, "Book One")

            read_record_repo.create(schema1, book1.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val author2 = author_repo.create(schema2, "First", "Last")
            val book2 = book_repo.create(schema2, author2.id, null, null, "Book Two")

            val result_schema2 = read_record_repo.findAllByBookId(schema2, book2.id)

            assertEquals(0, result_schema2.size)
        }
    }


    @Nested
    inner class FindById
    {
        @Test
        fun `returns null for non-existent id`()
        {
            val schema = tenants[0]

            val result = read_record_repo.findById(schema, Uuid.random())

            assertNull(result)
        }


        @Test
        fun `returns correctly mapped record for existing id`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            val created = read_record_repo.create(
                schema, book.id, null,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10),
                true, false, "chapter 5", "abandoned"
            )

            val result = read_record_repo.findById(schema, created.id)

            assertNotNull(result)
            assertEquals(created.id, result.id)
            assertEquals(book.id, result.book_id)
            assertEquals(LocalDate.of(2024, 1, 1), result.start_date)
            assertEquals(LocalDate.of(2024, 1, 10), result.end_date)
            assertTrue(result.did_not_finish)
            assertFalse(result.is_historical)
            assertEquals("chapter 5", result.stop_position)
            assertEquals("abandoned", result.notes)
        }


        @Test
        fun `returns record with a linked edition_id correctly`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val edition_id = insertEdition(schema, book.id, null, null, "paperback")

            val created = read_record_repo.create(schema, book.id, edition_id, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = read_record_repo.findById(schema, created.id)

            assertNotNull(result)
            assertEquals(edition_id, result.edition_id)
        }


        @Test
        fun `returns historical record with null dates correctly`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            val created = read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)

            val result = read_record_repo.findById(schema, created.id)

            assertNotNull(result)
            assertTrue(result.is_historical)
            assertNull(result.start_date)
            assertNull(result.end_date)
        }


        @Test
        fun `does not find a record that belongs to a different tenant schema`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val author = author_repo.create(schema1, "First", "Last")
            val book = book_repo.create(schema1, author.id, null, null, "Some Book")

            val created = read_record_repo.create(schema1, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = read_record_repo.findById(schema2, created.id)

            assertNull(result)
        }
    }


    @Nested
    inner class Create
    {
        @Test
        fun `creates a full record with all fields persisted correctly`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val edition_id = insertEdition(schema, book.id, null, null, "paperback")


            val result = read_record_repo.create(
                schema, book.id, edition_id,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10),
                true, false, "page 100", "some notes"
            )

            assertEquals(book.id, result.book_id)
            assertEquals(edition_id, result.edition_id)
            assertEquals(LocalDate.of(2024, 1, 1), result.start_date)
            assertEquals(LocalDate.of(2024, 1, 10), result.end_date)
            assertTrue(result.did_not_finish)
            assertFalse(result.is_historical)
            assertEquals("page 100", result.stop_position)
            assertEquals("some notes", result.notes)
        }


        @Test
        fun `creates a record with all nullable fields null`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            val result = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), null, false, false, null, null)

            assertNull(result.edition_id)
            assertNull(result.end_date)
            assertNull(result.stop_position)
            assertNull(result.notes)
        }


        @Test
        fun `first historical read for a book succeeds`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            val result = read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)

            assertTrue(result.is_historical)
        }


        @Test
        fun `second historical read for the same book throws ConflictException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)

            assertThrows<ConflictException> {
                read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)
            }
        }


        @Test
        fun `historical read for a different book succeeds independently`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book1 = book_repo.create(schema, author.id, null, null, "Book One")
            val book2 = book_repo.create(schema, author.id, null, null, "Book Two")
            read_record_repo.create(schema, book1.id, null, null, null, false, true, null, null)

            val result = read_record_repo.create(schema, book2.id, null, null, null, false, true, null, null)

            assertTrue(result.is_historical)
        }


        @Test
        fun `historical read with start_date set throws ConflictException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            assertThrows<ConflictException> {
                read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), null, false, true, null, null)
            }
        }


        @Test
        fun `historical read with end_date set throws ConflictException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            assertThrows<ConflictException> {
                read_record_repo.create(schema, book.id, null, null, LocalDate.of(2024, 1, 10), false, true, null, null)
            }
        }


        @Test
        fun `multiple non-historical reads for the same book all succeed`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            read_record_repo.create(schema, book.id, null, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 10), false, false, null, null)
            read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = read_record_repo.findAllByBookId(schema, book.id)
            assertEquals(2, result.size)
        }


        @Test
        fun `returned row matches independently re-fetched row`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")

            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)
            val fetched = read_record_repo.findById(schema, created.id)

            assertNotNull(fetched)
            assertEquals(created, fetched)
        }


        @Test
        fun `created record does not leak into a different tenant schema`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val author = author_repo.create(schema1, "First", "Last")
            val book = book_repo.create(schema1, author.id, null, null, "Some Book")

            val created = read_record_repo.create(schema1, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            assertNull(read_record_repo.findById(schema2, created.id))
        }
    }


    @Nested
    inner class Update
    {
        @Test
        fun `updates a single field, others untouched`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, "old position", "old notes")

            val result = read_record_repo.update(
                schema, created.id,
                Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined,
                Patch.Value("new position"), Patch.Undefined
            )

            assertEquals(1, result)

            val fetched = read_record_repo.findById(schema, created.id)

            assertNotNull(fetched)
            assertEquals("new position", fetched.stop_position)
            assertEquals("old notes", fetched.notes)
            assertEquals(LocalDate.of(2024, 1, 1), fetched.start_date)
        }


        @Test
        fun `Patch Value null clears a nullable field`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, "some position", "some notes")

            read_record_repo.update(
                schema, created.id,
                Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined,
                Patch.Undefined, Patch.Value(null)
            )

            val fetched = read_record_repo.findById(schema, created.id)

            assertNotNull(fetched)
            assertNull(fetched.notes)
        }


        @Test
        fun `all fields Undefined returns null and does not change the row`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, "position", "notes")

            val result = read_record_repo.update(
                schema, created.id,
                Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined
            )

            assertNull(result)

            val fetched = read_record_repo.findById(schema, created.id)

            assertEquals(created, fetched)
        }


        @Test
        fun `updating a non-existent id returns 0`()
        {
            val schema = tenants[0]

            val result = read_record_repo.update(
                schema, Uuid.random(),
                Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined,
                Patch.Value("position"), Patch.Undefined
            )

            assertEquals(0, result)
        }


        @Test
        fun `setting is_historical to true on a book that already has a historical read throws ConflictException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)
            val dated = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            assertThrows<ConflictException> {
                read_record_repo.update(
                    schema, dated.id,
                    Patch.Undefined, Patch.Value(null), Patch.Value(null), Patch.Undefined, Patch.Value(true),
                    Patch.Undefined, Patch.Undefined
                )
            }
        }


        @Test
        fun `setting is_historical to true while dates are still set throws ConflictException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            assertThrows<ConflictException> {
                read_record_repo.update(
                    schema, created.id,
                    Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value(true),
                    Patch.Undefined, Patch.Undefined
                )
            }
        }


        @Test
        fun `setting is_historical to true while clearing both dates succeeds`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = read_record_repo.update(
                schema, created.id,
                Patch.Undefined, Patch.Value(null), Patch.Value(null), Patch.Undefined, Patch.Value(true),
                Patch.Undefined, Patch.Undefined
            )

            assertEquals(1, result)

            val fetched = read_record_repo.findById(schema, created.id)

            assertNotNull(fetched)
            assertTrue(fetched.is_historical)
            assertNull(fetched.start_date)
            assertNull(fetched.end_date)
        }


        @Test
        fun `setting is_historical to false while start_date is null throws ConflictException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)

            assertThrows<ConflictException> {
                read_record_repo.update(
                    schema, created.id,
                    Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value(false),
                    Patch.Undefined, Patch.Undefined
                )
            }
        }


        @Test
        fun `setting is_historical to false while also setting start_date succeeds`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)

            val result = read_record_repo.update(
                schema, created.id,
                Patch.Undefined, Patch.Value(LocalDate.of(2024, 1, 1)), Patch.Undefined, Patch.Undefined, Patch.Value(false),
                Patch.Undefined, Patch.Undefined
            )

            assertEquals(1, result)

            val fetched = read_record_repo.findById(schema, created.id)

            assertNotNull(fetched)
            assertFalse(fetched.is_historical)
            assertEquals(LocalDate.of(2024, 1, 1), fetched.start_date)
        }


        @Test
        fun `patching start_date to non-null on a record that stays historical throws ConflictException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, null, null, false, true, null, null)

            assertThrows<ConflictException> {
                read_record_repo.update(
                    schema, created.id,
                    Patch.Undefined, Patch.Value(LocalDate.of(2024, 1, 1)), Patch.Undefined, Patch.Undefined, Patch.Undefined,
                    Patch.Undefined, Patch.Undefined
                )
            }
        }


        @Test
        fun `patching start_date to null on a non-historical record throws ConflictException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            assertThrows<ConflictException> {
                read_record_repo.update(
                    schema, created.id,
                    Patch.Undefined, Patch.Value(null), Patch.Undefined, Patch.Undefined, Patch.Undefined,
                    Patch.Undefined, Patch.Undefined
                )
            }
        }


        @Test
        fun `setting did_not_finish to null throws ConfigurationException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            assertThrows<ConfigurationException> {
                read_record_repo.update(
                    schema, created.id,
                    Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value(null), Patch.Undefined,
                    Patch.Undefined, Patch.Undefined
                )
            }
        }


        @Test
        fun `setting is_historical to null throws ConfigurationException`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            assertThrows<ConfigurationException> {
                read_record_repo.update(
                    schema, created.id,
                    Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value(null),
                    Patch.Undefined, Patch.Undefined
                )
            }
        }


        @Test
        fun `does not affect records in a different tenant schema`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val author1 = author_repo.create(schema1, "First", "Last")
            val book1 = book_repo.create(schema1, author1.id, null, null, "Book")
            val created1 = read_record_repo.create(schema1, book1.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, "original", null)

            val result = read_record_repo.update(
                schema2, created1.id,
                Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined,
                Patch.Value("hacked"), Patch.Undefined
            )

            assertEquals(0, result)

            val fetched = read_record_repo.findById(schema1, created1.id)

            assertNotNull(fetched)
            assertEquals("original", fetched.stop_position)
        }
    }


    @Nested
    inner class Delete
    {
        @Test
        fun `deletes existing record and returns 1`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = read_record_repo.delete(schema, created.id)

            assertEquals(1, result)
            assertNull(read_record_repo.findById(schema, created.id))
        }


        @Test
        fun `deleting a non-existent id returns 0 and does not throw`()
        {
            val schema = tenants[0]

            val result = read_record_repo.delete(schema, Uuid.random())

            assertEquals(0, result)
        }


        @Test
        fun `does not affect other records for the same book`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, author.id, null, null, "Some Book")
            val record1 = read_record_repo.create(schema, book.id, null, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 10), false, false, null, null)
            val record2 = read_record_repo.create(schema, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            read_record_repo.delete(schema, record1.id)

            val remaining = read_record_repo.findAllByBookId(schema, book.id)

            assertEquals(1, remaining.size)
            assertEquals(record2.id, remaining.first().id)
        }


        @Test
        fun `does not delete a record with the same id in a different tenant schema`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val author = author_repo.create(schema1, "First", "Last")
            val book = book_repo.create(schema1, author.id, null, null, "Some Book")
            val created = read_record_repo.create(schema1, book.id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val result = read_record_repo.delete(schema2, created.id)

            assertEquals(0, result)
            assertNotNull(read_record_repo.findById(schema1, created.id))
        }
    }
}
