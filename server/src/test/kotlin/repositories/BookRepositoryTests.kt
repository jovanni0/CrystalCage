package repositories

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.data_types.BookCreateContext
import dev.jovanni0.data_types.CoverCreateContext
import dev.jovanni0.data_types.EditionCreateContext
import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.exceptions.AssetStorageException
import dev.jovanni0.exceptions.AuthorMismatchException
import dev.jovanni0.exceptions.not_found.AuthorNotFoundException
import dev.jovanni0.exceptions.not_found.BookNotFoundException
import dev.jovanni0.exceptions.not_found.CoverNotFoundException
import dev.jovanni0.exceptions.not_found.SeriesNotFoundException
import dev.jovanni0.exceptions.not_found.UniverseNotFoundException
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.CoverRepository
import dev.jovanni0.repositories.EditionRepository
import dev.jovanni0.tables.tenant.BookTagTable
import dev.jovanni0.tables.tenant.CoverTable
import dev.jovanni0.tables.tenant.EditionTable
import dev.jovanni0.tables.tenant.ReadRecordTable
import dev.jovanni0.tables.tenant.SeriesTable
import dev.jovanni0.tables.tenant.TagTable
import dev.jovanni0.tables.tenant.UniverseTable
import dev.jovanni0.utils.Update
import io.mockk.every
import io.mockk.mockk
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
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
import kotlin.test.assertTrue
import kotlin.uuid.Uuid



