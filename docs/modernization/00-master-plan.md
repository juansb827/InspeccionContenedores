# Application modernization plan

Status: **steps 01/02 delivered; step 03 storage cutover and package implementation delivered with active-serialization exceptions and explicit operator/signed-update acceptance open; steps 04–10 unstarted**. The starting roadmap was prepared against `8ad90c0`; baseline documentation reviewed the working tree at `0a365c1`. Current execution and handoff are in [step-03 evidence](../testing/step-03-cutover-and-moves.md).

This plan improves maintainability, database access, layout adaptability, tests, and behavior documentation while preserving the existing inspection workflows and familiar Spanish UI. The original roadmap was planning-only. Step 01 now adds the [documentation index](../README.md), feature/architecture/testing documents, decisions and root [AGENTS.md](../../AGENTS.md). It does not move classes, change dependencies or modify application behavior.

## Recommended approach

Start by recording behavior and protecting it with tests, then reorganize packages. Before renaming persisted models, introduce explicit format recognition and the accepted old-data cutover; old draft/queue class names no longer need compatibility wrappers. Keep the existing Java/XML application and single `app` module. Introduce clear boundaries incrementally; a Kotlin conversion, Compose rewrite, and multi-module architecture are separate future decisions.

Android defines standard source/resource directories, but there is no mandatory package tree for every application. This project's `app/src/main`, `res`, `test`, and `androidTest` structure is already conventional. The improvement is inside the Java packages and their responsibilities. The proposed `ui`, `data`, and small business-model/rules packages adapt Android's separation-of-concerns guidance to this repository. [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations)

