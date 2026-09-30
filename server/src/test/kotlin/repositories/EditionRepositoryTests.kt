package repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.data_types.BookCreateContext
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.exceptions.AssetStorageException
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.filesystem.BookFilenameContext
import dev.jovanni0.filesystem.FilenameBuilder
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.CoverRepository
import dev.jovanni0.repositories.EditionRepository
import dev.jovanni0.repositories.ReadRecordRepository
import dev.jovanni0.tables.tenant.EditionTable
import dev.jovanni0.utils.Patch
import io.mockk.mockk
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.nio.file.Files
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.io.IOException
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.nio.file.Path



@Testcontainers
class EditionRepositoryTests
{
    private val author_repo = AuthorRepository()
    private val cover_repo = CoverRepository(AssetStorage(Files.createTempDirectory("cover-repo-tests")))
    private val edition_repo = EditionRepository(AssetStorage(Files.createTempDirectory("edition-repo-tests")))
    private val book_repo = BookRepository(author_repo, cover_repo, edition_repo)


    companion object
    {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16")

        private lateinit var data_source: HikariDataSource
        private val tenants = listOf("user_test-user1", "user_test-user2")
        private val schema_1 = tenants[0]
        private val schema_2 = tenants[1]


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


    private fun insertAuthor(schema: String, last_name: String = "Doe"): Uuid
    {
        return author_repo.create(schema, "Jane", last_name).id
    }


    private fun insertBook(schema: String, author_id: Uuid, title: String = "Test Book"): Uuid
    {
        return book_repo.create(schema, BookCreateContext(author_id, title = title)).id
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


    @BeforeEach
    fun setUpEach()
    {
        transaction {
            tenants.forEach {
                exec("TRUNCATE TABLE \"$it\".book_edition, \"$it\".series, \"$it\".universe, \"$it\".book, \"$it\".author RESTART IDENTITY CASCADE")
            }
        }
    }



    @Nested
    inner class FindById
    {
        @Test
        fun `returns the correct EditionRow with correct field mapping for an existing edition`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val release_date = LocalDate.of(2023, 6, 15)
            val added_at = LocalDate.of(2024, 1, 1)
            val edition_id = insertEdition(
                schema_1,
                book_id,
                label = "collector's edition",
                release_date = release_date,
                format = "hardcover",
                added_at = added_at,
            )

            val result = edition_repo.findById(schema_1, edition_id)

            assertNotNull(result)
            assertEquals(edition_id, result.id)
            assertEquals(book_id, result.book_id)
            assertEquals("collector's edition", result.label)
            assertEquals(release_date, result.release_date)
            assertEquals("hardcover", result.format)
            assertEquals(added_at, result.added_at)
        }


        @Test
        fun `returns null for a nonexistent id`()
        {
            val result = edition_repo.findById(schema_1, Uuid.random())

            assertNull(result)
        }


        @Test
        fun `returns null for an id belonging to a different tenant schema`()
        {
            val author_1 = insertAuthor(schema_1)
            val book_1 = insertBook(schema_1, author_1)
            val edition_1 = insertEdition(schema_1, book_1)

            val author_2 = insertAuthor(schema_2)
            val book_2 = insertBook(schema_2, author_2)
            insertEdition(schema_2, book_2)

            val result_wrong_schema = edition_repo.findById(schema_2, edition_1)

            assertNull(result_wrong_schema)
        }
    }



    @Nested
    inner class FindAllByBookId
    {
        @Test
        fun `returns all editions for a book, correctly mapped`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val edition_1 = insertEdition(schema_1, book_id, label = "paperback", format = "paperback")
            val edition_2 = insertEdition(schema_1, book_id, label = "ebook", format = "ebook")

            val result = edition_repo.findAllByBookId(schema_1, book_id)

            assertEquals(2, result.size)
            assertTrue(result.any { it.id == edition_1 && it.label == "paperback" && it.format == "paperback" })
            assertTrue(result.any { it.id == edition_2 && it.label == "ebook" && it.format == "ebook" })
        }


        @Test
        fun `returns empty list when book has no editions`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val result = edition_repo.findAllByBookId(schema_1, book_id)

            assertTrue(result.isEmpty())
        }


        @Test
        fun `returns empty list for a nonexistent book_id`()
        {
            val result = edition_repo.findAllByBookId(schema_1, Uuid.random())

            assertTrue(result.isEmpty())
        }


