# 02 — Regression safety net

Status: proposed. Depends on step 01. Outcome: important behavior is protected before broad refactoring.

## Starting point

Extend the existing JUnit 4/Robolectric tests and debug-only camera/gallery instrumentation. Do not replace this investment just to adopt a different testing library. The current `RecibosTest` under production sources is not evidence of automated test coverage. Historical Robolectric API 22/28 cases remain migration regression fixtures; installable support starts at API 29.

First reproduce the baseline checks using a configured compatible JDK and Android SDK:

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:assembleDebugAndroidTest
```

The existing docs record a local JBR 21 setup and extra heap for the video graph. Make environment setup portable in testing docs; do not hardcode one developer's home directory into CI. Building the test APK does not execute device tests. Preserve the existing custom runner and `suite` selections until an explicit runner migration is justified.

## Work sequence and initial test priorities

1. Run the baseline, record failures before changing code, and distinguish environment failures from behavior failures. Preserve `released-d9c52d3` serialized fixtures.
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
| Pending drafts | Save, replace by stable identity, reopen, corrupt file, interrupted replacement | Existing storage fixtures plus new workflow tests |
| Submission failure | Duplicate tap, duplicate document, timeout, uncertain commit; draft retained | Submission coordinator/repository contract |
| Permissions/read-only state | Actions blocked in handlers as well as visually; retry/cancel preserves work | Screen/controller tests |
| Media and printing | Existing camera/video/gallery recovery, partial photo upload, printer failure | Retain existing tests; add only uncovered changed behavior |

Use real SQL Server for dialect, function, collation, and transaction integration tests; SQLite/H2 and mocked SQL strings cannot establish those properties. Add that environment as part of step 05 if it is not available yet.

## Review slices and exit criteria

Suggested PRs: baseline/check runner; business-rule fixtures; submission/form-binding contracts. Measure coverage to locate gaps if useful, but do not set a cosmetic global percentage as the completion criterion. Test behavior and failure boundaries, not getters or exact internal call sequences. Local and instrumented tests serve different scopes. [Android testing fundamentals](https://developer.android.com/training/testing/fundamentals)

- [ ] Baseline checks run reproducibly, with existing failures explicitly documented.
- [ ] Every high-risk behavior about to be refactored has a meaningful regression test or an explicit integration gate before its change.
- [ ] Tests run without production SQL Server, FTP, printer, or credentials.
- [ ] Existing storage/media compatibility tests remain active.
- [ ] CI/local reports distinguish JVM, database integration, emulator, and physical-device evidence.

Rollback: revert new harness changes independently of behavior fixtures. A disabled/failing test is an unresolved item, not a passing phase.