Database modernization has two distinct concerns: remote SQL Server operations and local drafts/media metadata. Room addresses the latter; it does not replace the former. [Room overview](https://developer.android.com/training/data-storage/room)

**Confirmed scope:** retain direct Android-to-SQL Server access for this modernization. Implementing a backend API is out of scope at the user's direction. [Step 05](05-database-access.md) improves the JDBC adapter, queries, transactions, and connection handling; it mentions an API only as a possible future project. No phase depends on API design, hosting, implementation, or deployment.

**Confirmed storage decision:** backward compatibility for old drafts and the unused photo-upload queue is not required. The app must identify unsupported old formats, explain their unavailability, and allow new drafts without confusing corruption or I/O errors with version incompatibility. Public media remains intact. The [storage version policy](storage-version-policy.md) defines the cutover and supersedes the earlier draft/queue preservation requirements.

## What the repository actually contains

| Observation | Evidence and planning implication |
| --- | --- |
| Java/XML, one application module; min SDK 29, target 33, compile 34; AGP 8.10.1 | [App build](../../app/build.gradle), [root build](../../build.gradle). Preserve this baseline until a separate compatibility step. |
| Lowercase responsibility/feature implementations; retained serialized types and Android bridges | [84-class move inventory/exceptions](../architecture/package-moves.md); package ownership is not step-04 architectural separation. |
| Direct SQL Server access via bundled `jtds-1.2.7.jar` | [DAO](../../app/src/main/java/com/juans/inspeccion/data/remote/sqlserver/DAO.java) owns a shared connection, builds SQL and transaction batches, relaxes StrictMode, and logs a credential-bearing connection URL. |
| SQL generation extends beyond `DAO` | `Consultas`, `InspeccionDataSource`, `UsuarioDataSource`, `DaniosManager`, list/metadata loaders, and UI callers need an operation inventory. |
| UI widgets supply database field metadata and values | `InspeccionDataSource` accepts `CustomView`; `Formularios` traverses a particular table hierarchy. Layout changes require a binding regression check. |
| Damage screen uses table spans and weighted vertical space | `nuevo_danio.xml`, `fragment_entrada_pag2.xml`, and shared styles explain likely width/height pressure in the supplied screenshot. Reproduce at a known window width/font scale before confirming measurements. |
| Tests already exist | Step-01 baseline: 18 JVM suites / 94 tests. Step 02 now executes 26 suites / 174 tests (80 added), with rule/form/request/submission/recovery/preflight coverage. Counts are not a coverage percentage or device/SQL acceptance. |
| Existing docs already exist | [Android migration](../android-migration.md), [photo gallery](../photo-gallery.md), [embedded camera and video](../embedded-camera.md). They mix behavior, implementation, historical checkpoints, and validation. |
| Several local formats are in use | Retire serialized `listaPendientes` / `fotosPendientes` with explicit recognition; preserve public media and independently assess media IDs, capture journals and video sidecars. |

Links to code in this plan describe the baseline. Update them when later package-move PRs land.

## Sequential roadmap

Each step begins after the preceding step's exit criteria are met, or its explicitly permitted deferral is recorded. Testing and documentation continue in every step; they are not one-time tasks.

| Step | Outcome | Relative size / risk | Detailed plan |
| --- | --- | --- | --- |
| 01 | Documentation complete; operator/device/service acceptance assigned as unresolved | Small–medium / low | [Baseline and documentation](01-baseline-and-documentation.md) |
| 02 | 80 regression tests added; future cutover/new-format recovery criterion gated for step 03 | Medium / medium | [Regression safety net](02-regression-safety-net.md) |
| 03 | Atomic JSON cutover and 84 moves delivered; stable Android entries/active serialization exceptions verified locally; operator/signed-update acceptance open | Medium / high | [Package structure](03-package-structure.md) |
| 04 | Separate form binding, business inputs, and data operations | Medium–large / high | [Data and business boundaries](04-data-and-business-boundaries.md) |
| 05 | Modernize direct SQL Server queries, transactions, and connection ownership | Large / high | [Database access](05-database-access.md) |
| 06 | Fix truncation, cramped fields, scrolling, and layout adaptation with familiar styling | Medium–large / medium | [Responsive interface](06-responsive-interface.md) |
| 07 | Make asynchronous work and screen state survive lifecycle changes correctly | Medium–large / high | [Lifecycle and state](07-lifecycle-and-state.md) |
| 08 | Decide whether local storage warrants Room; migrate only with recovery/rollback evidence | Small decision or large migration / high | [Local persistence](08-local-persistence.md) |
| 09 | Address remaining platform, dependency, and operational issues in isolated changes | Medium / high | [Platform and dependencies](09-platform-and-dependencies.md) |
| 10 | Validate the complete workflows, pilot an in-place update, and close documentation gaps | Medium / high | [Acceptance and rollout](10-acceptance-and-rollout.md) |

Sizes compare phases, not calendar promises. Estimate actual tickets after steps 01–02 establish behavior, environments, and testability. Step 05 retains direct SQL Server access; step 08 decides whether a new local database is justified. A documented Room deferral completes step 08's decision work. Backend implementation is excluded from these estimates and completion criteria.

## Invariants across every phase

- Preserve the application ID, deployed signing identity, SQL Server records/document identifiers, public media and current-format drafts. Old drafts/upload queues follow the explicit retirement policy. Test an update; uninstall/reinstall is not an upgrade test.
- Preserve entry/exit and document-type distinctions, read-only restrictions, validation, costs, save/modify behavior, and printing semantics unless a separate behavior change is specified.
- Preserve recent camera/gallery/video work. Videos remain local and outside the FTP photo queue unless a separate feature is requested.
- Preserve recovery after partial failures. Only positively recognized retired formats may lead to a fresh separate store with a notice; corruption, unknown data and I/O failures must not silently become empty stores. A failed remote write must not be reported as success.
- Do not enable automatic uploading as a side effect of refactoring. Do not replay uncertain remote writes or printing automatically.
- Use sanitized fixtures and a dedicated test SQL Server database for integration tests. No validation step needs production records or credentials.
- Keep package moves, behavior changes, dependency upgrades, and storage migrations in separate reviewable commits/PRs.

## Decisions to resolve when their phase starts

| Decision | Default proposal | Evidence needed |
| --- | --- | --- |
| Which direct-access driver/query/connection approach is compatible? | Encapsulated JDBC with prepared statements and operation-owned connections; adopt additional libraries only after validation | SQL Server version/schema, Android/driver compatibility, latency and concurrency measurements |
| Is Room worth a later storage-engine change? | Start with explicit versioned atomic files; consider Room for demonstrated query/concurrency needs | Current-format draft size, observed problems, recovery requirements; no legacy draft/queue importer |
| Which windows/devices matter? | Start with 320–840dp widths, large text, keyboard and resizing | Device fleet and real operating conditions; screenshot pixel width alone is insufficient |
| How is the app distributed? | Preserve deployed identity and current supported minimum initially | Private deployment vs store, signing owner, supported OS/device list |
| Which behavior is intentional? | Document current observations separately from proposed fixes | Operator walkthroughs, representative sanitized examples, contradictions in old docs |

Unanswered questions block only the decision or test requiring them. Continue independent documentation, fixtures, and layout work. Do not label unverified workflows as accepted.

## Completion and evidence

For each phase, record the commit, behavior/test IDs covered, checks run, results, remaining exceptions, and rollback constraints. Step 01 documentation criteria are satisfied; step 02 satisfies four criteria with its full future-storage criterion explicitly deferred to real step-03 cutover. Steps 03–10 remain unstarted. Existing historical reports are references, not proof that this planning revision was tested.

The overall result is complete when the direct SQL Server adapter covers the operation inventory, critical workflows have regression tests, package boundaries are enforced, the UI passes the agreed device/text matrix, old-format drafts/queues are recognized and retired cleanly, new drafts and public media remain safe, and the current feature documents match operator-visible behavior. Optional work explicitly deferred in a decision record is not silently counted as implemented. A backend API or restored photo-upload feature is not required for completion.

Supporting documents: [storage version policy](storage-version-policy.md), [feature-document template](feature-document-template.md) and [primary references](references.md).

## Step 01 completion and handoff — 2026-10-13

Documentation-only work now describes all primary workflows with stable IDs and S/T/E/H/P evidence, current architecture/integrations, local stores/media/journals and external effects, accepted scope/storage ADRs, verified commands and an [acceptance register](../testing/acceptance.md). Historical camera/gallery/migration files remain in place with reconciliation notices; root README links to the index and AGENTS.md links to current facts/checks.

Fresh verification against working-tree source at `0a365c1`: debug assembly, test APK assembly, lintDebug and unsigned release assembly succeeded. The combined command reused its JVM test output; a forced fresh run then executed **94 tests in 18 suites, zero failures/errors/skips**. Lint reported **645 warnings and 7 errors filtered by the existing baseline**. This was not a clean-cache build, device acceptance or production-signing result. [Commands/environment/evidence](../testing/baseline.md).

All step-01 documentation exit criteria are satisfied, with unavailable operator/service/physical-device acceptance assigned to ACCEPT-01–08. No walkthrough/screenshots, authenticated SQL workflows, device suites, printing or in-place upgrade were performed. The connected API 37 emulator was inspected read-only. Device fleet, distribution/signing owner, usable test services, turno availability and suspected damage/receipt quirks remain unresolved. No application code, tests, dependencies, build configuration or storage behavior changed; the pre-existing IDE edit was preserved.

Step 02 should reproduce the baseline and add targeted characterization/failure tests and small test seams for damage rules/costs, field binding, inspection save/modify, container/booking branches and storage recognition. Preserve legacy fixture bytes for recognition and active media/printing checks. Do not assume historical deserialization is a permanent requirement, implement the full cutover/package moves yet, enable uploads or connect default tests to production. See [step 01 evidence/handoff](01-baseline-and-documentation.md) and the [coverage matrix](../testing/acceptance.md).

## Step 02 implementation and step-03 handoff — 2026-10-13

[Step-02 evidence](../testing/step-02-regression.md) and the updated [coverage matrix](../testing/acceptance.md) connect 80 new tests to stable behavior IDs. The real calculation/validation, form XML/helpers/menu, SQL request generation, submission/local cleanup, current draft recovery and unwired recognition preflight are exercised with synthetic inputs/fake external operations. Minimal seams retain existing defaults, SQL, serialization, layouts and quirks; no domain/package rewrite or cutover was implemented.

The original 94 tests were freshly reproduced, then the final fresh local runner passed **174 tests in 26 suites** (zero failures/errors/skips), debug/test APK assembly and lint: **645 warnings, 7 baseline-filtered errors unchanged**. Local shell/YAML/report and documentation checks were performed; hosted CI, device suites, operator/service/physical printer and upgrade acceptance remain unverified. The final reviewed unsigned release assembly also passed (51s); the GitHub Actions workflow is configured, not a reported hosted pass. Existing uncommitted documentation, unrelated IDE/title edits and fixture bytes were preserved.

Step 02's future storage criterion is explicitly partial. **GATE-03-01/02 must be satisfied by the actual prerequisite cutover before persisted-model renaming:** record-schema/encoding validation and supported-format reading independent of writer app version; separate atomic current namespace; accurate older/newer/corrupt/unknown/I/O states; durable isolation/notice/markers; interruption/repeat/new-draft recovery; no queue replay/blocking; independent capture/deletion/publication/import/index/backup/Android-extra audit and preservation. Probe tests prove neither record semantics nor a migration. Preserve legacy bytes and replace obsolete deserialization expectations only at intentional cutover.

GATE-03-03 then protects Android entry points/XML/runner/recreation after small move groups. Full UI/loader/receipt/no-turno paths and operator intent remain GATE-04-01 / ACCEPT-01–08; real SQL Server transaction/dialect/concurrency/uncertain-commit evidence remains GATE-05-01. Keep suspected calculation/validation/workflow fixes separate from characterization and package/storage changes.

## Step 03 delivery and step-04 handoff — 2026-10-13

GATE-03-01/02 now use real schema/read/write/isolation/notice/queue-retirement paths, not an envelope-only preflight or fake reset. Current drafts and recovery survive package moves; original fixture bytes and public-media/bookkeeping identities remain. GATE-03-03 has component/fragment/XML/JVM/build/local-device evidence, with authenticated saved-task and deployed signed-update acceptance still open. [Step-03 status](03-package-structure.md), [schema/recovery](../architecture/draft-schema-v1.md), [package inventory](../architecture/package-moves.md) and [results](../testing/step-03-cutover-and-moves.md) define the actual scope.

Step 04 should begin with typed inspector lookup and plain form-input mapping, then narrow contracts around existing adapters. Preserve hidden keys, dates, mode distinctions, characterized business quirks, schema 1 and media recovery. Replace active serialized widget/model/journal contracts safely before removing their exceptions. No API, SQL rewrite, Room or layout redesign was introduced in step 03; SQL integration remains step 05. No operator approval, physical printer/camera quality or hosted CI execution is implied.
