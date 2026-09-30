package dev.jovanni0.auth



data class AuthConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
) {
    fun validate()
    {
        require(secret.isNotBlank()) {
            "JWT secret must be provided"
        }

        require(issuer.isNotBlank()) {
            "JWT issuer must be provided"
        }

        require(audience.isNotBlank()) {
            "JWT audience must be provided"
        }

        require(realm.isNotBlank()) {
            "JWT realm must be provided"
        }
    }
}
