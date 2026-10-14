# 01 — Baseline and behavior documentation

Status: **documentation exit criteria satisfied, 2026-10-13; operator/device/service acceptance deferred explicitly**. Authorized for step 01 only. Baseline: working-tree source at `0a365c1`, version 10 / 1.10. Outcome: current behavior and modernization constraints documented; no application implementation changes.

## Agreed work sequence and recorded limitations

1. Record the starting commit, SDK/build configuration, device fleet, distribution method, and available test services. Inventory screens, menus, document types, entry/exit modes, editable/read-only modes, and external interactions.
2. Walk through representative workflows with an operator using sanitized data. Mark each statement as confirmed by walkthrough, observed in code/tests, or awaiting confirmation. Capture baseline screenshots with window dimensions in dp, Android version, font scale, and display scaling.
3. Inventory durable state: drafts, photo upload queue, connection settings, reference-list caches, media references, capture/deletion journals, and local video index. Mark old drafts/unused uploads as intentionally retired under the [storage version policy](storage-version-policy.md); distinguish preserved media and current data from rebuildable caches. Record retention/recovery behavior without copying secrets.
4. Reconcile existing documentation. `android-migration.md` describes an external-camera checkpoint; current code and `embedded-camera.md` describe CameraX and video. `photo-gallery.md` predates mixed media. Preserve historical validation as history, with links to current behavior.
5. Record that historical serialization-preservation requirements have been superseded for drafts/unused uploads. Keep released fixtures to test format recognition rather than require successful deserialization. Audit other serialized consumers, especially capture/publication journals, before moving models; do not restore old draft compatibility identifiers merely to preserve abandoned data.
6. Establish the documentation structure below and add a concise root `AGENTS.md` during this phase. It should link to facts and checks, rather than duplicate all feature requirements.

## Established documentation structure

```text
AGENTS.md                         contributor/agent working instructions
docs/
  README.md                       documentation entry point and status guide
  features/                       current observable behavior
    access-and-settings.md
    entry-inspections.md
    exit-inspections.md
    damage-recording.md
    containers-and-bookings.md
    pending-inspections.md
    camera-and-media.md
    photo-uploads.md
    searches-and-exports.md
    printing.md
  architecture/                   package/dependency map and data boundaries
  decisions/                      dated decisions, alternatives, consequences
  testing/                        commands, fixtures, device matrix, acceptance
  modernization/                  this plan and phase status
```

These destinations now exist; architecture/testing/decision files are linked from [docs/README.md](../README.md). Use stable kebab-case filenames and normal Markdown; a documentation generator is unnecessary at this size. The root README now links to `docs/README.md`. Keep existing documents in place initially to preserve links.

Feature documents answer: who can do what, under which conditions, with which visible outcomes; what happens on cancellation, failure, retry, loss of connectivity, or reopening. Use Spanish UI labels verbatim. Keep SQL, class names, libraries, and package diagrams in architecture/testing documents. Give behavior requirements stable IDs so tests and changes can refer to them. Start from the [template](feature-document-template.md).

The root [AGENTS.md](../../AGENTS.md) now covers: app purpose; entry points to feature/architecture docs; tested build commands and environment requirements; how to select relevant tests; the accepted storage-version/retirement policy and media preservation boundaries; fixture/test-service conventions; current package rules; and the requirement to update behavior docs alongside intentional behavior changes. Do not present proposed architecture as already implemented. Keep it brief and change it with the repository.

## Suggested review slices

- Documentation index, baseline inventory, and compatibility register.
- Feature documents, prioritizing inspection submission, damage rules, pending drafts, and media.
- Reconciled historical references and active contributor instructions.

## Exit criteria

- [x] Every primary workflow has a behavior document or a clearly assigned unresolved item.
- [x] Current, proposed, and historical behavior are distinguishable.
- [x] A storage inventory identifies retired formats, recognition/error outcomes, preserved media/journals, and current-data recovery requirements.
- [x] No credentials, real customer records, or machine-only paths are required to understand the docs.
- [x] The baseline commands and manual acceptance limitations are recorded honestly.

