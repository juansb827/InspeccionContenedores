# 07 — Lifecycle, asynchronous work, and screen state

Status: proposed. Depends on step 06. Outcome: background work, navigation, and recreation cannot lose edits or accidentally repeat actions.

## Scope and sequence

1. Inventory `AsyncTask`, loaders, static mutable fields, `MainActivity.context`, activity references, dialogs, and view access from workers. Reuse lessons and existing patterns from the newer camera/gallery ViewModels rather than replacing them wholesale.
2. Pilot the inspection information/damage workflow with a screen-scoped ViewModel, observable UI state and injected repositories. For this Java application, LiveData plus managed executors/callbacks is a practical starting proposal. Evaluate additional libraries only when an actual need appears.
3. Model states explicitly: editing, loading reference data, validation error, saving, confirmed saved, failed and outcome unknown. Make repeated taps, tab changes, Back and leaving the screen well-defined while work is active. Preserve existing restrictions around capture/deletion/printing.
4. Keep View references and Activity contexts out of long-lived state. Observe using the Fragment view lifecycle and clear binding references when its view is destroyed. Post UI changes on the main thread. Scope cancellation and callback delivery so old screen instances cannot overwrite current state.
5. Store small restoration keys such as inspection/album IDs, active tab, selected damage and pending operation identity in saved state. Persist durable edits/recovery records through the draft adapter. Do not put large images, media bytes, or entire growing object graphs into Bundles.
6. Migrate login, lookup/search, pending and settings work in separate slices. Confirm cancellation actually stops or safely ignores the underlying work; do not equate abandoning a callback with canceling a server commit.

A ViewModel preserves state across configuration changes; it is not durable storage across process death. Saved state is for restoration, and current-format drafts/journals protect durable work. Unsupported pre-cutover snapshots follow the storage policy; stale serialized extras must not crash a new screen. Process-death tests must actually recreate the process/state boundary rather than only rotating an Activity. [Android UI state guidance](https://developer.android.com/topic/libraries/architecture/saving-states)

WorkManager is a future option for explicitly requested persistent background jobs. Do not introduce it merely to replace every AsyncTask or activate the dormant upload service. Printing and uncertain submissions cannot be blindly replayed by a worker.

## Required scenarios

- Start a lookup, rotate/resize, and deliver its response: only the current screen is updated.
- Edit a draft, navigate through camera/gallery and return: the same inspection/album/damage remains associated.
- Submit then lose connectivity or the process: restore a recoverable operation status; do not duplicate the write.
- Leave while a validation/lookup error is pending: no stale dialog or callback touches a destroyed view.
- Deny/revoke camera, microphone or printer permission: preserve edits and existing retry/cancel behavior.
- Receive a completion twice: local association/acknowledgement is applied once.

## Review slices and exit criteria

Suggested PRs: inspection ViewModel/state model; restoration and submission recovery; remaining screens; removal of obsolete AsyncTasks/static owners only after consumers migrate.

- [ ] Migrated screens hold no Activity/View in long-lived workers or models.
- [ ] Layout changes, navigation and real process restoration preserve documented work.
- [ ] Repeated callbacks/taps cannot duplicate saves, media association or printing.
- [ ] Long operations remain off the main thread and their busy/error states are visible.
- [ ] Every remaining legacy background path has an owner and documented reason.

Rollback: retain repository contracts and durable formats; revert one screen controller at a time. Do not clear recovery journals to make the previous implementation appear functional.
