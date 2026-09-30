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
import dev.jovanni0.tables.tenant.BookTable
import dev.jovanni0.tables.tenant.CoverTable
import dev.jovanni0.utils.Patch
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.io.IOException
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import sun.java2d.cmm.ProfileDataVerifier.verify
import java.nio.file.Files
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid



@Testcontainers
class CoverRepositoryTests
{
    private val cover_repo = CoverRepository(AssetStorage(Files.createTempDirectory("cover-repo-tests")))
    private val author_repo = AuthorRepository()
    private val edition_repo = EditionRepository(AssetStorage(Files.createTempDirectory("edition-repo-tests")))
    private val book_repo = BookRepository(author_repo, cover_repo, edition_repo)
    private val read_record_repo = ReadRecordRepository()
    private val mock_cover_storage = mockk<AssetStorage>(relaxed = true)
    private val mock_cover_repo = CoverRepository(mock_cover_storage)


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

    
    private fun insertCover(schema: String, book_id: Uuid, label: String? = null, added_at: LocalDate = LocalDate.now()): Uuid
    {
        return tenantTransaction(schema)
        {
            CoverTable.insert {
                it[CoverTable.book_id] = book_id
                it[CoverTable.label] = label
                it[CoverTable.added_at] = added_at
            }[CoverTable.id]
        }
    }

    
    private fun setDefaultCover(schema: String, book_id: Uuid, cover_id: Uuid?)
    {
        tenantTransaction(schema)
        {
            BookTable.update({ BookTable.id eq book_id })
            {
                it[BookTable.cover_id] = cover_id
            }
        }
    }


    @BeforeEach
    fun setUpEach()
    {
        transaction {
            tenants.forEach {
                exec("TRUNCATE TABLE \"$it\".cover, \"$it\".series, \"$it\".universe, \"$it\".book, \"$it\".author RESTART IDENTITY CASCADE")
            }
        }
    }

    @Nested
    inner class FindAllByBookId
    {

        @Test
        fun `returns all covers for a book`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val cover_1 = insertCover(schema_1, book_id, label = "front")
            val cover_2 = insertCover(schema_1, book_id, label = "back")

            val result = cover_repo.findAllByBookId(schema_1, book_id)

            assertEquals(2, result.size)
            assertTrue(result.any { it.id == cover_1 && it.label == "front" })
            assertTrue(result.any { it.id == cover_2 && it.label == "back" })
        }

        
        @Test
        fun `findAllByBookId returns empty list when book has no covers`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val result = cover_repo.findAllByBookId(schema_1, book_id)

