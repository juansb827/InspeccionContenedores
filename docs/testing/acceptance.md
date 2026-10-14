# Behavior coverage and unresolved acceptance

Reviewed 2026-10-13 at `0a365c1`. All feature documents are source-observed (S), with test source (T) where linked; no operator-confirmed (O) behavior. [Fresh JVM/build results](baseline.md) are E at their stated scope. Test presence or helper success does not imply full workflow acceptance.

## Traceability after step 02

| Stable behavior IDs | Existing regression boundary | Gap / next test priority |
| --- | --- | --- |
| ACCESS-001, ACCESS-002, ACCESS-003, ACCESS-004, ACCESS-006 | No dedicated login/settings/synchronization business tests | Fake inspector/connection outcomes, refresh/retry/state; ACCEPT-01 |
| ACCESS-005 | LegacyPhotoImporterTest (6) | Current import unit behavior covered; dedicated SAF device/revocation acceptance and cutover dependency audit; ACCEPT-04/07 |
| ENTRY-001, EXIT-003 | GalleryAlbumsTest/CameraCaptureModelTest cover media mode admission; local instrumentation host covers four combinations historically | FormBindingTest inflates all four information forms and checks hidden keys; actual tab/lifecycle and p1 routing remain GATE-04-01 / ACCEPT-02 |
| ENTRY-002, ENTRY-003, EXIT-001, EXIT-002, CONTAINER-001, CONTAINER-005, CONTAINER-006 | InspectionRulesTest; FormBindingTest required fields | Branch/precedence/bypass tests now executed; actual loader/UI business data remains GATE-04-01 / ACCEPT-02/03 |
| ENTRY-004, ENTRY-005, ENTRY-006, EXIT-004, EXIT-005, EXIT-006 | InspeccionDataSourceTest + InspectionSubmissionTest | Real request/orchestration branches now executed with fake transport; UI/uncertain server outcomes and atomicity remain GATE-04-01/05-01 / ACCEPT-02 |
| DAMAGE-001, DAMAGE-002, DAMAGE-003, DAMAGE-004, DAMAGE-005, DAMAGE-006, DAMAGE-007 | DaniosManagerTest + DamageCostLookupTest; existing album tests | Rule/cost/add/remove/recalculation/copy cases executed; charge-selection UI, empty update callback and video-only warning remain GATE-04-01 / ACCEPT-03 |
| CONTAINER-002, CONTAINER-003, CONTAINER-004 | FormBindingTest date/required binding; InspeccionDataSourceTest linked writes | Real request composition covered; full container form/cancel/check-digit/ISO lookup acceptance remains GATE-04-01/05-01 / ACCEPT-03 |
| DRAFT-001, DRAFT-002, DRAFT-003, DRAFT-004 | StorageCompatibilityTest (6), DraftRecoveryTest (8), media recovery tests | Current serialization/backup/read-after-invalidation covered; full list/save/reopen UI remains ACCEPT-02/07; versioned writer/cutover is GATE-03-01 |
| DRAFT-005 | InspectionSubmissionTest (10) with real local draft cleanup | Timeout/duplicate error, post-save naming/cleanup failure and unrelated-draft preservation executed; AsyncTask/UI/uncertain server effects remain GATE-04-01/05-01 |
| DRAFT-006 (P), UPLOAD-004 (P) | StorageFormatProbeTest (13), unwired preflight only | Encoding/envelope recognition and distinct error outcomes executed; record validation, real versioned recovery, interrupted/repeated cutover/no replay remain GATE-03-01/02 / ACCEPT-07 |
| MEDIA-001, MEDIA-002 | CameraCaptureFragment/Model, CaptureCoordinator, MediaPhotoStore, ImageProcessor, PhotoNames tests | Retain existing tests; real OEM capture/permission/publication/recovery gate; ACCEPT-04 |
| MEDIA-003, MEDIA-004 | CameraCaptureModel, VideoCaptureCoordinator, MediaVideoStore tests | Limits/finalization unit coverage; physical full-duration/size/microphone acceptance absent; ACCEPT-04 |
| MEDIA-005, MEDIA-006, MEDIA-007 | GalleryAlbums/PhotoGalleryFragment/GalleryDeletionModel/LocalMediaIndex tests | Retain ordering/admission/metadata-retry tests; dedicated device playback/deletion/recreation; ACCEPT-04 |
| MEDIA-008, UPLOAD-001 | LocalMediaIndex, StorageCompatibility, VideoCaptureCoordinator tests; dormant starts inspected | Public retention helper evidence; full save/clear/update/journal compatibility and no replay gate; ACCEPT-04/07 |
| UPLOAD-002, UPLOAD-003 | UploadQueueTest (3), dormant service inspected | Not active feature acceptance; no FTP restoration required |
| SEARCH-001, SEARCH-002, SEARCH-003, SEARCH-004, SEARCH-005 | FileConsumersTest (5) covers helpers/replacement, not real query/UI | Container/direction/day, date-loading error, report columns/limit/empty/viewer fixtures; ACCEPT-06 |
| PRINT-001, PRINT-002, PRINT-003, PRINT-004, PRINT-005 | PrinterPermissionTest (6), PrinterTransportTest (4) | Transport gate covered; selection/receipt variants/confirmation/partial paper output not accepted; ACCEPT-05 |