Rollback: documentation-only reversions are straightforward; retain any newly discovered compatibility facts in the issue/decision record.

## Completed work and evidence

- [Documentation index](../README.md) and root README link; ten feature documents with stable ACCESS/ENTRY/EXIT/DAMAGE/CONTAINER/DRAFT/MEDIA/UPLOAD/SEARCH/PRINT IDs and acceptance examples.
- [Current system](../architecture/current-system.md) records build/identity, screen modes, actual packages, operation boundaries and direct SQL integration. [Storage/side effects](../architecture/storage-and-side-effects.md) inventories readers/writers, serialized consumers, backups, capture/deletion/publication journals, media index, caches/settings/exports and retirement/preservation decisions.
- [Scope ADR](../decisions/001-modernization-scope.md) and [storage ADR](../decisions/002-storage-retirement.md) record confirmed decisions; no future behavior is represented as implemented.
- [Testing baseline](../testing/baseline.md) records executed commands/results, environment, test suite counts and released fixture headers/hashes. [Coverage/acceptance](../testing/acceptance.md) assigns every primary workflow's unresolved review/test work. Root AGENTS.md gives concise contributor instructions.
- Existing migration/gallery/camera documents remain at their original paths, with original historical bodies/section headings and new current-document/supersession notices; an unrelated concurrent `22` title prefix in photo-gallery.md was retained. Existing fixture files and all build/test/application code remain unchanged.

Evidence is S/T unless execution explicitly appears in testing/baseline.md. No operator-confirmed claims were added. Build: combined command exit 0 (1m 41s); debug/test APK, lint and unsigned release successful with cached tasks identified. Forced JVM execution exit 0 (1m 22s): 18 suites / 94 tests, no failures/errors/skips. Lint: 645 warnings, 7 errors filtered by existing baseline. No device suite was run; adb inspection showed one API 37 emulator only. Documentation links/IDs/Markdown and diff scope were checked locally; see the verification record in testing/baseline.md.

## Deferred work and precise acceptance limits

Work-sequence item 2 (operator walkthrough/screenshots) was not performed. No sanitized authenticated test services, physical inspection/printer device or operator reviewer was supplied; the running API 37 emulator's suitability for destructive local fixture tests was not established. Fleet/distribution/signing ownership remain unknown. These are ACCEPT-01–08 with reviewing roles, required inputs and reproducible verification steps; missing optional context did not prevent source-based documentation.

The documentation exit criterion permits a clearly assigned unresolved item for a workflow; it does not assert operator approval. The five exit criteria above are satisfied **as documentation**, with live/manual behavior acceptance open. Historical reports are preserved as H; no device/update/signing/production success is inferred from them. Machine-specific historical artifact paths remain provenance only; new setup instructions use local environment configuration and relative repository paths. No production service was used and no credentials/customer records were copied into the new documentation.

## Handoff for step 02

Begin with [step 02](02-regression-safety-net.md), the [coverage matrix/acceptance IDs](../testing/acceptance.md) and [baseline commands](../testing/baseline.md). Add deterministic characterization for required/hidden-field binding; line/yard/turno/booking branches; damage validation, costs, repeated total calculation and edit/copy/removal; save/modify numbering/type/day and remote/local partial failures; draft save/reopen and unsupported/corrupt/unknown/I/O classification. The current no-turno initialization guards, empty damage-update callback, zero-tariff fallback and receipt completion dialog need explicit characterization/intent review, not silent fixes.

Use existing JUnit/Robolectric/custom device-runner investment and minimal test seams/fakes. Preserve original released fixture bytes for recognition. Current legacy deserialization tests remain baseline evidence until the deliberate cutover changes their obsolete contract. Step 03 implements full cutover before package renaming; independently audit model-bearing and nested-class journals first. Keep public media/index/publication/deletion recovery protected and dormant uploads inactive. No step-02 tests, seams, CI/build edits, storage implementation or package moves were started in step 01.
