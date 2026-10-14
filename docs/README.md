# Documentation index

Baseline reviewed 2026-10-13 from working-tree sources at `0a365c1e8035f38fe13c358704fb6ab8e2a83e10`, app 10 / 1.10. Step 01 documents existing behavior. Step 02 adds regression tests and narrow supporting seams; [implementation/results](testing/step-02-regression.md) distinguish executed coverage from open cutover/UI/device/service gates.

## Evidence and maintenance

- **S — source-observed:** reachable handlers/resources inspected; not an operator acceptance result.
- **T — test-observed:** assertions exist in test source. Execution status is recorded separately.
- **E — executed:** a command actually ran in this session; see the [baseline results](testing/baseline.md).
- **O — operator-confirmed:** requires a dated walkthrough/reviewer and environment. There are **no O claims** in this baseline.
- **H — historical:** earlier checkpoint reports, not rerun evidence.
- **P — planned:** accepted decisions or proposals, not current application behavior.

Feature tables give stable behavior IDs. Keep IDs when moving documents; add new IDs for new behavior. Quirks and suspected defects are observations pending acceptance, not instructions to perpetuate defects. Preserve Spanish labels exactly, including spelling/case. Put implementation explanations in architecture/testing documents. Use the [feature template](modernization/feature-document-template.md) for future changes; update acceptance references with intentional behavior changes.

## Current operator behavior (S/T)

| Workflow | Document / ID prefix |
| --- | --- |
| Login, connection configuration, reference refresh | [Access and settings](features/access-and-settings.md) — ACCESS |
| Patio and transport entry, save/modify | [Entry inspections](features/entry-inspections.md) — ENTRY |
| Patio and transport exit, save/modify | [Exit inspections](features/exit-inspections.md) — EXIT |
| Damage selection, validation, costs, removal | [Damage recording](features/damage-recording.md) — DAMAGE |
| Container creation/editing, turnos, booking | [Containers and bookings](features/containers-and-bookings.md) — CONTAINER |
| Save/reopen/remove drafts | [Pending inspections](features/pending-inspections.md) — DRAFT |
| Photo/video capture, mixed browsing, deletion/recovery | [Camera and media](features/camera-and-media.md) — MEDIA |
| Dormant photo upload | [Photo uploads](features/photo-uploads.md) — UPLOAD |
| Document search, reports, file viewers | [Searches and exports](features/searches-and-exports.md) — SEARCH |
| Printer setup, receipts, failure | [Printing](features/printing.md) — PRINT |

## Technical baseline and decisions

- [Architecture and integrations](architecture/current-system.md): build identity, screen/mode map, packages, remote operation inventory and current risks.
- [Storage, media and side effects](architecture/storage-and-side-effects.md): durable stores, readers/writers, journals, retention, cutover boundaries.
- [Step-01 testing environment and baseline](testing/baseline.md), [step-02 regression implementation/results](testing/step-02-regression.md), [behavior coverage and acceptance gaps](testing/acceptance.md).
- [ADR 001 — modernization scope](decisions/001-modernization-scope.md), [ADR 002 — storage retirement and media preservation](decisions/002-storage-retirement.md).
- [Master plan](modernization/00-master-plan.md), [step 01 completion/handoff](modernization/01-baseline-and-documentation.md), [step 02](modernization/02-regression-safety-net.md), [accepted storage policy](modernization/storage-version-policy.md).
- [Contributor/agent instructions](../AGENTS.md).

## Historical checkpoints (H)

[Android migration](android-migration.md) records an external-camera checkpoint; [photo gallery](photo-gallery.md) records the photo-only checkpoint; [embedded camera](embedded-camera.md) records CameraX/photo/video checkpoints. Their original validation reports and anchors remain in place. Their old draft/queue deserialization requirements are superseded by ADR 002 for the future cutover. Current source still supports those serialized stores. Historical external artifact paths are provenance, not required setup or newly verified evidence.
