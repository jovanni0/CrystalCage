package routes.auth

import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.auth.PasswordHasher
import dev.jovanni0.configureAuth
import dev.jovanni0.configureRouting
import dev.jovanni0.configureSerializer
import dev.jovanni0.data_types.auth.LoginRequest
import dev.jovanni0.data_types.auth.LoginResponse
import dev.jovanni0.data_types.auth.MeResponse
import dev.jovanni0.repositories.UserRepository
import dev.jovanni0.tables.User
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
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.Test
import kotlin.test.assertEquals



@Testcontainers
class MeRouteTests
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
        val user_repository = UserRepository()
        val username = "John"
        val password = "password123@"
    }


    @BeforeEach
    fun setUp()
    {
        Database.connect(
            url = postgres.jdbcUrl,
            user = postgres.username,
            password = postgres.password
        )

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
    }


    /**
     * creates a user in the `Users` table.
     */
    private fun seedUser(username: String, password: String)
    {
        transaction {
            User.insert {
                it[User.username] = username
                it[User.password_hash] = PasswordHasher.hash(password)
                it[User.role] = "user"
            }
        }
    }


    @Test
    fun `me with valid token returns the user details`() = testApplication {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureRouting(jwt_config, user_repository)
        }

        seedUser(username, password)

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val login_response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    username = username,
                    password = password
                )
            )
        }
        val token = login_response.body<LoginResponse>().token

        val me_response = client.get("/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, me_response.status)
        assertEquals(username, me_response.body<MeResponse>().username)
    }


    @Test
    fun `me without token returns 401`() = testApplication {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureRouting(jwt_config, user_repository)
        }

        val me_response = client.get("/auth/me")

        assertEquals(HttpStatusCode.Unauthorized, me_response.status)
    }


    @Test
    fun `me with bad token returns 401`() = testApplication {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureRouting(jwt_config, user_repository)
        }

        val me_response = client.get("/auth/me") {
            header(HttpHeaders.Authorization, "Bearer not-a-valid-token")
        }

        assertEquals(HttpStatusCode.Unauthorized, me_response.status)
    }
}