        @Test
        fun `does not include editions from other books`()
        {
            val author_id = insertAuthor(schema_1)
            val book_1 = insertBook(schema_1, author_id, title = "Book One")
            val book_2 = insertBook(schema_1, author_id, title = "Book Two")

            val edition_book_1 = insertEdition(schema_1, book_1)
            insertEdition(schema_1, book_2)

            val result = edition_repo.findAllByBookId(schema_1, book_1)

            assertEquals(1, result.size)
            assertEquals(edition_book_1, result.first().id)
        }


        @Test
        fun `does not leak editions across tenant schemas`()
        {
            val author_1 = insertAuthor(schema_1)
            val book_1 = insertBook(schema_1, author_1)
            insertEdition(schema_1, book_1)

            val author_2 = insertAuthor(schema_2)
            val book_2 = insertBook(schema_2, author_2)
            val edition_2 = insertEdition(schema_2, book_2)

            val result = edition_repo.findAllByBookId(schema_2, book_2)

            assertEquals(1, result.size)
            assertEquals(edition_2, result.first().id)
        }


        @Test
        fun `returns editions ordered ascending by added_at`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val newest = insertEdition(schema_1, book_id, label = "newest", added_at = LocalDate.now())
            val oldest = insertEdition(schema_1, book_id, label = "oldest", added_at = LocalDate.now().minusDays(5))
            val middle = insertEdition(schema_1, book_id, label = "middle", added_at = LocalDate.now().minusDays(2))

            val result = edition_repo.findAllByBookId(schema_1, book_id)