Counts identify related suites, not per-ID test counts. Step 01 added no tests; step 02 added 80 in eight new suites and executed all 174 tests. [Implementation/evidence and gates](step-02-regression.md) distinguish actual helpers, SQL request tests, unwired preflight and unverified UI/device/service paths. Stable observations about suspected defects must be characterized separately from desired fixes; do not lock an unintended bug into the permanent business specification without review.

## Acceptance register

All manual/operator/service items below remain open. Automated coverage now narrows the gaps as shown above; it does not close operator acceptance. Owner is a required reviewing role, not a claim that someone has accepted work. Independent documentation and fake/local step-02 tests can proceed while services/devices are unavailable.

| Item | Owner / missing input | How to verify and what to record |
| --- | --- | --- |
| ACCEPT-01 — access/settings | Operator + test-service owner; sanitized enabled/disabled/absent inspectors and isolated connection | Exercise numeric/invalid/missing/out-of-range login, failed/edited connection, save-anyway cancel, cache refresh and reconnect. Record messages, enabled controls and restart/session behavior; never copy credentials. |
| ACCEPT-02 — inspections/drafts | Patio/transport operator + service owner; sanitized EP/ET entry/exit, same/prior-day docs, yard/line states and turnos | Walk all four new modes plus view/modify/resumed draft. Resolve contradictory turno guards, required/hidden fields and date loading. Verify save/modify document IDs/detail effects; inject reported failure, duplicate tap/key, uncertain commit, local naming/cleanup failure, restart and explicit draft/exit. Record expected versus actual effects and preserve uncertain outcomes for inspection, not automatic resubmit. |
| ACCEPT-03 — damage/container/booking | Tariff/business owner; known rates, size/dimension methods, IVA, empty/membership/exhausted bookings and container edits | Compare deterministic rate/cost examples (including blanks/zero), add/remove/recalculate and copy/update actions. Decide intended zero-cost fallback/update/rounding/check-digit behavior. Verify code-change turno effects, absent booking and transport bypass. Document accepted corrections separately. |
| ACCEPT-04 — media/devices | Device owner; dedicated emulator/physical device with sanitized local fixtures | Run existing gallery/camera/video suites, then actual inspection albums, denials/revocation/settings retry, interrupted capture, process death, rotation, video-only editing albums, deleted bytes/failed metadata retry. Verify public gallery retention, long-duration/200 MB limits and physical mic/video quality. Record OS/model/window dp/font/display scale and screenshots only when actually captured. |
| ACCEPT-05 — printing | Printer/operator owner; dedicated paired printer and sanitized receipts | Test unsupported/off/denied/revoked Bluetooth, paired selection, connection/command failure and partial receipt. Verify all EP/ET entry/exit buttons, receipt values/layout and final confirmation/form clear. Record physical output and safe explicit retry behavior. |
| ACCEPT-06 — reports/search | Reporting owner; disposable SQL data and installed/missing viewers | Search invalid/missing container, entry/exit view/modify, service-date failure and line cancellation. Verify stock/day columns, empty results, 1000-row boundary, export replacement failures and viewer grants. PDF helper is not a claimed active operator flow. |
| ACCEPT-07 — storage/update | Storage + signing owner; sanitized stores/journal stages and dedicated update device | Step 02: recognition/error fixtures and fake current-store/cutover contracts. Step 03 before rename: inventory `.bak`/staging/marker states, preserve public bytes/index/publication/deletion journals, isolate old snapshots without stale replay, restart repeatedly and save/reopen new drafts. Later perform in-place signed update using same identity; no uninstall/clear-data substitute. Roll forward through corrected current-format build; downgrade compatibility not promised. |
| ACCEPT-08 — operating environment/layout | Deployment/operator owner; device fleet, distribution/signing ownership, supported OS and reviewer | Establish actual distribution/certificate owner without publishing secrets. Agree portrait/resizing/keyboard widths/font scales and capture baseline screens with dimensions. Target 320–840dp and large text is a proposed layout matrix for step 06, not device evidence or current fleet support. |

