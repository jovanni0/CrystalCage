

/**
 * counter used by `generateEphemeralId()`
 */
let ephemeral_id_generator_counter = 0



/**
 * generates a locally-unique ID for client-side, single-session, ephemeral use (ex: draft list keys before server persistence).
 * 
 * NOT collisions-resistant across tabs/sessions/restarts and NOT suitable for anything persistent, synced or security-related.
 * use a real UUID source for those cases instead.
 */
export function generateEphemeralId(): string
{
    ephemeral_id_generator_counter += 1
    
    return `local-${Date.now()}-${ephemeral_id_generator_counter}`
}