            assertEquals(listOf(oldest, middle, newest), result.map { it.id })
        }
    }



    @Nested
    inner class Create
    {
        @Test
        fun `inserts an edition row and returns matching book_id, label, release_date, format`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!
            val release_date = LocalDate.of(2023, 6, 15)

            val result = edition_repo.create(
                schema_1,
                book_id,
                "collector's edition",
                release_date,
                "hardcover",
                "edition.epub",
                "fake-file-bytes".toByteArray(),
                book_context,
            )

            assertEquals(book_id, result.book_id)
            assertEquals("collector's edition", result.label)
            assertEquals(release_date, result.release_date)
            assertEquals("hardcover", result.format)

            val stored = edition_repo.findById(schema_1, result.id)

            assertNotNull(stored)
            assertEquals(result.id, stored.id)
        }


        @Test
        fun `label and release_date can be null and are returned as null`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            val result = edition_repo.create(
                schema_1,
                book_id,
                null,
                null,
                "ebook",
                "edition.epub",
                ByteArray(0),
                book_context,
            )

            assertNull(result.label)
            assertNull(result.release_date)

            val stored = edition_repo.findById(schema_1, result.id)

            assertNull(stored?.label)
            assertNull(stored?.release_date)
        }


        @Test
        fun `added_at is set to the current date on creation`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            val result = edition_repo.create(schema_1, book_id, null, null, "ebook", "edition.epub", ByteArray(0), book_context)

            assertEquals(LocalDate.now(), result.added_at)
        }


        @Test
        fun `calls edition_storage store with a filename containing the newly generated edition id`()
        {
            val fake_storage = mockk<AssetStorage>(relaxed = true)
            val repo = EditionRepository(fake_storage)

            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            val filename_slot = slot<String>()
            every { fake_storage.store(any(), any(), capture(filename_slot), any()) } returns Path.of("/tmp/fake-path")

            val result = repo.create(schema_1, book_id, null, null, "ebook", "edition.epub", ByteArray(0), book_context)

            assertTrue(filename_slot.captured.contains(result.id.toString()))
        }


        @Test
        fun `extension is correctly derived from the original filename, including the no-extension edge case`()
        {
            val fake_storage = mockk<AssetStorage>(relaxed = true)
            val repo = EditionRepository(fake_storage)

            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            val captured_filenames = mutableListOf<String>()
            every { fake_storage.store(any(), any(), capture(captured_filenames), any()) } returns Path.of("/tmp/fake-path")

            repo.create(schema_1, book_id, null, null, "ebook", "book.EPUB", ByteArray(0), book_context)
            repo.create(schema_1, book_id, null, null, "pdf", "book.pdf", ByteArray(0), book_context)
            repo.create(schema_1, book_id, null, null, "unknown", "no-extension", ByteArray(0), book_context)

            assertTrue(captured_filenames[0].endsWith(".EPUB"))
            assertTrue(captured_filenames[1].endsWith(".pdf"))
            // substringAfterLast('.', "") returns "" for a filename with no dot, so the built
            // filename ends in a bare trailing "." — same behavior as CoverRepository.create.
            assertTrue(captured_filenames[2].endsWith("."))
        }


        @Test
        fun `rollback - when edition_storage store throws, no row is left in EditionTable`()
        {
            val fake_storage = mockk<AssetStorage>()
            val repo = EditionRepository(fake_storage)

            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            every { fake_storage.store(any(), any(), any(), any()) } throws IOException("disk full")

            assertThrows<IOException> {
                repo.create(schema_1, book_id, null, null, "hardcover", "edition.epub", ByteArray(0), book_context)
            }

            assertTrue(repo.findAllByBookId(schema_1, book_id).isEmpty())
        }


        @Test
        fun `rollback - exception from edition_storage store propagates out of create rather than being swallowed`()
        {
            val fake_storage = mockk<AssetStorage>()
            val repo = EditionRepository(fake_storage)

            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            every { fake_storage.store(any(), any(), any(), any()) } throws AssetStorageException("partial write failure")

            assertThrows<AssetStorageException> {
                repo.create(schema_1, book_id, null, null, "hardcover", "edition.epub", ByteArray(0), book_context)
            }
        }


        @Test
        fun `filename passed to edition_storage store is built from the given book_context`()
        {
            val fake_storage = mockk<AssetStorage>(relaxed = true)
            val repo = EditionRepository(fake_storage)

            val author_id = insertAuthor(schema_1, last_name = "Tolkien")
            val book_id = insertBook(schema_1, author_id, title = "The Fellowship of the Ring")
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            mockkObject(FilenameBuilder)
            val context_slot = slot<BookFilenameContext>()
            every {
                FilenameBuilder.build(capture(context_slot), any(), any(), any(), any())
            } returns "stub-filename.epub"

            repo.create(schema_1, book_id, null, null, "hardcover", "edition.epub", ByteArray(0), book_context)

            assertEquals(book_context, context_slot.captured)

            unmockkObject(FilenameBuilder)
        }


        @Test
        fun `format is persisted exactly as given, with no trimming or casing changes`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            val result = edition_repo.create(schema_1, book_id, null, null, "  Special-Format_A1  ", "edition.epub", ByteArray(0), book_context)

            assertEquals("  Special-Format_A1  ", result.format)
            assertEquals("  Special-Format_A1  ", edition_repo.findById(schema_1, result.id)?.format)
        }
    }


    @Nested
    inner class Update
    {
        @Test
        fun `updates label only, leaving release_date and format untouched`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val release_date = LocalDate.of(2023, 1, 1)
            val edition_id = insertEdition(schema_1, book_id, label = "old label", release_date = release_date, format = "hardcover")

            val result = edition_repo.update(schema_1, edition_id, Patch.Value("new label"), Patch.Undefined, Patch.Undefined)

            assertNotNull(result)
            assertEquals("new label", result.label)
            assertEquals(release_date, result.release_date)
            assertEquals("hardcover", result.format)
        }


        @Test
        fun `updates release_date only, leaving label and format untouched`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val new_date = LocalDate.of(2024, 3, 10)
            val edition_id = insertEdition(schema_1, book_id, label = "label", release_date = LocalDate.of(2020, 1, 1), format = "hardcover")

            val result = edition_repo.update(schema_1, edition_id, Patch.Undefined, Patch.Value(new_date), Patch.Undefined)

            assertNotNull(result)
            assertEquals("label", result.label)
            assertEquals(new_date, result.release_date)
            assertEquals("hardcover", result.format)
        }


        @Test
        fun `updates format only, leaving label and release_date untouched`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val release_date = LocalDate.of(2023, 1, 1)
            val edition_id = insertEdition(schema_1, book_id, label = "label", release_date = release_date, format = "hardcover")

            val result = edition_repo.update(schema_1, edition_id, Patch.Undefined, Patch.Undefined, Patch.Value("paperback"))

            assertNotNull(result)
            assertEquals("label", result.label)
            assertEquals(release_date, result.release_date)
            assertEquals("paperback", result.format)
        }


        @Test
        fun `Patch Value null clears label`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id, label = "label")

            val result = edition_repo.update(schema_1, edition_id, Patch.Value(null), Patch.Undefined, Patch.Undefined)

            assertNotNull(result)
            assertNull(result.label)
        }


        @Test
        fun `Patch Value null clears release_date`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id, release_date = LocalDate.of(2020, 1, 1))

            val result = edition_repo.update(schema_1, edition_id, Patch.Undefined, Patch.Value(null), Patch.Undefined)

            assertNotNull(result)
            assertNull(result.release_date)
        }


        @Test
        fun `returns the updated row reflecting all changes made in the same call`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id, label = "old", release_date = LocalDate.of(2020, 1, 1), format = "hardcover")

            val new_date = LocalDate.of(2025, 5, 5)
            val result = edition_repo.update(schema_1, edition_id, Patch.Value("new"), Patch.Value(new_date), Patch.Value("ebook"))

            assertNotNull(result)
            assertEquals("new", result.label)
            assertEquals(new_date, result.release_date)
            assertEquals("ebook", result.format)
        }


        @Test
        fun `returns null when the target id does not exist`()
        {
            val result = edition_repo.update(schema_1, Uuid.random(), Patch.Value("new label"), Patch.Undefined, Patch.Undefined)

            assertNull(result)
        }


        @Test
        fun `guard clause - format Patch Value null returns null without touching the row`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id, label = "unchanged", format = "hardcover")

            val result = edition_repo.update(schema_1, edition_id, Patch.Value("attempted change"), Patch.Undefined, Patch.Value(null))

            assertNull(result)

            val unchanged = edition_repo.findById(schema_1, edition_id)
            assertEquals("unchanged", unchanged?.label)
            assertEquals("hardcover", unchanged?.format)
        }


        @Test
        fun `guard clause - all three Patch Undefined returns null`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id)

            val result = edition_repo.update(schema_1, edition_id, Patch.Undefined, Patch.Undefined, Patch.Undefined)

            assertNull(result)
        }


        @Test
        fun `updating a nonexistent id with a fully valid patch returns null, not an exception`()
        {
            assertDoesNotThrow {
                val result = edition_repo.update(schema_1, Uuid.random(), Patch.Value("label"), Patch.Value(LocalDate.now()), Patch.Value("ebook"))
                assertNull(result)
            }
        }
    }


    @Nested
    inner class Delete
    {
        private val read_record_repo = ReadRecordRepository()


        @Test
        fun `deleting an existing edition returns true and the row is gone`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id)

            val result = edition_repo.delete(schema_1, edition_id)

            assertTrue(result)
            assertNull(edition_repo.findById(schema_1, edition_id))
        }


        @Test
        fun `deleting a nonexistent edition returns false`()
        {
            val result = edition_repo.delete(schema_1, Uuid.random())

            assertFalse(result)
        }


        @Test
        fun `deleting an edition cascades to delete its read records`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id)

            val read_record_1 = read_record_repo.create(
                schema_1, book_id, edition_id, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 20), false, false, null, null,
            ).id
            val read_record_2 = read_record_repo.create(
                schema_1, book_id, edition_id, LocalDate.of(2022, 5, 1), LocalDate.of(2022, 5, 15), false, false, null, null,
            ).id

            edition_repo.delete(schema_1, edition_id)

            assertNull(read_record_repo.findById(schema_1, read_record_1))
            assertNull(read_record_repo.findById(schema_1, read_record_2))
        }


        @Test
        fun `deleting an edition does not affect read records belonging to other editions`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_to_delete = insertEdition(schema_1, book_id)
            val other_edition = insertEdition(schema_1, book_id)

            read_record_repo.create(
                schema_1, book_id, edition_to_delete, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 20), false, false, null, null,
            )
            val other_read_record = read_record_repo.create(
                schema_1, book_id, other_edition, LocalDate.of(2022, 5, 1), LocalDate.of(2022, 5, 15), false, false, null, null,
            ).id

            edition_repo.delete(schema_1, edition_to_delete)

            assertNotNull(read_record_repo.findById(schema_1, other_read_record))
        }


        @Test
        fun `deleting an edition with associated read records does not throw`()
        {
            // Sanity check that ON DELETE CASCADE is actually wired end-to-end at the DB level,
            // not just declared in the migration — an unwired FK would surface here as a
            // constraint-violation exception rather than a clean delete.
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id)

            read_record_repo.create(
                schema_1, book_id, edition_id, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 20), false, false, null, null,
            )

            assertDoesNotThrow {
                edition_repo.delete(schema_1, edition_id)
            }
        }
    }


    @Nested
    inner class Exists
    {
        @Test
        fun `returns true for an existing id`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val edition_id = insertEdition(schema_1, book_id)

            assertTrue(edition_repo.exists(schema_1, edition_id))
        }


        @Test
        fun `returns false for a nonexistent id`()
        {
            assertFalse(edition_repo.exists(schema_1, Uuid.random()))
        }


        @Test
        fun `returns false for an id that exists only in a different tenant schema`()
        {
            val author_1 = insertAuthor(schema_1)
            val book_1 = insertBook(schema_1, author_1)
            val edition_1 = insertEdition(schema_1, book_1)

            assertFalse(edition_repo.exists(schema_2, edition_1))
        }
    }
}
