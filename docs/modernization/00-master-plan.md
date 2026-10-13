# Application modernization plan

Status: **proposed; implementation has not started**. Prepared 2026-10-13 against `8ad90c0`.

This plan improves maintainability, database access, layout adaptability, tests, and behavior documentation while preserving the existing inspection workflows and familiar Spanish UI. This planning change adds Markdown only. It does not move classes, change dependencies, introduce an active `AGENTS.md`, or modify application behavior.

## Recommended approach

Start by recording behavior and protecting it with tests, then reorganize packages. Moving files first would make regressions harder to detect, and some package names are part of the saved-draft format. Keep the existing Java/XML application and single `app` module. Introduce clear boundaries incrementally; a Kotlin conversion, Compose rewrite, and multi-module architecture are separate future decisions.

Android defines standard source/resource directories, but there is no mandatory package tree for every application. This project's `app/src/main`, `res`, `test`, and `androidTest` structure is already conventional. The improvement is inside the Java packages and their responsibilities. The proposed `ui`, `data`, and small business-model/rules packages adapt Android's separation-of-concerns guidance to this repository. [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations)

Database modernization has two distinct concerns: remote SQL Server operations and local drafts/media metadata. Room addresses the latter; it does not replace the former. [Room overview](https://developer.android.com/training/data-storage/room)

**Confirmed scope:** retain direct Android-to-SQL Server access for this modernization. Implementing a backend API is out of scope at the user's direction. [Step 05](05-database-access.md) improves the JDBC adapter, queries, transactions, and connection handling; it mentions an API only as a possible future project. No phase depends on API design, hosting, implementation, or deployment.

## What the repository actually contains

| Observation | Evidence and planning implication |
| --- | --- |
| Java/XML, one application module; min SDK 29, target 33, compile 34; AGP 8.10.1 | [App build](../../app/build.gradle), [root build](../../build.gradle). Preserve this baseline until a separate compatibility step. |
| Broad `Mundo`, `Interfaz`, `Other`, and `CustomView` packages | Reorganize by responsibility and feature; avoid a mass rename. |
| Direct SQL Server access via bundled `jtds-1.2.7.jar` | [DAO](../../app/src/main/java/com/juans/inspeccion/Mundo/DAO.java) owns a shared connection, builds SQL and transaction batches, relaxes StrictMode, and logs a credential-bearing connection URL. |
| SQL generation extends beyond `DAO` | `Consultas`, `InspeccionDataSource`, `UsuarioDataSource`, `DaniosManager`, list/metadata loaders, and UI callers need an operation inventory. |
| UI widgets supply database field metadata and values | `InspeccionDataSource` accepts `CustomView`; `Formularios` traverses a particular table hierarchy. Layout changes require a binding regression check. |
| Damage screen uses table spans and weighted vertical space | `nuevo_danio.xml`, `fragment_entrada_pag2.xml`, and shared styles explain likely width/height pressure in the supplied screenshot. Reproduce at a known window width/font scale before confirming measurements. |
| Tests already exist | 18 JVM test classes, with 94 `@Test` annotations found in source, concentrated on storage/media/printing. This is a static inventory, not a fresh passing test result or coverage measurement. |
| Existing docs already exist | [Android migration](../android-migration.md), [photo gallery](../photo-gallery.md), [embedded camera and video](../embedded-camera.md). They mix behavior, implementation, historical checkpoints, and validation. |
| Persistent compatibility is important | Serialized `listaPendientes` / `fotosPendientes`, released fixtures, media IDs, capture journals, and video sidecars must survive upgrades. |

Links to code in this plan describe the baseline. Update them when later package-move PRs land.

## Sequential roadmap

Each step begins after the preceding step's exit criteria are met, or its explicitly permitted deferral is recorded. Testing and documentation continue in every step; they are not one-time tasks.

| Step | Outcome | Relative size / risk | Detailed plan |
| --- | --- | --- | --- |
| 01 | Record existing behavior, compatibility constraints, and documentation conventions | Small–medium / low | [Baseline and documentation](01-baseline-and-documentation.md) |
| 02 | Protect critical business workflows with regression tests and repeatable checks | Medium / medium | [Regression safety net](02-regression-safety-net.md) |
| 03 | Establish coherent packages without breaking stored objects or Android entry points | Medium / high | [Package structure](03-package-structure.md) |
| 04 | Separate form binding, business inputs, and data operations | Medium–large / high | [Data and business boundaries](04-data-and-business-boundaries.md) |
| 05 | Modernize direct SQL Server queries, transactions, and connection ownership | Large / high | [Database access](05-database-access.md) |
| 06 | Fix truncation, cramped fields, scrolling, and layout adaptation with familiar styling | Medium–large / medium | [Responsive interface](06-responsive-interface.md) |
| 07 | Make asynchronous work and screen state survive lifecycle changes correctly | Medium–large / high | [Lifecycle and state](07-lifecycle-and-state.md) |
| 08 | Decide whether local storage warrants Room; migrate only with recovery/rollback evidence | Small decision or large migration / high | [Local persistence](08-local-persistence.md) |
| 09 | Address remaining platform, dependency, and operational issues in isolated changes | Medium / high | [Platform and dependencies](09-platform-and-dependencies.md) |
| 10 | Validate the complete workflows, pilot an in-place update, and close documentation gaps | Medium / high | [Acceptance and rollout](10-acceptance-and-rollout.md) |

Sizes compare phases, not calendar promises. Estimate actual tickets after steps 01–02 establish behavior, environments, and testability. Step 05 retains direct SQL Server access; step 08 decides whether a new local database is justified. A documented Room deferral completes step 08's decision work. Backend implementation is excluded from these estimates and completion criteria.

## Invariants across every phase

- Preserve the application ID, deployed signing identity, draft files, document/container identifiers, and photo/video references. Test an update; uninstall/reinstall is not an upgrade test.
- Preserve entry/exit and document-type distinctions, read-only restrictions, validation, costs, save/modify behavior, and printing semantics unless a separate behavior change is specified.
- Preserve recent camera/gallery/video work. Videos remain local and outside the FTP photo queue unless a separate feature is requested.
- Preserve recovery after partial failures. A failed read must not become an empty store; a failed remote write must not be reported as success.
- Do not enable automatic uploading as a side effect of refactoring. Do not replay uncertain remote writes or printing automatically.
- Use sanitized fixtures and a dedicated test SQL Server database for integration tests. No validation step needs production records or credentials.
- Keep package moves, behavior changes, dependency upgrades, and storage migrations in separate reviewable commits/PRs.

## Decisions to resolve when their phase starts

| Decision | Default proposal | Evidence needed |
| --- | --- | --- |
| Which direct-access driver/query/connection approach is compatible? | Encapsulated JDBC with prepared statements and operation-owned connections; adopt additional libraries only after validation | SQL Server version/schema, Android/driver compatibility, latency and concurrency measurements |
| Is Room worth the migration risk? | Retain atomic files until there is a demonstrated query/concurrency need | Draft/queue size, observed problems, recovery requirements, rollback compatibility |
| Which windows/devices matter? | Start with 320–840dp widths, large text, keyboard and resizing | Device fleet and real operating conditions; screenshot pixel width alone is insufficient |
| How is the app distributed? | Preserve deployed identity and current supported minimum initially | Private deployment vs store, signing owner, supported OS/device list |
| Which behavior is intentional? | Document current observations separately from proposed fixes | Operator walkthroughs, representative sanitized examples, contradictions in old docs |

Unanswered questions block only the decision or test requiring them. Continue independent documentation, fixtures, and layout work. Do not label unverified workflows as accepted.

## Completion and evidence

For each phase, record the commit, behavior/test IDs covered, checks run, results, remaining exceptions, and rollback constraints. All implementation checkboxes in the detailed plans are initially open. Existing historical reports are references, not proof that this planning revision was tested.

The overall result is complete when the direct SQL Server adapter covers the operation inventory, critical workflows have regression tests, package boundaries are enforced, the UI passes the agreed device/text matrix, upgrades preserve drafts/media, and the current feature documents match operator-visible behavior. Optional work explicitly deferred in a decision record is not silently counted as implemented. A backend API is not required for completion.

Supporting documents: [feature-document template](feature-document-template.md) and [primary references](references.md).
