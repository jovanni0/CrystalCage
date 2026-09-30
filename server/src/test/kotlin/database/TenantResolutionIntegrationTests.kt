package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.auth.PasswordHasher
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.data_types.auth.LoginRequest
import dev.jovanni0.data_types.auth.LoginResponse
import dev.jovanni0.data_types.author.AuthorResponse
import dev.jovanni0.database.provisionTenantSchema
import dev.jovanni0.database.transactions.sharedTransaction
import dev.jovanni0.database.transactions.tenantTransaction
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.UserRepository
import dev.jovanni0.routes.authRoutes
import dev.jovanni0.routes.authorRoutes
import dev.jovanni0.tables.tenant.AuthorTable
import dev.jovanni0.tables.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.ktor.server.routing.routing



@Testcontainers
class TenantResolutionIntegrationTests
{
    companion object
    {
        @Container
        val postgres = PostgreSQLContainer("postgres:16")

        val jwt_config = AuthConfig(
            secret = "test-secret",
            issuer = "test-issuer",
            audience = "test-audience",
            realm = "test-realm"
        )

        lateinit var data_source: HikariDataSource

        val user_repository = UserRepository()
        val author_repository = AuthorRepository()
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

        sharedTransaction {
            SchemaUtils.create(User)
        }
    }


    /**
     * create a user in the `User` table and it's schema.
     */
    private fun seedTenant(username: String, password: String): String
    {
        val user_id = transaction {
            User
                .insertReturning(listOf(User.id)) {
                    it[User.username] = username
                    it[User.password_hash] = PasswordHasher.hash(password)
                    it[User.role] = "user"
                }
                .single()[User.id]
                .toString()
        }

        provisionTenantSchema(data_source, user_id)

        tenantTransaction("user_$user_id") {
            SchemaUtils.create(AuthorTable)
        }

        return user_id
    }


    /**
     * request login and return the token.
     */
    private suspend fun login(client: HttpClient, username: String, password: String): String
    {
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    username = username,
                    password = password
                )
            )
        }

        return response.body<LoginResponse>().token
    }


    @Test
    fun `request with valid token resolves to the correct schema`() = testApplication {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            routing {
                authRoutes(user_repository, jwt_config)
                authorRoutes(author_repository)
            }
        }

        val username = "John"
        val password = "password"
        val author = "Asher"

        val user_id = seedTenant(username, password)

        tenantTransaction("user_$user_id") {
            AuthorTable.insert {
                it[last_name] = author
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val token = login(client, username, password)

        assertTrue(token.isNotEmpty())

        val response = client.get("/authors") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, response.body<List<AuthorResponse>>().size)
        assertEquals(listOf(author), response.body<List<AuthorResponse>>().map { it.last_name })
    }
}
