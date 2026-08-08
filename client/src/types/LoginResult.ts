


export type LoginResult = {
    success: true
} | {
    success: false
    error_reason: "INVALID_CREDENTIALS" | "NETWORK_ERROR" | "UNKNOWN_ERROR"
}