No test SQL/FTP service configuration, physical inspection/printer device, operator walkthrough or deployment owner was supplied for this session. A connected API 37 emulator exists, but no dedicated test-data suitability was established. These gaps are assigned here, not represented as failed application tests or claimed operator acceptance.

## Proposed device matrix / evidence separation

| Environment | Why needed | Step-01 evidence |
| --- | --- | --- |
| API 29 dedicated emulator/device | Minimum installable platform, MediaStore and local suite | H API 29 checkpoint reports only; no new run |
| API 30 and API 31+ printer/device | Pre/post Bluetooth runtime permission behavior | E JVM historical/API 30/31 helper cases; no physical print |
| API 34 dedicated emulator/device | Existing camera/gallery/video checkpoint matrix | H only; no new run |
| API 37 connected emulator | Host available; future OS compatibility is not established | E read-only environment listing only |
| Fleet physical device, mic, paired printer | OEM/quality/receipt/service acceptance | None |
| Agreed width/font/keyboard/resizing matrix | Operator screenshots and later responsive layouts | None; proposed by master plan |

Historical reports are [android migration](../android-migration.md), [photo gallery](../photo-gallery.md), [embedded camera/video](../embedded-camera.md). They retain earlier claims/artifact provenance; their old draft/queue import/downgrade assertions are superseded at cutover by [ADR 002](../decisions/002-storage-retirement.md).

## Next session: step 03 prerequisite cutover, then packages

Use the [step-02 handoff/gates](step-02-regression.md) and [step 03](../modernization/03-package-structure.md). Implement the real separate versioned store/cutover and reader/writer/notice/error behavior before persisted-model renames. Preserve released fixture bytes and active media/printing tests; only at deliberate cutover replace obsolete deserialization expectations with recognition/error contracts. Cover record validation, old/newer/corrupt/unknown/I/O data, interrupted isolation/markers, new-draft reopen and no queue replay (GATE-03-01/02). Separately audit all model-bearing/nested-class journals, public media/index/publication/deletion state and Android extras. The preflight's SUPPORTED_SCHEMA is not payload validation or an application cutover. Keep SQL integration for GATE-05-01 and full UI/operator/device acceptance open. No step-03 writer, quarantine/reset, package move or queue retirement was implemented in step 02.
