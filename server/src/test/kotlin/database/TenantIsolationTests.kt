package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.DriverManager
import java.util.concurrent.Executors
import java.util.concurrent.Phaser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse



@Testcontainers
class TenantIsolationTests
{
    companion object
    {
        @Container
         val postgres = PostgreSQLContainer("postgres:16")
    }


    @BeforeEach
    fun createSchema()
    {
        DriverManager
            .getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
            .use {
                it.createStatement()
                    .use { statement ->
                        statement.execute("CREATE SCHEMA user_1")
                        statement.execute("CREATE SCHEMA user_2")

                        statement.execute("CREATE TABLE user_1.author (id SERIAL PRIMARY KEY, last_name TEXT)")
                        statement.execute("CREATE TABLE user_2.author (id SERIAL PRIMARY KEY, last_name TEXT)")

                        statement.execute("INSERT INTO user_1.author (last_name) VALUES ('Asher')")
                        statement.execute("INSERT INTO user_2.author (last_name) VALUES ('Wells')")
                    }
            }
    }


    @AfterEach
    fun dropSchema()
    {
        DriverManager
            .getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
            .use {
                it.createStatement()
                    .use { statement ->
                        statement.execute("DROP SCHEMA user_1 CASCADE")
                        statement.execute("DROP SCHEMA user_2 CASCADE")
                    }
            }
    }


    @Test
    fun `search_path isolates tenant data`()
    {
        val connection = DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)

        connection.createStatement()
            .use { statement ->
                statement.execute("SET search_path TO user_1")
                val result_set = statement.executeQuery("SELECT last_name FROM author")

                result_set.next()

                assertEquals("Asher", result_set.getString("last_name"))
                assertFalse(result_set.next(), "user_1 should see exactly one row in AUTHOR table")
            }

        connection.createStatement()
            .use { statement ->
                statement.execute("SET search_path TO user_2")
                val result_set = statement.executeQuery("SELECT last_name FROM author")

                result_set.next()

                assertEquals("Wells", result_set.getString("last_name"))
                assertFalse(result_set.next(), "user_2 should see exactly one row in AUTHOR table")
            }

        connection.close()
    }


    @Test
    fun `search_path isolates parallel tenant requests`()
    {
        val executor = Executors.newFixedThreadPool(2)
        val phaser = Phaser(3)

        val task_1 = executor.submit<String> {
            DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
                .use { connection ->
                    connection.createStatement()
                        .use { statement ->
                            statement.execute("SET search_path TO user_1")

                            phaser.arriveAndAwaitAdvance()

                            val result_set = statement.executeQuery("SELECT last_name FROM author")
                            result_set.next()
                            result_set.getString("last_name")
                        }
                }
        }
        val task_2 = executor.submit<String> {
            DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
                .use { connection ->
                    connection.createStatement()
                        .use { statement ->
                            statement.execute("SET search_path TO user_2")

                            phaser.arriveAndAwaitAdvance()

                            val result_set = statement.executeQuery("SELECT last_name FROM author")
                            result_set.next()
                            result_set.getString("last_name")
                        }
                    }
        }

        phaser.arriveAndAwaitAdvance()
        val result_1 = task_1.get()
        val result_2 = task_2.get()
        executor.shutdown()

        assertEquals("Asher", result_1)
        assertEquals("Wells", result_2)
    }


    @Test
    fun `pooled connection leaks search_path across checkouts`()
    {
        val hikari_config = HikariConfig().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username
            password = postgres.password
            maximumPoolSize = 1
        }
        val data_source = HikariDataSource(hikari_config)

        data_source.connection.use { connection ->
            connection
                .createStatement()
                .use { statement ->
                    statement.execute("SET search_path TO user_1")

                    val result_set = statement.executeQuery("SELECT last_name FROM author")
                    result_set.next()

                    assertEquals("Asher", result_set.getString("last_name"))
                }
        }

        data_source.connection.use { connection ->
            connection
                .createStatement()
                .use { statement ->
                    val result_set = statement.executeQuery("SHOW search_path")
                    result_set.next()

                    assertEquals("user_1", result_set.getString(1))
                }
        }

        data_source.close()
    }
}
