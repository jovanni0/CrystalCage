package repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.data_types.BookCreateContext
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.CoverRepository
import dev.jovanni0.repositories.EditionRepository
import dev.jovanni0.repositories.SeriesRepository
import dev.jovanni0.repositories.TagRepository
import dev.jovanni0.utils.Patch
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid



@Testcontainers
class TagRepositoryTests
{
    private val tag_repo = TagRepository()
    private val author_repo = AuthorRepository()
    private val cover_repo = CoverRepository(AssetStorage(Files.createTempDirectory("cover-repo-tests")))
    private val edition_repo = EditionRepository(AssetStorage(Files.createTempDirectory("edition-repo-tests")))
    private val book_repo = BookRepository(author_repo, cover_repo, edition_repo)
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
                exec("TRUNCATE TABLE \"$it\".read_record, \"$it\".book_tag, \"$it\".tag, \"$it\".book, \"$it\".series, \"$it\".universe, \"$it\".author RESTART IDENTITY CASCADE")
            }
        }
    }

    @Nested
    inner class FindAll
    {
        @Test
        fun `returns empty list when no tags exist`()
        {
            val schema = tenants[0]

            val result = tag_repo.findAll(schema)

            assertEquals(0, result.size)
        }


        @Test
        fun `tag with no linked books has book_count 0`()
        {
            val schema = tenants[0]
            tag_repo.create(schema, "Fantasy", null)

            val result = tag_repo.findAll(schema)

            assertEquals(1, result.size)
            assertEquals(0, result.first().book_count)
        }


        @Test
        fun `tag with one linked book has book_count 1`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            val result = tag_repo.findAll(schema)

            assertEquals(1, result.size)
            assertEquals(1, result.first().book_count)
        }


        @Test
        fun `tag with multiple linked books has correct count and no duplicate rows`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book1 = book_repo.create(schema, BookCreateContext(author.id, title =  "Book One"))
            val book2 = book_repo.create(schema, BookCreateContext(author.id, title =  "Book Two"))
            val book3 = book_repo.create(schema, BookCreateContext(author.id, title =  "Book Three"))
            val tag = tag_repo.create(schema, "Fantasy", null)

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book1.id, book2.id, book3.id), emptyList())

            val result = tag_repo.findAll(schema)

            assertEquals(1, result.size, "tag should appear once despite multiple joined books")
            assertEquals(3, result.first().book_count)
        }


        @Test
        fun `multiple tags each report their own independent book_count`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book1 = book_repo.create(schema, BookCreateContext(author.id, title = "Book One"))
            val book2 = book_repo.create(schema, BookCreateContext(author.id, title = "Book Two"))

            val tag_no_books = tag_repo.create(schema, "Empty Tag", null)
            val tag_one_book = tag_repo.create(schema, "One Book Tag", null)
            val tag_two_books = tag_repo.create(schema, "Two Book Tag", null)

            tag_repo.update(schema, tag_one_book.id, Patch.Undefined, Patch.Undefined, listOf(book1.id), emptyList())
            tag_repo.update(schema, tag_two_books.id, Patch.Undefined, Patch.Undefined, listOf(book1.id, book2.id), emptyList())

            val result = tag_repo.findAll(schema)
            val counts = result.associate { it.name to it.book_count }

            assertEquals(3, result.size)
            assertEquals(0, counts["Empty Tag"])
            assertEquals(1, counts["One Book Tag"])
            assertEquals(2, counts["Two Book Tag"])
        }


        @Test
        fun `results are sorted alphabetically by name`()
        {
            val schema = tenants[0]
            tag_repo.create(schema, "Zebra", null)
            tag_repo.create(schema, "Apple", null)
            tag_repo.create(schema, "Mango", null)

            val result = tag_repo.findAll(schema)

            assertEquals(listOf("Apple", "Mango", "Zebra"), result.map { it.name })
        }


        @Test
        fun `does not leak tags across tenant schemas`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]

            tag_repo.create(schema1, "Tenant One Tag", null)
            tag_repo.create(schema2, "Tenant Two Tag", null)

            val result_schema1 = tag_repo.findAll(schema1)
            val result_schema2 = tag_repo.findAll(schema2)

            assertEquals(1, result_schema1.size)
            assertEquals("Tenant One Tag", result_schema1.first().name)
            assertEquals(1, result_schema2.size)
            assertEquals("Tenant Two Tag", result_schema2.first().name)
        }
    }


    @Nested
    inner class FindById
    {
        @Test
        fun `returns null when tag does not exist`()
        {
            val schema = tenants[0]

            val result = tag_repo.findById(schema, Uuid.random())

            assertNull(result)
        }


        @Test
        fun `returns tag with empty books list when it has no linked books`()
        {
            val schema = tenants[0]
            val tag = tag_repo.create(schema, "Fantasy", "Books with magic")

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(tag.id, result.id)
            assertEquals("Fantasy", result.name)
            assertEquals("Books with magic", result.description)
            assertEquals(0, result.books.size)
        }


        @Test
        fun `returns null description correctly`()
        {
            val schema = tenants[0]
            val tag = tag_repo.create(schema, "Fantasy", null)

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertNull(result.description)
        }


        @Test
        fun `tag with one linked book returns that book with correct fields`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(1, result.books.size)

            val book_result = result.books.first()

            assertEquals(book.id, book_result.id)
            assertEquals("Some Book", book_result.title)
            assertNull(book_result.series_id)
            assertNull(book_result.cover_id)
        }


        @Test
        fun `tag with multiple linked books returns all, not duplicated or dropped`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book1 = book_repo.create(schema, BookCreateContext(author.id, title = "Book One"))
            val book2 = book_repo.create(schema, BookCreateContext(author.id, title = "Book Two"))
            val book3 = book_repo.create(schema, BookCreateContext(author.id, title = "Book Three"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book1.id, book2.id, book3.id), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(3, result.books.size)
            assertEquals(setOf("Book One", "Book Two", "Book Three"), result.books.map { it.title }.toSet())
        }


        @Test
        fun `linked book with non-null series_id is reflected correctly`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val series = series_repo.create(schema, author.id, null, "Some Series", false)
            val book = book_repo.create(schema, BookCreateContext(author.id, series_id = series.id, title = "Book In Series"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(series.id, result.books.first().series_id)
        }


        @Test
        fun `tag-level fields come from the tag row, not a joined book row`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Title"))
            val tag = tag_repo.create(schema, "My Tag", "My Description")
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(tag.id, result.id)
            assertEquals("My Tag", result.name)
            assertEquals("My Description", result.description)
        }


        @Test
        fun `does not return books linked to a different tag`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book1 = book_repo.create(schema, BookCreateContext(author.id, title = "Book For Tag One"))
            val book2 = book_repo.create(schema, BookCreateContext(author.id, title = "Book For Tag Two"))

            val tag1 = tag_repo.create(schema, "Tag One", null)
            val tag2 = tag_repo.create(schema, "Tag Two", null)

            tag_repo.update(schema, tag1.id, Patch.Undefined, Patch.Undefined, listOf(book1.id), emptyList())
            tag_repo.update(schema, tag2.id, Patch.Undefined, Patch.Undefined, listOf(book2.id), emptyList())

            val result = tag_repo.findById(schema, tag1.id)

            assertNotNull(result)
            assertEquals(1, result.books.size)
            assertEquals("Book For Tag One", result.books.first().title)
        }


        @Test
        fun `does not leak tags across tenant schemas`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val tag = tag_repo.create(schema1, "Tenant One Tag", null)

            val result = tag_repo.findById(schema2, tag.id)

            assertNull(result)
        }
    }


    @Nested
    inner class Create
    {
        @Test
        fun `creates tag with name and description and persists them`()
        {
            val schema = tenants[0]

            val result = tag_repo.create(schema, "Fantasy", "Books with magic")

            assertEquals("Fantasy", result.name)
            assertEquals("Books with magic", result.description)
        }


        @Test
        fun `creates tag with null description`()
        {
            val schema = tenants[0]

            val result = tag_repo.create(schema, "Fantasy", null)

            assertNull(result.description)
        }


        @Test
        fun `persisted row matches independently re-fetched row`()
        {
            val schema = tenants[0]

            val created = tag_repo.create(schema, "Fantasy", "Books with magic")
            val fetched = tag_repo.findById(schema, created.id)

            assertNotNull(fetched)
            assertEquals(created.id, fetched.id)
            assertEquals(created.name, fetched.name)
            assertEquals(created.description, fetched.description)
        }


        @Test
        fun `generates a unique id per tag`()
        {
            val schema = tenants[0]

            val first = tag_repo.create(schema, "Tag A", null)
            val second = tag_repo.create(schema, "Tag B", null)

            assertNotEquals(first.id, second.id)
        }


        @Test
        fun `newly created tag has book_count 0 when listed`()
        {
            val schema = tenants[0]

            tag_repo.create(schema, "Fantasy", null)
            val result = tag_repo.findAll(schema)

            assertEquals(1, result.size)
            assertEquals(0, result.first().book_count)
        }


        @Test
        fun `created tag does not leak into a different tenant schema`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]

            val created = tag_repo.create(schema1, "Tenant One Tag", null)

            val result_other_tenant = tag_repo.findById(schema2, created.id)

            assertNull(result_other_tenant)
        }


        @Test
        fun `creating a tag with a duplicate name throws`()
        {
            val schema = tenants[0]
            tag_repo.create(schema, "Fantasy", null)

            assertThrows<ExposedSQLException> {
                tag_repo.create(schema, "Fantasy", null)
            }
        }
    }


    @Nested
    inner class Update
    {
        @Test
        fun `updates name only, description untouched`()
        {
            val schema = tenants[0]
            val tag = tag_repo.create(schema, "Old Name", "Original description")

            tag_repo.update(schema, tag.id, Patch.Value("New Name"), Patch.Undefined, emptyList(), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertEquals("New Name", result!!.name)
            assertEquals("Original description", result.description)
        }


        @Test
        fun `updates description only, name untouched`()
        {
            val schema = tenants[0]
            val tag = tag_repo.create(schema, "Original Name", "Old description")

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Value("New description"), emptyList(), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertEquals("Original Name", result!!.name)
            assertEquals("New description", result.description)
        }


        @Test
        fun `sets description to null via Patch Present null`()
        {
            val schema = tenants[0]
            val tag = tag_repo.create(schema, "Fantasy", "Some description")

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Value(null), emptyList(), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNull(result!!.description)
        }


        @Test
        fun `Patch Undefined on both fields leaves name and description untouched`()
        {
            val schema = tenants[0]
            val tag = tag_repo.create(schema, "Fantasy", "Some description")

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, emptyList(), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals("Fantasy", result.name)
            assertEquals("Some description", result.description)
        }


        @Test
        fun `adds books via added_book_ids`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(1, result.books.size)
            assertEquals(book.id, result.books.first().id)
        }


        @Test
        fun `removes books via removed_book_ids`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, emptyList(), listOf(book.id))

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(0, result.books.size)
        }


        @Test
        fun `adds and removes different books in the same call without interference`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book_to_remove = book_repo.create(schema, BookCreateContext(author.id, title = "Book To Remove"))
            val book_to_add = book_repo.create(schema, BookCreateContext(author.id, title = "Book To Add"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book_to_remove.id), emptyList())

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book_to_add.id), listOf(book_to_remove.id))

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(1, result.books.size)
            assertEquals(book_to_add.id, result.books.first().id)
        }


        @Test
        fun `adding an id that is already linked does not throw or duplicate`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(1, result.books.size)
        }


        @Test
        fun `removing an id that is not linked is a no-op, not an error`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)

            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, emptyList(), listOf(book.id))

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals(0, result.books.size)
        }


        @Test
        fun `empty added and removed lists leave existing links untouched`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            tag_repo.update(schema, tag.id, Patch.Value("Fantasy Renamed"), Patch.Undefined, emptyList(), emptyList())

            val result = tag_repo.findById(schema, tag.id)

            assertNotNull(result)
            assertEquals("Fantasy Renamed", result.name)
            assertEquals(1, result.books.size)
        }


        @Test
        fun `updating a non-existent tag id does not throw`()
        {
            val schema = tenants[0]

            assertDoesNotThrow {
                tag_repo.update(schema, Uuid.random(), Patch.Value("Name"), Patch.Undefined, emptyList(), emptyList())
            }
        }


        @Test
        fun `does not affect tags in a different tenant schema`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val tag_schema1 = tag_repo.create(schema1, "Original", null)
            tag_repo.create(schema2, "Original", null)

            tag_repo.update(schema1, tag_schema1.id, Patch.Value("Changed"), Patch.Undefined, emptyList(), emptyList())

            val result_schema1 = tag_repo.findAll(schema1).first()
            val result_schema2 = tag_repo.findAll(schema2).first()

            assertEquals("Changed", result_schema1.name)
            assertEquals("Original", result_schema2.name)
        }
    }


    @Nested
    inner class Delete
    {
        @Test
        fun `deletes existing tag and returns 1`()
        {
            val schema = tenants[0]
            val tag = tag_repo.create(schema, "Fantasy", null)

            val rows_deleted = tag_repo.delete(schema, tag.id)

            assertEquals(1, rows_deleted)
        }


        @Test
        fun `deleted tag is no longer findable`()
        {
            val schema = tenants[0]
            val tag = tag_repo.create(schema, "Fantasy", null)

            tag_repo.delete(schema, tag.id)

            val result = tag_repo.findById(schema, tag.id)
            assertNull(result)
        }


        @Test
        fun `deleting a non-existent tag returns 0 and does not throw`()
        {
            val schema = tenants[0]

            val rows_deleted = tag_repo.delete(schema, Uuid.random())

            assertEquals(0, rows_deleted)
        }


        @Test
        fun `deleting a tag removes its book links`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            tag_repo.delete(schema, tag.id)

            // re-fetch via a second tag linking the same book, to confirm the book itself still exists
            // and only the tag_book link for the deleted tag is gone
            val other_tag = tag_repo.create(schema, "Other Tag", null)
            tag_repo.update(schema, other_tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())
            val other_tag_result = tag_repo.findById(schema, other_tag.id)

            assertNotNull(other_tag_result)
            assertEquals(1, other_tag_result.books.size, "book should still be linkable, confirming it wasn't cascade-deleted")
        }


        @Test
        fun `deleting a tag does not delete the linked book itself`()
        {
            val schema = tenants[0]
            val author = author_repo.create(schema, "First", "Last")
            val book = book_repo.create(schema, BookCreateContext(author.id, title = "Some Book"))
            val tag = tag_repo.create(schema, "Fantasy", null)
            tag_repo.update(schema, tag.id, Patch.Undefined, Patch.Undefined, listOf(book.id), emptyList())

            tag_repo.delete(schema, tag.id)

            val book_result = book_repo.findById(schema, book.id)
            assertNotNull(book_result)
        }


        @Test
        fun `deleting a tag does not affect other tags`()
        {
            val schema = tenants[0]
            val tag1 = tag_repo.create(schema, "Tag One", null)
            val tag2 = tag_repo.create(schema, "Tag Two", null)

            tag_repo.delete(schema, tag1.id)

            val remaining = tag_repo.findAll(schema)
            assertEquals(1, remaining.size)
            assertEquals(tag2.id, remaining.first().id)
        }


        @Test
        fun `does not delete a tag with the same id in a different tenant schema`()
        {
            val schema1 = tenants[0]
            val schema2 = tenants[1]
            val tag_schema1 = tag_repo.create(schema1, "Tenant One Tag", null)
            tag_repo.create(schema2, "Tenant Two Tag", null)

            val rows_deleted = tag_repo.delete(schema2, tag_schema1.id)

            assertEquals(0, rows_deleted)
            assertNotNull(tag_repo.findById(schema1, tag_schema1.id))
        }
    }
}