@Testcontainers
class BookRepositoryTests
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


    private fun insertAuthor(schema: String, first_name: String? = "Jane", last_name: String = "Doe"): Uuid
    {
        return author_repo.create(schema, first_name, last_name).id
    }


    private fun insertUniverse(schema: String, author_id: Uuid, name: String = "Test Universe"): Uuid
    {
        return tenantTransaction(schema)
        {
            UniverseTable.insert {
                it[UniverseTable.author_id] = author_id
                it[UniverseTable.name] = name
            }[UniverseTable.id]
        }
    }


    private fun insertSeries(
        schema: String,
        author_id: Uuid,
        universe_id: Uuid? = null,
        name: String = "Test Series",
        is_finished: Boolean = false,
    ): Uuid
    {
        return tenantTransaction(schema)
        {
            SeriesTable.insert {
                it[SeriesTable.author_id] = author_id
                it[SeriesTable.universe_id] = universe_id
                it[SeriesTable.name] = name
                it[SeriesTable.is_finished] = is_finished
            }[SeriesTable.id]
        }
    }


    private fun insertBook(
        schema: String,
        author_id: Uuid,
        universe_id: Uuid? = null,
        series_id: Uuid? = null,
        title: String = "Test Book",
        volume_number: Float? = null,
    ): Uuid
    {
        return book_repo.create(
            schema,
            BookCreateContext(
                author_id = author_id,
                universe_id = universe_id,
                series_id = series_id,
                title = title,
                volume_number = volume_number,
            )
        ).id
    }


    private fun insertTag(schema: String, name: String = "fantasy", description: String? = null): Uuid
    {
        return tenantTransaction(schema)
        {
            TagTable.insert {
                it[TagTable.name] = name
                it[TagTable.description] = description
            }[TagTable.id]
        }
    }


    private fun linkBookTag(schema: String, book_id: Uuid, tag_id: Uuid)
    {
        tenantTransaction(schema)
        {
            BookTagTable.insert {
                it[BookTagTable.book_id] = book_id
                it[BookTagTable.tag_id] = tag_id
            }
        }
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


    private fun insertReadRecord(
        schema: String,
        book_id: Uuid,
        edition_id: Uuid? = null,
        start_date: LocalDate? = null,
        end_date: LocalDate? = null,
        did_not_finish: Boolean = false,
        is_historical: Boolean = false,
    ): Uuid
    {
        return tenantTransaction(schema)
        {
            ReadRecordTable.insert {
                it[ReadRecordTable.book_id] = book_id
                it[ReadRecordTable.edition_id] = edition_id
                it[ReadRecordTable.start_date] = start_date
                it[ReadRecordTable.end_date] = end_date
                it[ReadRecordTable.did_not_finish] = did_not_finish
                it[ReadRecordTable.is_historical] = is_historical
            }[ReadRecordTable.id]
        }
    }


    @BeforeEach
    fun setUpEach()
    {
        transaction {
            tenants.forEach {
                exec(
                    "TRUNCATE TABLE \"$it\".read_record, \"$it\".book_tag, \"$it\".cover, \"$it\".book_edition, " +
                            "\"$it\".tag, \"$it\".book, \"$it\".series, \"$it\".universe, \"$it\".author RESTART IDENTITY CASCADE"
                )
            }
        }
    }


    @Nested
    inner class FindAllBy
    {
        @Test
        fun `returns all books when no filters are given`()
        {
            val author_id = insertAuthor(schema_1)
            insertBook(schema_1, author_id, title = "Book A")
            insertBook(schema_1, author_id, title = "Book B")

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(2, result.size)
        }


        @Test
        fun `filters by author_id`()
        {
            val author_1 = insertAuthor(schema_1, last_name = "Alpha")
            val author_2 = insertAuthor(schema_1, last_name = "Beta")
            insertBook(schema_1, author_1, title = "Book A")
            insertBook(schema_1, author_2, title = "Book B")

            val result = book_repo.findAllBy(schema_1, author_1, null, null, null)

            assertEquals(1, result.size)
            assertEquals("Book A", result.first().title)
        }


        @Test
        fun `filters by universe_id`()
        {
            val author_id = insertAuthor(schema_1)
            val universe_id = insertUniverse(schema_1, author_id)
            insertBook(schema_1, author_id, universe_id = universe_id, title = "In Universe")
            insertBook(schema_1, author_id, title = "No Universe")

            val result = book_repo.findAllBy(schema_1, null, universe_id, null, null)

            assertEquals(1, result.size)
            assertEquals("In Universe", result.first().title)
        }


        @Test
        fun `filters by series_id`()
        {
            val author_id = insertAuthor(schema_1)
            val series_id = insertSeries(schema_1, author_id)
            insertBook(schema_1, author_id, series_id = series_id, title = "In Series")
            insertBook(schema_1, author_id, title = "No Series")

            val result = book_repo.findAllBy(schema_1, null, null, series_id, null)

            assertEquals(1, result.size)
            assertEquals("In Series", result.first().title)
        }


        @Test
        fun `filters by tag_id and excludes books without the tag`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_id = insertTag(schema_1, "tagged")
            val tagged_book_id = insertBook(schema_1, author_id, title = "Tagged Book")
            insertBook(schema_1, author_id, title = "Untagged Book")
            linkBookTag(schema_1, tagged_book_id, tag_id)

            val result = book_repo.findAllBy(schema_1, null, null, null, tag_id)

            assertEquals(1, result.size)
            assertEquals("Tagged Book", result.first().title)
        }


        @Test
        fun `combines multiple filters`()
        {
            val author_id = insertAuthor(schema_1)
            val series_id = insertSeries(schema_1, author_id)
            insertBook(schema_1, author_id, series_id = series_id, title = "Match")
            insertBook(schema_1, author_id, title = "No Series")

            val result = book_repo.findAllBy(schema_1, author_id, null, series_id, null)

            assertEquals(1, result.size)
            assertEquals("Match", result.first().title)
        }


        @Test
        fun `returns empty list when no books match filters`()
        {
            val author_id = insertAuthor(schema_1)
            insertBook(schema_1, author_id)

            val result = book_repo.findAllBy(schema_1, Uuid.random(), null, null, null)

            assertTrue(result.isEmpty())
        }


        @Test
        fun `orders results by author last_name, first_name, universe, series, volume_number, title`()
        {
            val author_z = insertAuthor(schema_1, first_name = "Zoe", last_name = "Zeta")
            val author_a = insertAuthor(schema_1, first_name = "Amy", last_name = "Alpha")
            insertBook(schema_1, author_z, title = "Z Book")
            insertBook(schema_1, author_a, title = "A Book")

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals("A Book", result[0].title)
            assertEquals("Z Book", result[1].title)
        }


        @Test
        fun `does not leak data across tenant schemas`()
        {
            val author_schema_1 = insertAuthor(schema_1)
            val author_schema_2 = insertAuthor(schema_2)
            insertBook(schema_1, author_schema_1, title = "Tenant 1 Book")
            insertBook(schema_2, author_schema_2, title = "Tenant 2 Book")

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(1, result.size)
            assertEquals("Tenant 1 Book", result.first().title)
        }


        @Test
        fun `computes UNREAD status when there are no read records`()
        {
            val author_id = insertAuthor(schema_1)
            insertBook(schema_1, author_id)

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(ReadStatus.UNREAD, result.first().read_status)
            assertEquals(0, result.first().reread_count)
        }


        @Test
        fun `computes IN_PROGRESS status for a single open record`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            insertReadRecord(schema_1, book_id, start_date = LocalDate.now())

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(ReadStatus.IN_PROGRESS, result.first().read_status)
            assertEquals(0, result.first().reread_count)
        }


        @Test
        fun `computes IN_PROGRESS with reread_count when open record coexists with completed records`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            insertReadRecord(schema_1, book_id, start_date = LocalDate.now(), end_date = null)
            insertReadRecord(schema_1, book_id, start_date = LocalDate.of(2023, 1, 1), end_date = LocalDate.of(2023, 2, 1))

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(ReadStatus.IN_PROGRESS, result.first().read_status)
            assertEquals(1, result.first().reread_count)
        }


        @Test
        fun `computes READ status with correct reread_count for multiple completed records`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            insertReadRecord(schema_1, book_id, start_date = LocalDate.of(2023, 1, 1), end_date = LocalDate.of(2023, 2, 1))
            insertReadRecord(schema_1, book_id, start_date = LocalDate.of(2024, 1, 1), end_date = LocalDate.of(2024, 2, 1))

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(ReadStatus.READ, result.first().read_status)
            assertEquals(2, result.first().reread_count)
        }


        @Test
        fun `computes READ status when a completed reread coexists with a DNF record`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            insertReadRecord(schema_1, book_id, start_date = LocalDate.of(2023, 1, 1), end_date = LocalDate.of(2023, 2, 1), did_not_finish = false)
            insertReadRecord(schema_1, book_id, start_date = LocalDate.of(2024, 1, 1), end_date = LocalDate.of(2024, 1, 10), did_not_finish = true)

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(ReadStatus.READ, result.first().read_status)
            assertEquals(1, result.first().reread_count)
        }


        @Test
        fun `computes DNF status when only a DNF record exists`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            insertReadRecord(schema_1, book_id, start_date = LocalDate.of(2023, 1, 1), end_date = LocalDate.of(2023, 1, 10), did_not_finish = true)

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(ReadStatus.DNF, result.first().read_status)
        }


        @Test
        fun `computes READ status with count 1 for a historical-only record`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            insertReadRecord(schema_1, book_id, is_historical = true)

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(ReadStatus.READ, result.first().read_status)
            assertEquals(1, result.first().reread_count)
        }


        @Test
        fun `computes DNF status for a historical DNF record with no other dated records`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            insertReadRecord(schema_1, book_id, is_historical = true, did_not_finish = true)

            val result = book_repo.findAllBy(schema_1, null, null, null, null)

            assertEquals(ReadStatus.DNF, result.first().read_status)
        }
    }


    @Nested
    inner class FindById
    {
        @Test
        fun `returns full detail row with correct field mapping`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_id = insertTag(schema_1)
            val book_id = insertBook(schema_1, author_id, title = "Detail Book")
            linkBookTag(schema_1, book_id, tag_id)

            val result = book_repo.findById(schema_1, book_id)

            assertNotNull(result)
            assertEquals(book_id, result.id)
            assertEquals(author_id, result.author_id)
            assertEquals("Detail Book", result.title)
            assertEquals(listOf(tag_id), result.tag_ids)
        }


        @Test
        fun `returns null for a nonexistent id`()
        {
            val result = book_repo.findById(schema_1, Uuid.random())

            assertNull(result)
        }


        @Test
        fun `only aggregates read records belonging to the requested book`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id_1 = insertBook(schema_1, author_id, title = "Book 1")
            val book_id_2 = insertBook(schema_1, author_id, title = "Book 2")
            insertReadRecord(schema_1, book_id_1, start_date = LocalDate.of(2023, 1, 1), end_date = LocalDate.of(2023, 2, 1))
            insertReadRecord(schema_1, book_id_2, start_date = LocalDate.of(2024, 1, 1), end_date = LocalDate.of(2024, 2, 1))
            insertReadRecord(schema_1, book_id_2, start_date = LocalDate.of(2024, 3, 1), end_date = LocalDate.of(2024, 4, 1))

            val result = book_repo.findById(schema_1, book_id_1)

            assertNotNull(result)
            assertEquals(1, result.reread_count)
        }


        @Test
        fun `does not return a book belonging to another tenant`()
        {
            val author_id = insertAuthor(schema_2)
            val book_id = insertBook(schema_2, author_id)

            val result = book_repo.findById(schema_1, book_id)

            assertNull(result)
        }
    }


    @Nested
    inner class Create
    {
        @Test
        fun `creates a book with only required fields`()
        {
            val author_id = insertAuthor(schema_1)

            val result = book_repo.create(schema_1, BookCreateContext(author_id = author_id, title = "Minimal Book"))

            assertEquals("Minimal Book", result.title)
            assertEquals(ReadStatus.UNREAD, result.read_status)
            assertNull(result.cover_id)
        }


        @Test
        fun `creates a book with all optional scalar fields populated`()
        {
            val author_id = insertAuthor(schema_1)
            val universe_id = insertUniverse(schema_1, author_id)
            val series_id = insertSeries(schema_1, author_id, universe_id)

            val result = book_repo.create(
                schema_1,
                BookCreateContext(
                    author_id = author_id,
                    universe_id = universe_id,
                    series_id = series_id,
                    volume_number = 1.5f,
                    title = "Full Book",
                    launch_date = LocalDate.of(2020, 1, 1),
                    word_count = 90000,
                    blurb = "a blurb",
                    notes = "some notes",
                )
            )

            val fetched = book_repo.findById(schema_1, result.id)

            assertNotNull(fetched)
            assertEquals(universe_id, fetched.universe_id)
            assertEquals(series_id, fetched.series_id)
            assertEquals(1.5f, fetched.volume_number)
            assertEquals(LocalDate.of(2020, 1, 1), fetched.launch_date)
            assertEquals(90000, fetched.word_count)
            assertEquals("a blurb", fetched.blurb)
            assertEquals("some notes", fetched.notes)
        }


        @Test
        fun `creates BookTag rows for provided tag_ids`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_1 = insertTag(schema_1, "tag-one")
            val tag_2 = insertTag(schema_1, "tag-two")

            val result = book_repo.create(
                schema_1,
                BookCreateContext(author_id = author_id, title = "Tagged Book", tag_ids = listOf(tag_1, tag_2))
            )

            val fetched = book_repo.findById(schema_1, result.id)

            assertNotNull(fetched)
            assertEquals(setOf(tag_1, tag_2), fetched.tag_ids.toSet())
        }


        @Test
        fun `deduplicates repeated tag_ids instead of throwing`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_id = insertTag(schema_1)

            val result = book_repo.create(
                schema_1,
                BookCreateContext(author_id = author_id, title = "Dup Tags", tag_ids = listOf(tag_id, tag_id))
            )

            val fetched = book_repo.findById(schema_1, result.id)

            assertNotNull(fetched)
            assertEquals(listOf(tag_id), fetched.tag_ids)
        }


        @Test
        fun `sets cover_id on the book when exactly one cover is marked is_default`()
        {
            val author_id = insertAuthor(schema_1)

            val result = book_repo.create(
                schema_1,
                BookCreateContext(
                    author_id = author_id,
                    title = "Covered Book",
                    covers = listOf(
                        CoverCreateContext(label = "front", is_default = true, filename = "front.jpg", file = ByteArray(1)),
                        CoverCreateContext(label = "alt", is_default = false, filename = "alt.jpg", file = ByteArray(1)),
                    ),
                )
            )

            assertNotNull(result.cover_id)
        }


        @Test
        fun `does not set cover_id when no cover is marked default`()
        {
            val author_id = insertAuthor(schema_1)

            val result = book_repo.create(
                schema_1,
                BookCreateContext(
                    author_id = author_id,
                    title = "No Default Cover",
                    covers = listOf(
                        CoverCreateContext(label = "front", is_default = false, filename = "front.jpg", file = ByteArray(1)),
                    ),
                )
            )

            assertNull(result.cover_id)
        }


        @Test
        fun `creates associated editions`()
        {
            val author_id = insertAuthor(schema_1)

            val result = book_repo.create(
                schema_1,
                BookCreateContext(
                    author_id = author_id,
                    title = "Book With Editions",
                    editions = listOf(
                        EditionCreateContext(label = "ebook", release_date = null, format = "epub", filename = "book.epub", file = ByteArray(1)),
                    ),
                )
            )

            val editions = edition_repo.findAllByBookId(schema_1, result.id)

            assertEquals(1, editions.size)
            assertEquals("epub", editions.first().format)
        }


        @Test
        fun `throws AuthorNotFoundException for a nonexistent author_id`()
        {
            assertThrows<AuthorNotFoundException> {
                book_repo.create(schema_1, BookCreateContext(author_id = Uuid.random(), title = "Orphan Book"))
            }
        }


        @Test
        fun `throws UniverseNotFoundException for a nonexistent universe_id`()
        {
            val author_id = insertAuthor(schema_1)

            assertThrows<UniverseNotFoundException> {
                book_repo.create(schema_1, BookCreateContext(author_id = author_id, universe_id = Uuid.random(), title = "Book"))
            }
        }


        @Test
        fun `throws SeriesNotFoundException for a nonexistent series_id`()
        {
            val author_id = insertAuthor(schema_1)

            assertThrows<SeriesNotFoundException> {
                book_repo.create(schema_1, BookCreateContext(author_id = author_id, series_id = Uuid.random(), title = "Book"))
            }
        }


        @Test
        fun `throws AuthorMismatchException when series belongs to a different author`()
        {
            val author_1 = insertAuthor(schema_1, last_name = "One")
            val author_2 = insertAuthor(schema_1, last_name = "Two")
            val series_id = insertSeries(schema_1, author_1)

            assertThrows<AuthorMismatchException> {
                book_repo.create(schema_1, BookCreateContext(author_id = author_2, series_id = series_id, title = "Book"))
            }
        }


        @Test
        fun `throws AuthorMismatchException when universe belongs to a different author than the series`()
        {
            val author_1 = insertAuthor(schema_1, last_name = "One")
            val author_2 = insertAuthor(schema_1, last_name = "Two")
            val universe_id = insertUniverse(schema_1, author_1)
            val series_id = insertSeries(schema_1, author_2)

            assertThrows<AuthorMismatchException> {
                book_repo.create(
                    schema_1,
                    BookCreateContext(author_id = author_2, universe_id = universe_id, series_id = series_id, title = "Book")
                )
            }
        }


        @Test
        fun `rolls back the DB insert when a cover file save fails`()
        {
            val cover_asset_storage_mock = mockk<AssetStorage>()
            val cover_repo_mock = CoverRepository(cover_asset_storage_mock)
            val book_repo_mock = BookRepository(author_repo, cover_repo_mock, edition_repo)
            val author_id = insertAuthor(schema_1)

            every { cover_asset_storage_mock.store(any(), any(), any(), any()) } throws AssetStorageException("exception message")

            assertThrows<AssetStorageException> {
                book_repo_mock.create(
                    schema_1,
                    BookCreateContext(
                        author_id = author_id,
                        title = "Broken Cover Book",
                        covers = listOf(
                            CoverCreateContext(label = null, is_default = false, filename = "", file = ByteArray(0)),
                        ),
                    )
                )
            }

            val result = book_repo.findAllBy(schema_1, author_id, null, null, null)

            assertTrue(result.isEmpty())
        }
    }


    @Nested
    inner class RepoUpdate
    {
        @Test
        fun `updates only the fields explicitly provided and leaves others untouched`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id, title = "Original Title")

            book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Undefined,
                volume_number = Update.Undefined,
                title = Update.Value("Updated Title"),
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Undefined,
            )

            val result = book_repo.findById(schema_1, book_id)

            assertNotNull(result)
            assertEquals("Updated Title", result.title)
            assertEquals(author_id, result.author_id)
        }


        @Test
        fun `clears a nullable field when explicitly set to null`()
        {
            val author_id = insertAuthor(schema_1)
            val universe_id = insertUniverse(schema_1, author_id)
            val book_id = insertBook(schema_1, author_id, universe_id = universe_id)

            book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Value(null),
                series_id = Update.Undefined,
                cover_id = Update.Undefined,
                volume_number = Update.Undefined,
                title = Update.Undefined,
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Undefined,
            )

            val result = book_repo.findById(schema_1, book_id)

            assertNotNull(result)
            assertNull(result.universe_id)
        }


        @Test
        fun `throws BookNotFoundException for a nonexistent book id`()
        {
            assertThrows<BookNotFoundException> {
                book_repo.update(
                    schema_1,
                    Uuid.random(),
                    author_id = Update.Undefined,
                    universe_id = Update.Undefined,
                    series_id = Update.Undefined,
                    cover_id = Update.Undefined,
                    volume_number = Update.Undefined,
                    title = Update.Value("Whatever"),
                    launch_date = Update.Undefined,
                    word_count = Update.Undefined,
                    blurb = Update.Undefined,
                    notes = Update.Undefined,
                    tag_ids = Update.Undefined,
                )
            }
        }


        @Test
        fun `revalidates ownership when author_id changes and throws AuthorMismatchException on conflict`()
        {
            val author_1 = insertAuthor(schema_1, last_name = "One")
            val author_2 = insertAuthor(schema_1, last_name = "Two")
            val series_id = insertSeries(schema_1, author_1)
            val book_id = insertBook(schema_1, author_1, series_id = series_id)

            assertThrows<AuthorMismatchException> {
                book_repo.update(
                    schema_1,
                    book_id,
                    author_id = Update.Value(author_2),
                    universe_id = Update.Undefined,
                    series_id = Update.Undefined,
                    cover_id = Update.Undefined,
                    volume_number = Update.Undefined,
                    title = Update.Undefined,
                    launch_date = Update.Undefined,
                    word_count = Update.Undefined,
                    blurb = Update.Undefined,
                    notes = Update.Undefined,
                    tag_ids = Update.Undefined,
                )
            }
        }


        @Test
        fun `does not revalidate ownership when author, universe and series are all left untouched`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val result = book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Undefined,
                volume_number = Update.Undefined,
                title = Update.Value("Renamed"),
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Undefined,
            )

            assertNotNull(result)
            assertEquals("Renamed", result.title)
        }


        @Test
        fun `throws CoverNotFoundException when cover_id does not belong to the book`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val other_book_id = insertBook(schema_1, author_id, title = "Other Book")
            val foreign_cover_id = insertCover(schema_1, other_book_id)

            assertThrows<CoverNotFoundException> {
                book_repo.update(
                    schema_1,
                    book_id,
                    author_id = Update.Undefined,
                    universe_id = Update.Undefined,
                    series_id = Update.Undefined,
                    cover_id = Update.Value(foreign_cover_id),
                    volume_number = Update.Undefined,
                    title = Update.Undefined,
                    launch_date = Update.Undefined,
                    word_count = Update.Undefined,
                    blurb = Update.Undefined,
                    notes = Update.Undefined,
                    tag_ids = Update.Undefined,
                )
            }
        }


        @Test
        fun `allows clearing cover_id to null without an ownership check`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val cover_id = insertCover(schema_1, book_id)

            book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Value(cover_id),
                volume_number = Update.Undefined,
                title = Update.Undefined,
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Undefined,
            )

            val cleared = book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Value(null),
                volume_number = Update.Undefined,
                title = Update.Undefined,
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Undefined,
            )

            assertNotNull(cleared)
            assertNull(cleared.cover_id)
        }


        @Test
        fun `adds and removes tags to match the provided tag_ids`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_keep = insertTag(schema_1, "keep")
            val tag_remove = insertTag(schema_1, "remove")
            val tag_add = insertTag(schema_1, "add")
            val book_id = insertBook(schema_1, author_id)
            linkBookTag(schema_1, book_id, tag_keep)
            linkBookTag(schema_1, book_id, tag_remove)

            val result = book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Undefined,
                volume_number = Update.Undefined,
                title = Update.Undefined,
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Value(listOf(tag_keep, tag_add)),
            )

            assertNotNull(result)
            assertEquals(setOf(tag_keep, tag_add), result.tag_ids.toSet())
        }


        @Test
        fun `removing a tag from one book does not remove it from other books`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_id = insertTag(schema_1)
            val book_id_1 = insertBook(schema_1, author_id, title = "Book 1")
            val book_id_2 = insertBook(schema_1, author_id, title = "Book 2")
            linkBookTag(schema_1, book_id_1, tag_id)
            linkBookTag(schema_1, book_id_2, tag_id)

            book_repo.update(
                schema_1,
                book_id_1,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Undefined,
                volume_number = Update.Undefined,
                title = Update.Undefined,
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Value(emptyList()),
            )

            val other_book = book_repo.findById(schema_1, book_id_2)

            assertNotNull(other_book)
            assertEquals(listOf(tag_id), other_book.tag_ids)
        }


        @Test
        fun `leaves tags untouched when tag_ids is Undefined`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_id = insertTag(schema_1)
            val book_id = insertBook(schema_1, author_id)
            linkBookTag(schema_1, book_id, tag_id)

            val result = book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Undefined,
                volume_number = Update.Undefined,
                title = Update.Value("Renamed Only"),
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Undefined,
            )

            assertNotNull(result)
            assertEquals(listOf(tag_id), result.tag_ids)
        }


        @Test
        fun `clears all tags when tag_ids is an explicit empty list`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_id = insertTag(schema_1)
            val book_id = insertBook(schema_1, author_id)
            linkBookTag(schema_1, book_id, tag_id)

            val result = book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Undefined,
                volume_number = Update.Undefined,
                title = Update.Undefined,
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Value(emptyList()),
            )

            assertNotNull(result)
            assertTrue(result.tag_ids.isEmpty())
        }


        @Test
        fun `deduplicates repeated tag_ids on update instead of throwing`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_id = insertTag(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val result = book_repo.update(
                schema_1,
                book_id,
                author_id = Update.Undefined,
                universe_id = Update.Undefined,
                series_id = Update.Undefined,
                cover_id = Update.Undefined,
                volume_number = Update.Undefined,
                title = Update.Undefined,
                launch_date = Update.Undefined,
                word_count = Update.Undefined,
                blurb = Update.Undefined,
                notes = Update.Undefined,
                tag_ids = Update.Value(listOf(tag_id, tag_id)),
            )

            assertNotNull(result)
            assertEquals(listOf(tag_id), result.tag_ids)
        }
    }


    @Nested
    inner class Delete
    {
        @Test
        fun `deletes an existing book and returns true`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val was_deleted = book_repo.delete(schema_1, book_id)

            assertTrue(was_deleted)
            assertNull(book_repo.findById(schema_1, book_id))
        }


        @Test
        fun `returns false for a nonexistent book`()
        {
            val was_deleted = book_repo.delete(schema_1, Uuid.random())

            assertFalse(was_deleted)
        }


        @Test
        fun `cascades and removes associated editions, covers, read records and tags`()
        {
            val author_id = insertAuthor(schema_1)
            val tag_id = insertTag(schema_1)
            val book_id = insertBook(schema_1, author_id)
            val cover_id = insertCover(schema_1, book_id)
            val edition_id = insertEdition(schema_1, book_id)
            insertReadRecord(schema_1, book_id, edition_id = edition_id, start_date = LocalDate.now())
            linkBookTag(schema_1, book_id, tag_id)

            book_repo.delete(schema_1, book_id)

            assertNull(cover_repo.findById(schema_1, cover_id))
            assertNull(edition_repo.findById(schema_1, edition_id))

            val remaining_read_records = tenantTransaction(schema_1)
            {
                ReadRecordTable.selectAll().where { ReadRecordTable.book_id eq book_id }.count()
            }
            val remaining_book_tags = tenantTransaction(schema_1)
            {
                BookTagTable.selectAll().where { BookTagTable.book_id eq book_id }.count()
            }

            assertEquals(0, remaining_read_records)
            assertEquals(0, remaining_book_tags)
        }


        @Test
        fun `does not delete a book belonging to another tenant`()
        {
            val author_id = insertAuthor(schema_2)
            val book_id = insertBook(schema_2, author_id)

            val was_deleted = book_repo.delete(schema_1, book_id)

            assertFalse(was_deleted)
            assertNotNull(book_repo.findById(schema_2, book_id))
        }
    }


    @Nested
    inner class GetBookContext
    {
        @Test
        fun `returns correct filename context including nullable universe and series names`()
        {
            val author_id = insertAuthor(schema_1, first_name = "Jane", last_name = "Doe")
            val universe_id = insertUniverse(schema_1, author_id, name = "The Universe")
            val series_id = insertSeries(schema_1, author_id, universe_id, name = "The Series")
            val book_id = insertBook(schema_1, author_id, universe_id, series_id, title = "The Title", volume_number = 3f)

            val result = book_repo.getBookContext(schema_1, book_id)

            assertNotNull(result)
            assertEquals("Jane", result.first_name)
            assertEquals("Doe", result.last_name)
            assertEquals("The Universe", result.universe)
            assertEquals("The Series", result.series)
            assertEquals(3f, result.volume_number)
            assertEquals("The Title", result.title)
        }


        @Test
        fun `returns context with null universe and series when book has none`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            val result = book_repo.getBookContext(schema_1, book_id)

            assertNotNull(result)
            assertNull(result.universe)
            assertNull(result.series)
        }


        @Test
        fun `returns null for a nonexistent book id`()
        {
            val result = book_repo.getBookContext(schema_1, Uuid.random())

            assertNull(result)
        }
    }


    @Nested
    inner class Exists
    {
        @Test
        fun `returns true for an existing book`()
        {
            val author_id = insertAuthor(schema_1)
            val book_id = insertBook(schema_1, author_id)

            assertTrue(book_repo.exists(schema_1, book_id))
        }


        @Test
        fun `returns false for a nonexistent book`()
        {
            assertFalse(book_repo.exists(schema_1, Uuid.random()))
        }


        @Test
        fun `returns false for a book that exists only in another tenant schema`()
        {
            val author_id = insertAuthor(schema_2)
            val book_id = insertBook(schema_2, author_id)

            assertFalse(book_repo.exists(schema_1, book_id))
        }
    }
}
