# Primary references for the modernization plan

Consulted during planning on 2026-10-13. These explain platform capabilities; the phase sequencing, package names, migration boundaries and UI proposals are project-specific recommendations based on repository inspection. Recheck versions and requirements when implementing.

| Reference | Relevance |
| --- | --- |
| [Android architecture recommendations](https://developer.android.com/topic/architecture/recommendations) | Separate UI/data responsibilities, repository boundaries, and selective use of additional layers |
| [Room overview](https://developer.android.com/training/data-storage/room) | Room is a local SQLite abstraction, with entities and DAOs; it is not a remote SQL Server client |
| [Room release and setup documentation](https://developer.android.com/jetpack/androidx/releases/room) | Java annotation processing and version/toolchain compatibility to verify before adoption |
| [Responsive/adaptive layouts with Views](https://developer.android.com/develop/ui/views/layout/responsive-adaptive-design-with-views) | Available-window sizing, flexible layouts and qualified resources |
| [Android accessibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps) | Touch targets, labels and accessible interaction |
| [Android testing fundamentals](https://developer.android.com/training/testing/fundamentals) | Local vs instrumented tests, scopes and testable boundaries |
| [Saving UI state](https://developer.android.com/topic/libraries/architecture/saving-states) | Distinguish configuration-change state, saved state and durable storage |
| [Java serialization versioning](https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/version.html) | Persisted class compatibility needs deliberate migration |
| [Kysely getting started](https://kysely.dev/docs/getting-started) | Future reference only: possible TypeScript backend tooling; backend implementation is outside this plan |
| [jOOQ editions](https://www.jooq.org/download/) | Future reference only: SQL Server edition/licensing and Java compatibility for a possible JVM backend; outside this plan |

Repository sources are linked from the [master plan](00-master-plan.md). The three existing migration/media documents contain valuable historical evidence but are not a complete current feature specification. No production services were queried for this plan, and no new build/test execution is claimed.