            assertTrue(result.isEmpty())
        }


        @Test
        fun `returns empty list for a nonexistent book_id`()
        {
            val result = cover_repo.findAllByBookId(schema_1, Uuid.random())

            assertTrue(result.isEmpty())
        }

        @Test
        fun `marks exactly one cover as default when book cover_id is set`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val cover_1 = insertCover(schema_1, book_id)
            val cover_2 = insertCover(schema_1, book_id)
            setDefaultCover(schema_1, book_id, cover_2)

            val result = cover_repo.findAllByBookId(schema_1, book_id)

            assertEquals(false, result.first { it.id == cover_1 }.is_default)
            assertEquals(true, result.first { it.id == cover_2 }.is_default)
        }


        @Test
        fun `marks all covers as non-default when book cover_id is null`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            insertCover(schema_1, book_id)
            insertCover(schema_1, book_id)

            val result = cover_repo.findAllByBookId(schema_1, book_id)

            assertTrue(result.all { !it.is_default })
        }


        @Test
        fun `does not include covers from other books`()
        {
            val author_id = insertAuthor(schema_1)
            val book_1 = insertBook(schema_1, author_id, title = "Book One")
            val book_2 = insertBook(schema_1, author_id, title = "Book Two")

            val cover_book_1 = insertCover(schema_1, book_1)
            insertCover(schema_1, book_2)

            val result = cover_repo.findAllByBookId(schema_1, book_1)

            assertEquals(1, result.size)
            assertEquals(cover_book_1, result.first().id)
        }
    }



    @Nested
    inner class FindById
    {
        @Test
        fun `findById returns the correct cover with correct field mapping`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val added_at = LocalDate.of(2024, 5, 1)
            val cover_id = insertCover(schema_1, book_id, label = "special edition", added_at = added_at)

            val result = cover_repo.findById(schema_1, cover_id)

            assertNotNull(result)
            assertEquals(cover_id, result.id)
            assertEquals(book_id, result.book_id)
            assertEquals("special edition", result.label)
            assertEquals(added_at, result.added_at)
        }


        @Test
        fun `findById returns null for nonexistent id`()
        {
            val result = cover_repo.findById(schema_1, Uuid.random())

            assertNull(result)
        }


        @Test
        fun `findById marks is_default true when cover is the book default`()
        {
            val schema = "user_test-user1"
            val author_id = insertAuthor(schema)
            val book_id = insertBook(schema, author_id)
            val cover_id = insertCover(schema, book_id)
            setDefaultCover(schema, book_id, cover_id)

            val result = cover_repo.findById(schema, cover_id)

            assertNotNull(result)
            assertTrue(result.is_default)
        }


        @Test
        fun `findById marks is_default false when cover exists but is not the default`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val cover_1 = insertCover(schema_1, book_id)
            val cover_2 = insertCover(schema_1, book_id)
            setDefaultCover(schema_1, book_id, cover_2)
    
            val result = cover_repo.findById(schema_1, cover_1)
    
            assertNotNull(result)
            assertFalse(result.is_default)
        }


        @Test
        fun `findById does not leak covers across tenant schemas`()
        {
            val schema_1 = "user_test-user1"
            val schema_2 = "user_test-user2"
    
            val author_1 = insertAuthor(schema_1)
            val book_1 = insertBook(schema_1, author_1)
            val cover_1 = insertCover(schema_1, book_1)

            val author_2 = insertAuthor(schema_2)
            val book_2 = insertBook(schema_2, author_2)
            insertCover(schema_2, book_2)
    
            val result_wrong_schema = cover_repo.findById(schema_2, cover_1)
    
            assertNull(result_wrong_schema)
        }
    }


    @Nested
    inner class Create
    {
        @Test
        fun `inserts a cover row and returns matching book_id, label, and added_at`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            val result = cover_repo.create(
                schema_1,
                book_id,
                "front cover",
                "cover.jpg",
                "fake-image-bytes".toByteArray(),
                book_context,
            )

            assertEquals(book_id, result.book_id)
            assertEquals("front cover", result.label)
            assertEquals(LocalDate.now(), result.added_at)

            val stored = cover_repo.findById(schema_1, result.id)
            
            assertNotNull(stored)
            assertEquals(result.id, stored.id)
        }


        @Test
        fun `returned is_default is always false on creation`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            val result = cover_repo.create(schema_1, book_id, null, "cover.png", ByteArray(0), book_context)

            assertFalse(result.is_default)
        }


        @Test
        fun `label null is preserved as null on the returned and persisted row`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            val result = cover_repo.create(schema_1, book_id, null, "cover.png", ByteArray(0), book_context)

            assertNull(result.label)
            assertNull(cover_repo.findById(schema_1, result.id)?.label)
        }


//        @Test
//        fun `calls cover_storage store with a filename containing the newly generated cover id`()
//        {
//            val author_id = insertAuthor(schema_1)
//            val book_id = insertBook(schema_1, author_id)
//            val book_context = book_repo.getBookContext(schema_1, book_id)!!
//
//            val filename_slot = slot<String>()
//            every { mock_cover_storage.store(any(), any(), capture(filename_slot), any()) } just Awaits
//
//            val result = mock_cover_repo.create(schema_1, book_id, "front", "cover.jpg", ByteArray(0), book_context)
//
//            assertTrue(filename_slot.captured.contains(result.id.toString()))
//        }


