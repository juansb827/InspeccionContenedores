# 03 — Package structure and naming

Status: implemented with active-serialization exceptions; automated storage/package gates verified, operator/signed-update acceptance open. Step 04 is unstarted. [Executed evidence](../testing/step-03-cutover-and-moves.md) and [move inventory](../architecture/package-moves.md) are authoritative for delivered work.

## Proposed structure

Keep `app/src/main/java/com/juans/inspeccion`, `res`, `test`, and `androidTest`. Within the existing Java root, use lowercase packages, feature grouping for screens, and separate data/platform responsibilities:

```text
com.juans.inspeccion/
  app/                            dependency construction and application setup
  ui/
    access/  settings/  inspection/  containers/  pending/  search/
    media/camera/  media/gallery/  printing/
    common/                       genuinely shared widgets/dialogs
  domain/
    model/                        new business values, independent of Views
    rules/                        validation and calculations worth isolating
  data/
    repository/                   public operation contracts and implementations
    remote/sqlserver/             JDBC adapter for direct SQL Server access
    local/                        draft/cache adapters; optional Room later
    mapping/                      storage/remote records ↔ business values
  platform/
    media/  printing/  export/     Android/device integrations
  Mundo/                          unmigrated classes only; retire incrementally
```

Do not create empty packages or a use-case class for every method. `domain` stays small; it is not a requirement to introduce a full architectural framework. Keep one Gradle module until a concrete build/team/dependency problem warrants modularization.

## Initial mapping

| Current location | Target / exception |
| --- | --- |
| `Interfaz` activities and `Inspeccion_tabs/p1`, `p2` | Feature packages under `ui`; rename `p1`/`p2` to descriptive information/damage fragment names after compatibility review |
| `Interfaz/Camera`, `Interfaz/Gallery` | `ui/media/camera`, `ui/media/gallery` |
| `Interfaz/Dialogs`, `CustomView` | Feature-owned dialogs when local; `ui/common` only when shared |
| `Mundo/DAO`, query/data sources | `data/remote/sqlserver`; keep existing responsibilities initially, split in later steps |
| `Mundo/Storage` | Draft adapters in `data/local`; device media APIs in `platform/media`, classified individually |
| `Mundo/Recibos` | Receipt formatting separated from `platform/printing` and `platform/export` as needed in step 04 |
| `Mundo/Album`, `Inspeccion`, `Danio`, `DaniosManager` | Move/rename by responsibility after the explicit draft/queue cutover and journal audit; no old-draft compatibility wrappers required |
| `Other`, `Varios`, broad managers | Classify each usage; no new general-purpose `utils` dumping ground |

## Work sequence

