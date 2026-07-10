# Server Issues

## connection pooling and `search_path` leakage

**problem**: HikariCP does not reset session-level state (including `search_path`) on connection checkin/checkout. confirmed experimentally using a test in `TenantIsolationTests`: a pooled connection with `maximumPoolSize = 1` retains the previous request's `search_path` when handed to the next request. this means a request that forgets to explicitly set the schema will silently run queries against whichever tenant schema the connection was last used for (a cross-tenant data leak).

**solution**:
- **structural enforcement** = all tenant-scoped queries must go through a `tenantTransaction(schema_name) { }` wrapper (instead of the raw Exposed `transaction { }`) which calls `SET search_path TO "schema_name"` as the first statement of every transaction, before any query runs. this removes the "someone forgot to set the schema" failure mode, since there is no code path to tenant data that skips it.
- **pool-level reset as a safety net** = HikariCP is configured with `.setSchema()` (or equivalent reset hook) so that even if some future code path bypasses `tenantTransaction`, the connection returns a known, non-tenant default schema on checkin. this turns the silent leak  into a loud failure rather then a correctness bug.

> solution 1 is the actual fix, solution 2 is a defense in case solution 1 is ever bypassed by mistake.