# 03 — Package structure and naming

Status: proposed. Depends on step 02. Outcome: navigable packages with explicit ownership and safe incremental moves.

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

- [ ] Debug/release assembly, targeted tests, and lint pass after each move group.
- [ ] Released draft/upload fixtures are recognized as unsupported old data; new drafts save/reopen after model renames, and corrupt/unknown data has a different outcome.
- [ ] Existing activity/fragment restoration, custom XML inflation, debug hosts, and instrumentation runner resolve correctly.
- [ ] Application ID, file-provider authority and signing identity stay stable. New storage namespaces and intentional retirement ship in the prerequisite cutover PR, not as accidental consequences of package moves.
- [ ] Remaining legacy classes have an explicit reason and destination; naming cleanup is not mistaken for completed architectural separation.

Rollback: revert move-only PRs within the new storage format independently. The pre-cutover APK is not a supported data rollback; use a corrected build that understands current drafts. Preserve compatibility wrappers for Android component names where needed; these are separate from retired draft-model compatibility.