1. Build a move manifest covering imports, XML custom-view names, manifest components, fragment restoration names, explicit intents, debug hosts, test runner names, reflection, and persisted/saved-state objects.
2. Before renaming persisted models, implement the [storage version policy](storage-version-policy.md) in a separate prerequisite PR: recognize retired draft/queue formats without deserializing their objects, isolate them, show a notice, and write new drafts in a versioned format independent of Java class names. Audit every serialized consumer, including media journals and saved-state extras; the accepted break applies to drafts/unused uploads, not arbitrary media deletion or unhandled restoration crashes. Do not retain old classes solely to open abandoned drafts. Matching `serialVersionUID` alone would not fix class renames under the old format. [Java serialization versioning](https://docs.oracle.com/en/java/javase/17/docs/specs/serialization/version.html)
3. Move a small low-risk group, update references, compile, and run related tests. Keep move-only diffs separate from logic changes. Prefer keeping manifest entry classes as thin stable wrappers when launch/component compatibility would otherwise change; an activity alias can preserve a launcher component when appropriate.
4. Repeat by feature. Keep resource filenames/IDs stable during package moves; Android resources remain in their standard flat/qualified directories. Tests mirror the new package names, while released fixture bytes stay untouched.
5. Publish the dependency map and a temporary exception list for remaining legacy cycles. A move does not by itself fix dependencies; step 04 removes the coupling.

## Review slices and exit criteria

Suggested PRs: format recognition and current-store cutover; low-risk UI helpers; one complete feature; data/platform groups; architecture map and exception inventory. Keep the intentional storage behavior change separate from subsequent move-only diffs.

- [x] Compile/focused checks pass after UI/media/data groups; fresh debug/test APK, JVM/lint and unsigned release assembly pass at storage/final milestones. This follows the session's milestone verification scope rather than claiming release/lint ran separately after every individual class move.
- [x] Released draft/upload fixture bytes are retained and recognized as retired; current JSON drafts save/reopen across package moves; older/newer/corrupt/unknown/I/O outcomes differ. Persisted models retain their identities because active journal/Bundle contracts still require them; no model-rename verification is claimed.
- [x] Original/new registered entries, original fragment names, operator form/custom XML, debug host and configured runner resolve; local camera/gallery recreation is exercised on dedicated API 34. This does not claim authenticated information/damage-screen restoration or an upgrade from a deployed signed APK.
- [x] Application ID, provider authority, resource IDs/layout structure, version and signing configuration remain unchanged. Deliberate storage namespaces/retirement belong to the separately reviewable storage slice.
- [x] Remaining classes/cycles have concrete serialization or Android-entry reasons and future destinations; no architecture purity is claimed.
- [ ] Operator/fleet review of notices/resource capacity, authenticated saved-task restoration and in-place deployed signed-update acceptance (ACCEPT-02/04/07/08). These remain separate acceptance gates, not passing test claims.

## Delivered work and review separation — 2026-10-13

The [atomic schema/cutover](../architecture/draft-schema-v1.md) is implemented, including strict record/encoding/bounds validation, durable notice, legacy candidate isolation, retired queue controls and staging-only recovery preservation. Old snapshots never replay; public media/index/publication/import bookkeeping and active serialized class identities remain intact. JVM cutover/notice tests exercise real storage and interruption boundaries, replacing only superseded legacy deserialization/replay expectations.

The inventory covers 84 production moves in groups of 31 UI, 18 media and 35 data/platform classes, with 22 stable main-source Android/fragment bridges plus the configured test runner and debug host bridges. `p1`/`p2` become `InspectionInformationFragment`/`InspectionDamageFragment`. Retained legacy models are active serialization exceptions, not abandoned-draft compatibility.

Session review patches separate storage from package-only code changes. The two late staging-reader guards and four-byte legacy recognition are assigned to the storage slice; reversing class/package names and imports yields identical bodies for all 84 moved production classes. Seven existing lint records only relocate file paths. See [results](../testing/step-03-cutover-and-moves.md) for fresh/cache/failure distinctions and device limits.

## Handoff for step 04

Reproduce the current full checks, then pilot typed inspector lookup using `data.remote.sqlserver.UsuarioDataSource`, with found/missing/disabled/connection outcomes and sanitized fakes. Extract plain form inputs from `ui.common.Formularios` and the two inspection fragments, preserving hidden/business keys, date conversion, read-only/modification behavior and all characterized calculations/branches. Introduce only the needed operation contracts around existing SQL adapters; direct SQL Server and one module remain.

Keep schema 1, notice state, isolation/no-replay and current media recovery tests. Remove widget Serializable transport deliberately before moving MyEditText/GoneView/interface. Audit pinned implicit UIDs before changing model signatures; replace active snapshot/publication/deletion/cache and Android-state contracts or supply tested handling before their class renames. DraftCodec reflection is a temporary explicit legacy adapter, not a reason to duplicate business calculations. Do not fix accumulated totals, null-error ambiguity, turno guards or other characterized quirks without separate expected-behavior decisions. SQL dialect/atomicity/concurrency need isolated step-05 integration, never mock claims.

Rollback: revert move-only PRs within the new storage format independently. The pre-cutover APK is not a supported data rollback; use a corrected build that understands current drafts. Preserve compatibility wrappers for Android component names where needed; these are separate from retired draft-model compatibility.
