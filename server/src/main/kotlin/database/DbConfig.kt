package dev.jovanni0.database



data class DbConfig(
    val jdbc_url: String,
    val username: String,
    val password: String,
) {
    fun validate()
    {
        require(jdbc_url.isNotBlank()) {
            "JDBC URL must be provided"
        }

        require(username.isNotBlank()) {
            "DB username must be provided"
        }

        require(password.isNotBlank()) {
            "DB password audience must be provided"
        }
    }
}
