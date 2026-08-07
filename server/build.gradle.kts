plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(ktorLibs.plugins.ktor)
}

group = "dev.jovanni0"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("io.ktor:ktor-server-core:3.5.1")
    implementation("io.ktor:ktor-server-netty:3.5.1")
    implementation("io.ktor:ktor-server-content-negotiation:3.5.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.5.1")
    implementation("io.ktor:ktor-server-auth:3.5.1")
    implementation("io.ktor:ktor-server-auth-jwt:3.5.1")
    implementation("io.ktor:ktor-server-status-pages:3.5.1")
    implementation("io.ktor:ktor-server-call-logging:3.5.1")
    implementation("io.ktor:ktor-server-partial-content:3.5.1")
    implementation("io.ktor:ktor-server-config-yaml:3.5.1")
    implementation("io.ktor:ktor-server-cors:3.5.1")
    implementation("ch.qos.logback:logback-classic:1.5.35")

    implementation("org.postgresql:postgresql:42.7.11")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.jetbrains.exposed:exposed-core:1.3.0")
    implementation("org.jetbrains.exposed:exposed-dao:1.3.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.3.0")
    implementation("org.jetbrains.exposed:exposed-java-time:1.3.0")


    implementation("org.flywaydb:flyway-core:12.10.0")
    implementation("org.flywaydb:flyway-database-postgresql:12.10.0")

    implementation("at.favre.lib:bcrypt:0.10.2")

    testImplementation("io.ktor:ktor-server-test-host:3.5.1")
    testImplementation("io.ktor:ktor-client-content-negotiation:3.5.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
    testImplementation("io.mockk:mockk:1.14.9")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Djdk.instrument.traceUsage")
}
