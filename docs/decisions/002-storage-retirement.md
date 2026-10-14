# ADR 002 — Retire old drafts/unused uploads; preserve public media

Date: 2026-10-13. Status: accepted decision, **not implemented**. Authority: user's confirmed decisions and the current [storage version policy](../modernization/storage-version-policy.md), which is the full normative contract.

## Decision

Backward-compatible reading/import of old drafts and the unused photo queue is not required at the deliberate future cutover. Positively recognized retired storage must be excluded with an understandable unsupported-format notice, while new drafts work in a separate current store. Never infer exact originating app versions or individual lost-draft counts from an old collection.

Format/schema version is independent of app version. Supported data from another writer app version opens normally. Distinguish missing data, supported valid data, recognized retired formats, unsupported newer formats, corruption, unknown formats and I/O failures. Preserve unrecognized/damaged/newer data and failed-write state; do not silently convert these outcomes to an empty store or incompatibility. Legacy location plus serialization signature recognizes encoding without deserializing model classes; generic deserialization exceptions or filenames alone are insufficient.

Public photos/videos and independent index/publication bookkeeping are preserved. Associations existing only in abandoned drafts need not be recovered; never guess them into a new draft. Capture/deletion/publication journals require separate assessment: some contain old model snapshots and must be preserved/isolated visibly with stale replay prevented. Retirement is not blanket deletion permission.

## Alternatives and consequences

Keeping every old model/class identifier or building an importer would protect abandoned draft/queue compatibility beyond the accepted requirement. Blanket reset would destroy media/recovery evidence and misclassify faults. Both alternatives are rejected.

An explicit atomic, package-independent versioned format is required; JSON envelope is the initial proposal, Room remains a later decision. Isolate recognized originals/quarantine and atomically persist classification/cutover/notice state; failure to do so is an I/O problem, not successful retirement. Do not replay the queue or block saving for unavailable uploads. Physical cleanup requires a separate explicit action. Recovery is roll-forward; a pre-cutover APK is not a safe automatic downgrade.

## Sequencing and evidence

Current source still deserializes both collections and assigns missing media IDs. Existing compatibility tests characterize this baseline; no version envelope/cutover notice has been added here. Step 02 preserves released fixture bytes and adds recognition/error/recovery contracts. Step 03 implements cutover before renaming persisted models; step 08 may change storage engine without reintroducing old draft import.

Read the [storage inventory](../architecture/storage-and-side-effects.md) and [testing gates](../testing/acceptance.md) for affected consumers, backups and journal formats. The original historical documents remain evidence of earlier choices, with supersession notices added instead of removing history.