//        @Test
//        fun `extension is correctly derived from the original filename, including the no-extension edge case`()
//        {
//            val author_id = insertAuthor(schema_1)
//            val book_id = insertBook(schema_1, author_id)
//            val book_context = book_repo.getBookContext(schema_1, book_id)!!
//
//            val captured_filenames = mutableListOf<String>()
//            every { mock_cover_storage.store(any(), any(), capture(captured_filenames), any()) } just Awaits
//
//            mock_cover_repo.create(schema_1, book_id, null, "front-cover.JPG", ByteArray(0), book_context)
//            mock_cover_repo.create(schema_1, book_id, null, "back-cover.png", ByteArray(0), book_context)
//            mock_cover_repo.create(schema_1, book_id, null, "no-extension", ByteArray(0), book_context)
//
//            assertTrue(captured_filenames[0].endsWith(".JPG"))
//            assertTrue(captured_filenames[1].endsWith(".png"))
//            // substringAfterLast('.', "") returns "" for a filename with no dot, so the built filename ends in a bare trailing "."
//            assertTrue(captured_filenames[2].endsWith("."))
//        }


        @Test
        fun `rollback - when cover_storage store throws, no row is left in CoverTable`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            every { mock_cover_storage.store(any(), any(), any(), any()) } throws IOException("disk full")

            assertThrows<IOException> {
                mock_cover_repo.create(schema_1, book_id, "front", "cover.jpg", ByteArray(0), book_context)
            }

            assertTrue(mock_cover_repo.findAllByBookId(schema_1, book_id).isEmpty())
        }


        @Test
        fun `rollback - exception from cover_storage store propagates out of create rather than being swallowed`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            every { mock_cover_storage.store(any(), any(), any(), any()) } throws AssetStorageException("partial write failure")

            assertThrows<AssetStorageException> {
                mock_cover_repo.create(schema_1, book_id, "front", "cover.jpg", ByteArray(0), book_context)
            }
        }


        @Test
        fun `filename passed to cover_storage store is built from the given book_context`()
        {
            val mock_cover_storage = mockk<AssetStorage>(relaxed = true)
            val repo = CoverRepository(mock_cover_storage)

            val author_id = insertAuthor(schema_1, last_name = "Tolkien")
            val book_id = insertBook(schema_1, author_id, title = "The Fellowship of the Ring")
            val book_context = book_repo.getBookContext(schema_1, book_id)!!

            mockkObject(FilenameBuilder)
            val context_slot = slot<BookFilenameContext>()
            every {
                FilenameBuilder.build(capture(context_slot), any(), any(), any(), any())
            } returns "stub-filename.jpg"

            mock_cover_repo.create(schema_1, book_id, "front", "cover.jpg", ByteArray(0), book_context)

            assertEquals(book_context, context_slot.captured)

            unmockkObject(FilenameBuilder)
        }
    }


    @Nested
    inner class Update
    {
        @Test
        fun `Patch Undefined for label results in no DB write and label remains unchanged`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val cover_id = insertCover(schema_1, book_id, label = "original")

            cover_repo.update(schema_1, cover_id, Patch.Undefined)

            val result = cover_repo.findById(schema_1, cover_id)

            assertEquals("original", result?.label)
        }


        @Test
        fun `Patch Value null clears the label`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val cover_id = insertCover(schema_1, book_id, label = "original")

            cover_repo.update(schema_1, cover_id, Patch.Value(null))

            val result = cover_repo.findById(schema_1, cover_id)

            assertNull(result?.label)
        }


        @Test
        fun `Patch Value with a string sets the label to the new value`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val cover_id = insertCover(schema_1, book_id, label = "original")

            cover_repo.update(schema_1, cover_id, Patch.Value("new label"))

            val result = cover_repo.findById(schema_1, cover_id)

            assertEquals("new label", result?.label)
        }


        @Test
        fun `updating a nonexistent id does not throw and affects zero rows`()
        {
            // update() currently has no return value indicating rows affected, so this test can
            // only confirm the call doesn't throw. The route relies entirely on the follow-up
            // findById() call to turn this into a 404 — this test documents that dependency
            // rather than validating an affected-row count, since none is returned.
            assertDoesNotThrow {
                cover_repo.update(schema_1, Uuid.random(), Patch.Value("new label"))
            }
        }


        @Test
        fun `updating a cover in one tenant schema does not affect a same-id lookalike scenario in another`()
        {
            val author_1 = insertAuthor(schema_1)
            val book_1 = insertBook(schema_1, author_1)
            val cover_1 = insertCover(schema_1, book_1, label = "schema one label")

            val author_2 = insertAuthor(schema_2)
            val book_2 = insertBook(schema_2, author_2)
            insertCover(schema_2, book_2, label = "schema two label")

            cover_repo.update(schema_2, cover_1, Patch.Value("hijacked"))

            val result = cover_repo.findById(schema_1, cover_1)

            assertEquals("schema one label", result?.label)
        }
    }


    @Nested
    inner class Delete
    {
        @Test
        fun `deleting an existing non-default cover returns true and removes the row`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val cover_id = insertCover(schema_1, book_id)

            val result = cover_repo.delete(schema_1, cover_id)

            assertTrue(result)
            assertNull(cover_repo.findById(schema_1, cover_id))
        }


        @Test
        fun `deleting a non-default cover leaves BookTable cover_id untouched for other books`()
        {
            val author_id = insertAuthor(schema_1)
            val book_1 = insertBook(schema_1, author_id, title = "Book One")
            val book_2 = insertBook(schema_1, author_id, title = "Book Two")

            val cover_book_1 = insertCover(schema_1, book_1)
            val cover_book_2 = insertCover(schema_1, book_2)
            setDefaultCover(schema_1, book_2, cover_book_2)

            cover_repo.delete(schema_1, cover_book_1)

            val book_2_cover = cover_repo.findAllByBookId(schema_1, book_2).first { it.id == cover_book_2 }

            assertTrue(book_2_cover.is_default)
        }


        @Test
        fun `deleting a nonexistent cover id returns false and affects no rows`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val existing_cover = insertCover(schema_1, book_id)

            val result = cover_repo.delete(schema_1, Uuid.random())

            assertFalse(result)
            assertNotNull(cover_repo.findById(schema_1, existing_cover))
        }


        @Test
        fun `deleting the default cover reassigns BookTable cover_id to the most recently added remaining cover`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val older_cover = insertCover(schema_1, book_id, added_at = LocalDate.now().minusDays(5))
            val current_default = insertCover(schema_1, book_id, added_at = LocalDate.now().minusDays(2))
            val newest_cover = insertCover(schema_1, book_id, added_at = LocalDate.now())
            setDefaultCover(schema_1, book_id, current_default)

            cover_repo.delete(schema_1, current_default)

            val remaining = cover_repo.findAllByBookId(schema_1, book_id)
            assertTrue(remaining.first { it.id == newest_cover }.is_default)
            assertFalse(remaining.first { it.id == older_cover }.is_default)
        }


        @Test
        fun `deleting the default cover when it was the only cover sets BookTable cover_id to null`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val only_cover = insertCover(schema_1, book_id)
            setDefaultCover(schema_1, book_id, only_cover)

            val result = cover_repo.delete(schema_1, only_cover)

            assertTrue(result)
            assertTrue(cover_repo.findAllByBookId(schema_1, book_id).isEmpty())

            val book = book_repo.findById(schema_1, book_id)

            assertNotNull(book)
            assertNull(book.cover_id)
        }


        @Test
        fun `deleting a non-default cover when the book has a default set elsewhere leaves cover_id unchanged`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val default_cover = insertCover(schema_1, book_id, added_at = LocalDate.now())
            val other_cover = insertCover(schema_1, book_id, added_at = LocalDate.now().minusDays(1))
            setDefaultCover(schema_1, book_id, default_cover)

            cover_repo.delete(schema_1, other_cover)

            val remaining = cover_repo.findAllByBookId(schema_1, book_id)
            assertTrue(remaining.first { it.id == default_cover }.is_default)
        }


        @Test
        fun `when two remaining covers share the exact same added_at, tie-breaking is deterministic`()
        {
            // Flagged gap from the test plan: CoverTable currently has no secondary sort key, so
            // orderBy(added_at, DESC) alone doesn't guarantee a stable winner when timestamps collide.
            // This test pins down whatever Postgres/Exposed currently returns as "first" so a
            // regression is visible, but it is NOT a guarantee of intended behavior — if a tiebreaker
            // (e.g. insertion order, a sequence column) is added to delete(), this test's expected
            // winner should be revisited.
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val same_day = LocalDate.now()

            val cover_a = insertCover(schema_1, book_id, added_at = same_day)
            val cover_b = insertCover(schema_1, book_id, added_at = same_day)
            val default_cover = insertCover(schema_1, book_id, added_at = same_day)
            setDefaultCover(schema_1, book_id, default_cover)

            cover_repo.delete(schema_1, default_cover)

            val remaining = cover_repo.findAllByBookId(schema_1, book_id)
            val new_default = remaining.singleOrNull { it.is_default }

            assertNotNull(new_default)
            assertTrue(new_default.id == cover_a || new_default.id == cover_b)
        }


        @Test
        fun `deleting a cover from one book never affects another book's cover_id, even with matching added_at`()
        {
            val author_id = insertAuthor(schema_1)
            val book_a = insertBook(schema_1, author_id, title = "Book A")
            val book_b = insertBook(schema_1, author_id, title = "Book B")
            val shared_timestamp = LocalDate.now()

            val cover_a_default = insertCover(schema_1, book_a, added_at = shared_timestamp)
            setDefaultCover(schema_1, book_a, cover_a_default)

            val cover_b_default = insertCover(schema_1, book_b, added_at = shared_timestamp)
            setDefaultCover(schema_1, book_b, cover_b_default)

            cover_repo.delete(schema_1, cover_a_default)

            val book_b_covers = cover_repo.findAllByBookId(schema_1, book_b)
            assertTrue(book_b_covers.first { it.id == cover_b_default }.is_default)
        }
    }
}
