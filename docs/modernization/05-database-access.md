# 05 — Database access, queries, and connections

Status: proposed. Depends on step 04. Outcome: all remote operations use bound values, defined transaction boundaries, owned connections, and testable results.

## Technology decision

The current app connects directly to Microsoft SQL Server with jTDS. Its `DAO` is not a Room DAO, and the unused SQLite-style constants in `InspeccionDataSource` do not establish an existing SQLite database.

**Confirmed scope:** keep direct SQL Server access. The user has excluded backend API implementation from this modernization. The selected approach is an encapsulated JDBC adapter with prepared statements, explicit transaction/connection ownership, and bounded execution. Library and driver compatibility remain implementation decisions; backend availability is not an open decision or prerequisite.

| Option | Fit for this project | Proposed disposition |
| --- | --- | --- |
| Android → encapsulated JDBC adapter → SQL Server | Preserves today's deployment, but the device still holds database access | Selected for this plan: prepared statements, explicit connection/transaction ownership, bounded work and integration tests |
| Room on Android | Local SQLite database with entities, DAO methods and checked SQL | Evaluate for local drafts/queues in step 08; cannot execute existing SQL Server queries. [Room](https://developer.android.com/training/data-storage/room) |

A query builder, driver, and connection pool solve different problems. Choosing one does not establish transaction correctness or retry safety. Do not add a server-oriented JDBC pool to Android just because it is common in backend Java, and do not build a new general-purpose query builder inside this app.

Evaluate any proposed in-app SQL helper or connection-pool library against Android, jTDS and the deployed SQL Server version before adoption. Prepared statements are the baseline; a query DSL or pool must demonstrate a concrete benefit and compatible lifecycle/resource behavior. Record the residual credential/network limitations of direct access without making backend work an exit requirement.

## 1. Inventory operations and preserve semantics

List each query/write with its caller, input types, outputs, tables/functions, sorting, null/date handling, error semantics, and transaction membership. Cover `DAO`, `Consultas`, `InspeccionDataSource`, `UsuarioDataSource`, `DaniosManager`, metadata/list loading, settings, search/export, and remaining UI callers. Search for SQL execution and construction; reviewing only `DAO.java` misses substantial logic.

Obtain a sanitized SQL Server schema and representative fixture data, including functions such as `FC26_ULTIMAENTRADACONTENEDOR`, defaults, triggers, document-number allocation, uniqueness constraints, permissions and collation. Inspect current insert/reinsert behavior before replacing it: delete/reinsert can differ from update when triggers or foreign keys exist. Determine whether IDs come from the server or are computed on the client and protect concurrent allocation.

## 2. Modernize direct access

1. Stop logging connection URLs containing usernames/passwords, complete SQL with business values, and raw secrets in errors. Preserve actionable sanitized error codes and operation names.
2. Introduce a connection provider at the adapter boundary. Use operation-owned connections with bounded concurrency as the initial design; serialize work if the driver/environment requires it. Share one connection only within a single transaction, never across unrelated concurrent operations. Measure connection setup cost before deciding that pooling is necessary.
3. Convert each query family to fixed SQL with bound parameters and typed result mapping. Parameterize values, including quotes, Unicode, dates, decimals, nulls, filters and search patterns. Table/column/sort identifiers cannot be bound like values: select them from an explicit trusted allowlist derived from the schema/configuration contract. Do not accept arbitrary identifiers from forms or a generic SQL string from UI code.
4. Close statements/results and owned connections deterministically on success, exception, timeout and cancellation. A pooled future implementation must return only clean connections and discard broken ones. Specify connect/query deadlines, maximum queued work, cancellation behavior and reconnect after network changes; prove actual jTDS behavior on API 29 and target devices.
5. Replace concatenated `BEGIN TRAN` batches with an explicit transaction boundary using the chosen driver's transaction API. For JDBC this means disabling autocommit for the operation, committing once, rolling back on failure, and reliably restoring/closing the connection. Keep header/details and linked container/turno mutations in their documented atomic units. Verify against SQL Server, including driver failure modes.
6. Remove the broad StrictMode network exemption once all adapter entry points dispatch off the main thread. Migration cannot leave a synchronous UI caller hidden behind the new interface.

The bundled driver's replacement is a separate compatibility evaluation; do not assume a modern desktop/server JDBC driver works on Android. Record driver/JDK/Android/SQL Server/TLS support before introducing one.

## 3. Handle uncertainty without duplicate operations

Distinguish a validation rejection, confirmed rollback, duplicate key, and lost response after a possibly successful commit. Read operations may have bounded retries for transient failures. Writes need a stable operation identity and a server-verifiable reconciliation strategy before retry. A timeout must retain the draft and show a recoverable status rather than success or an unconditional resubmission.

Determine whether existing document uniqueness can safely identify a request; do not equate a repeated key with identical content. Reconcile uncertain outcomes through SQL Server reads using the operation's stable business identity and expected content. Where existing schema/constraints cannot establish the result safely, retain the draft and require reconciliation instead of automatic retry; document any required database-side change separately. Mobile button disabling alone cannot prevent concurrent duplicates across clients. Database commit and FTP photo transfer are separate operations: retain queued photos until transfer acknowledgement and test partial completion. This phase does not add video upload or automatic background uploading.

## Future option — outside this plan

A separately scoped backend API could eventually own database credentials, authorization and pooling behind business endpoints. Kysely is a possible TypeScript backend option; jOOQ is a possible JVM option subject to SQL Server edition/licensing and runtime compatibility. These are future references, not dependencies or implementation tasks for this plan. [Kysely](https://kysely.dev/docs/getting-started), [jOOQ editions](https://www.jooq.org/download/)

Keep the repository boundary independent of JDBC details so that a future transport change remains possible. Do not add API scaffolding, endpoints, a backend service, HTTP cutover logic, or deployment work now.

## Suggested migration PRs

1. Operation inventory, test database fixtures, connection/execution abstraction and log redaction.
2. Inspector/reference lookups; then container/booking/damage-price reads.
3. Search/export reads and dynamic metadata/identifier handling.
4. Inspection header/detail submit and modify transactions.
5. Container/turno writes, duplicate/uncertain-outcome handling, and remaining callers.
6. Remove dead SQL builders and shared-connection paths after all callers are accounted for; finalize the direct-access integration suite and resource/latency measurements.

## Exit criteria

- [ ] Every inventoried operation has a migrated owner and regression case; residual raw access is either removed or explicitly deferred with a release consequence.
- [ ] Inputs containing apostrophes, Unicode, SQL-like text, nulls, long values and date/decimal boundaries behave correctly as data.
- [ ] Injected mid-transaction failure leaves no partial header/detail or container/turno update.
- [ ] Concurrent submissions, duplicate IDs, connect loss, statement failure, and uncertain commit are tested against a dedicated SQL Server database.
- [ ] Repeated/cancelled operations demonstrate bounded resources and no connection/result leaks; record representative latency versus baseline.
- [ ] Business UI contracts expose no SQL or JDBC connections; credential handling is confined to dedicated configuration/data components, and credential-bearing logging is removed.
- [ ] The selected JDBC driver and any query/pool helper are validated on supported devices, and failures preserve recoverable work.

Rollback: choose the previous adapter only before a new operation starts and after reconciling in-flight work. A code rollback cannot undo committed database writes. Preserve compatibility with the deployed SQL Server schema and do not restore known credential logging or unsafe query interpolation as an operational fallback.
