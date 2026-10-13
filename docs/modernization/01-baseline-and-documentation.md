# 01 — Baseline and behavior documentation

Status: proposed. Prerequisite: agreement to begin implementation. Outcome: a shared description of what the app does and what must survive modernization.

## Work sequence

1. Record the starting commit, SDK/build configuration, device fleet, distribution method, and available test services. Inventory screens, menus, document types, entry/exit modes, editable/read-only modes, and external interactions.
2. Walk through representative workflows with an operator using sanitized data. Mark each statement as confirmed by walkthrough, observed in code/tests, or awaiting confirmation. Capture baseline screenshots with window dimensions in dp, Android version, font scale, and display scaling.
3. Inventory durable state: drafts, photo upload queue, connection settings, reference-list caches, media references, capture/deletion journals, and local video index. Mark old drafts/unused uploads as intentionally retired under the [storage version policy](storage-version-policy.md); distinguish preserved media and current data from rebuildable caches. Record retention/recovery behavior without copying secrets.
4. Reconcile existing documentation. `android-migration.md` describes an external-camera checkpoint; current code and `embedded-camera.md` describe CameraX and video. `photo-gallery.md` predates mixed media. Preserve historical validation as history, with links to current behavior.
5. Record that historical serialization-preservation requirements have been superseded for drafts/unused uploads. Keep released fixtures to test format recognition rather than require successful deserialization. Audit other serialized consumers, especially capture/publication journals, before moving models; do not restore old draft compatibility identifiers merely to preserve abandoned data.
6. Establish the documentation structure below and add a concise root `AGENTS.md` during this phase. It should link to facts and checks, rather than duplicate all feature requirements.

## Proposed documentation structure

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

These are proposed destinations, not files claimed to exist today. Use stable kebab-case filenames and normal Markdown; a documentation generator is unnecessary at this size. Update the root README to link to `docs/README.md` when it exists. Keep existing documents in place initially to preserve links.

Feature documents answer: who can do what, under which conditions, with which visible outcomes; what happens on cancellation, failure, retry, loss of connectivity, or reopening. Use Spanish UI labels verbatim. Keep SQL, class names, libraries, and package diagrams in architecture/testing documents. Give behavior requirements stable IDs so tests and changes can refer to them. Start from the [template](feature-document-template.md).

Proposed `AGENTS.md` contents: app purpose; entry points to feature/architecture docs; tested build commands and environment requirements; how to select relevant tests; the accepted storage-version/retirement policy and media preservation boundaries; fixture/test-service conventions; current package rules; and the requirement to update behavior docs alongside intentional behavior changes. Do not present proposed architecture as already implemented. Keep it brief and change it with the repository.

## Suggested review slices

- Documentation index, baseline inventory, and compatibility register.
- Feature documents, prioritizing inspection submission, damage rules, pending drafts, and media.
- Reconciled historical references and active contributor instructions.

## Exit criteria

- [ ] Every primary workflow has a behavior document or a clearly assigned unresolved item.
- [ ] Current, proposed, and historical behavior are distinguishable.
- [ ] A storage inventory identifies retired formats, recognition/error outcomes, preserved media/journals, and current-data recovery requirements.
- [ ] No credentials, real customer records, or machine-only paths are required to understand the docs.
- [ ] The baseline commands and manual acceptance limitations are recorded honestly.

Rollback: documentation-only reversions are straightforward; retain any newly discovered compatibility facts in the issue/decision record.
