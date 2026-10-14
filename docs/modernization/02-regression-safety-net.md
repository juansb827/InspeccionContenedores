# 02 — Regression safety net

Status: **regression implementation delivered 2026-10-13; four exit criteria satisfied, future cutover/recovery criterion partially open**. Depends on step 01. Outcome: 80 additional JVM tests and narrow supporting seams; no package move, SQL rewrite, storage cutover or behavior fix.

## Starting point

Extend the existing JUnit 4/Robolectric tests and debug-only camera/gallery instrumentation. Do not replace this investment just to adopt a different testing library. The current `RecibosTest` under production sources is not evidence of automated test coverage. Historical Robolectric API 22/28 cases remain migration regression fixtures; installable support starts at API 29.

First reproduce the baseline checks using a configured compatible JDK and Android SDK:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleDebugAndroidTest
```

The existing docs record a local JBR 21 setup and extra heap for the video graph. Make environment setup portable in testing docs; do not hardcode one developer's home directory into CI. Building the test APK does not execute device tests. Preserve the existing custom runner and `suite` selections until an explicit runner migration is justified.

## Work sequence and initial test priorities

1. Run the baseline, record failures before changing code, and distinguish environment failures from behavior failures. Preserve `released-d9c52d3` fixture bytes for old-format recognition tests. Existing deserialization tests remain baseline evidence until the deliberate cutover PR replaces their superseded expectations.
2. Create a behavior-to-test matrix with the cases below. Add characterization tests: tests recording important existing outcomes, including intentional quirks. Suspected defects get separate proposed corrections rather than becoming permanent requirements.
3. Add the smallest seams needed to test logic without a live database or device. Keep those changes local; the full repository architecture comes in step 04. If a behavior cannot yet be isolated, cover it at the nearest feasible integration boundary and record the gap.
4. Make fast checks repeatable locally and in the repository's selected CI system. Add a separate opt-in test-service/device suite; default tests must not discover or connect to production settings.

| Area | High-value scenarios | Appropriate test boundary |
| --- | --- | --- |
| Damage validation | Missing location/component/type/method/quantity; dimension-dependent requirements; container-size-dependent behavior | JVM/Robolectric around existing rules, then plain Java after extraction |
| Damage costs | Value + labor + tax; blank values; rounding/formatting observations; edit/remove/recalculate | Deterministic fixtures around `DaniosManager`; do not silently change float semantics |
| Form binding | All visible and hidden fields map to the same business keys; date conversion; editable/read-only modes | Robolectric inflation/binding tests |
| Inspection save/modify | Entry vs exit; with/without turno; document-number preservation; header/details atomicity | Fake repository first, isolated SQL Server integration in step 05 |
| Containers/bookings | Membership/balance checks; container-code changes and associated turno update | Recorded fixtures and operation contracts |
| Pending drafts | Current-format save/reopen; unsupported old/newer schemas; corruption/I/O failure; interrupted cutover/replacement | Old-format detection fixtures and new workflow/storage tests |
| Submission failure | Duplicate tap, duplicate document, timeout, uncertain commit; draft retained | Submission coordinator/repository contract |
| Permissions/read-only state | Actions blocked in handlers as well as visually; retry/cancel preserves work | Screen/controller tests |
| Media and printing | Camera/video/gallery recovery, public media preservation, printer failure; retired upload queue cannot replay or block saving | Retain active media/printing tests; replace obsolete upload compatibility expectations at cutover |

Use real SQL Server for dialect, function, collation, and transaction integration tests; SQLite/H2 and mocked SQL strings cannot establish those properties. Add that environment as part of step 05 if it is not available yet.

## Review slices and exit criteria

Suggested PRs: baseline/check runner; business-rule fixtures; submission/form-binding contracts. Measure coverage to locate gaps if useful, but do not set a cosmetic global percentage as the completion criterion. Test behavior and failure boundaries, not getters or exact internal call sequences. Local and instrumented tests serve different scopes. [Android testing fundamentals](https://developer.android.com/training/testing/fundamentals)

- [x] Baseline checks run reproducibly, with existing failures explicitly documented.
- [x] Every high-risk behavior about to be refactored has a meaningful regression test or an explicit integration gate before its change.
- [x] Tests run without production SQL Server, FTP, printer, or credentials.
- [ ] Old-format recognition, distinct corruption/I/O errors and new-draft recovery are covered; active media/printing tests remain. Retired draft/queue deserialization requirements are deliberately replaced, not disabled to hide unrelated failures.
- [x] CI/local reports distinguish JVM, database integration, emulator, and physical-device evidence.

Rollback: revert new harness changes independently of behavior fixtures. A disabled/failing test is an unresolved item, not a passing phase.

## Implementation and measured results — 2026-10-13

[Full implementation/evidence/coverage](../testing/step-02-regression.md) records eight new suites (80 tests), protected SQL/metadata/date seams, tariff lookup, source-characterized rules and save/local-cleanup work. The existing SQL builders and persisted model signatures remain; original fixture bytes and all 94 existing tests were preserved. An unwired format probe tests legacy signature/location, proposed old/supported/newer JSON envelope schemas and corruption/unknown/I/O distinctions. It does not deserialize models, validate record semantics, switch writers, isolate/reset data or retire uploads.

Fresh pre-change baseline: 18 suites / 94 tests, zero failures/errors/skips; debug/test APK/lint successful in 2m 56s, 74 tasks executed. Final `./scripts/check.sh --fresh`: **26 suites / 174 tests, zero failures/errors/skips**, fresh debug/test APK and lint successful in the final reviewed 2m 34s run, 74 tasks executed. Lint remained **645 warnings with 7 errors filtered by the unchanged baseline**. Initial development compile/fixture failures were corrected, not disabled or hidden. The final reviewed unsigned release assembly passed in 51s (8 tasks executed, 36 up-to-date); no deployment/signing/upgrade acceptance is inferred. No operator/device/service workflow or hosted CI was executed.

The portable local runner supports fresh or focused checks and reports JVM XML separately from device/service/coverage evidence. A GitHub Actions workflow now runs that runner and retains build/lint/JVM reports without business credentials; only local syntax/configuration validation is claimed. Existing custom instrumentation runner/suite commands remain unchanged.

Characterization includes repeated total accumulation, zero-tariff/blank-quantity fallback, booking bypass/message precedence, copy without photos, month 00, empty hidden-field destination, modify-date conversion, linked update without turno, repeated submission and null-message error ambiguity. These are test-observed quirks awaiting business review, not approved fixes.

## Open exit criterion and authorized handoff

The storage criterion remains unchecked: recognition/error preflight and current serialized draft recovery are covered, but real future versioned draft save/reopen, interrupted/repeated cutover, queue retirement/no replay, notices and deliberate replacement of deserialization expectations cannot run before their step-03 implementation. This is an explicit sequencing deferral under the accepted storage policy, not a fake test counted as a migration pass.

Step 03 may start with **GATE-03-01/02 prerequisite cutover only**: define/validate real records and encoding limits, implement a separate atomic current namespace and durable classification/notice/marker state, exercise all failure/restart stages and new-store recovery, prevent queue replay/blocking, and independently protect/isolate snapshot and publication/deletion/index/import journals. Preserve original fixture bytes; replace only obsolete deserialization expectations at that deliberate change. Do not rename persisted models until those gates pass. Then use GATE-03-03 for small move groups/Android restoration, keeping application identity/provider/runner stable and re-running appropriate tests/build/lint/device checks.

Before business/form/receipt changes, GATE-04-01 still requires real handler/lifecycle characterization with sanitized fixtures. Real SQL Server dialect/atomicity/concurrency/uncertain commit evidence is GATE-05-01. Manual ACCEPT-01–08 remain open; no unit/fake result closes them. No step-03 implementation was begun here.
