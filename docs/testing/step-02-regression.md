# Step 02 regression safety net — implementation and evidence

Reviewed 2026-10-13 against working-tree source at `0a365c1` plus the uncommitted step-01 documentation. Step 02 adds tests and narrow supporting seams. Java/XML, module/dependencies, SQL builders, layouts, serialized writers and Android components remain in place. No production services/settings, operator/device walkthrough, printer, camera or SQL Server integration was used.

## Fresh baseline and final checks

Before production changes, the documented JBR 21.0.11 / Android SDK environment ran:

```sh
./gradlew --no-daemon \
  -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' --max-workers=2 \
  :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug --rerun-tasks
```

Baseline: exit 0, `BUILD SUCCESSFUL in 2m 56s`, 74 tasks executed; **18 suites / 94 tests / zero failures, errors or skips**. Lint: **645 warnings, 7 errors filtered by the unchanged baseline**. This forced task execution, not an empty dependency cache. The image-decoder “unimplemented” diagnostic and Java 8/deprecation warnings remained; they were not failing tests or environment blockers.

The initial targeted development run found a missing static constant import in the extracted tariff method (compile failure, corrected), then two new SQL-request assertions failed because synthetic schema fixtures used `varchar` instead of the app's dictionary code `C`. Fixing the test fixtures left SQL generation unchanged. No application calculation/validation defect was fixed and no test was disabled.

A subsequent targeted run through the new local runner executed **84 tests in 9 suites**, zero failures/errors/skips: the first 78 added tests plus the original six StorageCompatibilityTest cases. Two more focused boundary cases (trailing JSON garbage and repeated loaded-damage recalculation) were then added for the final full run.

Final reviewed `JAVA_HOME=<configured JDK> ./scripts/check.sh --fresh`: exit 0, `BUILD SUCCESSFUL in 2m 34s`, all **74 tasks executed**; **26 suites / 174 tests / zero failures, errors or skips**. Debug assembly, instrumentation APK assembly and lintDebug passed. Lint still reported **645 warnings and 7 baseline-filtered errors**; the baseline was not broadened. No device suite was executed. The earlier full run also passed (2m 50s, 74 tasks executed). After review replaced duplicated SQL-list logic in the transport double with the original DAO method plus injected metadata, the final 2m 34s run above passed all 174 tests again. Unsigned release assembly before that metadata refinement passed (`:app:assembleRelease`, exit 0, 58s; 8 tasks executed, 36 up-to-date); the post-review unsigned release also passed (`:app:assembleRelease`, exit 0, 51s; 8 tasks executed, 36 up-to-date). This is not deployed signing or an upgrade check. Generated paths remain `app/build/test-results/testDebugUnitTest/`, `app/build/reports/tests/testDebugUnitTest/`, `app/build/reports/lint-results-debug.html` and `build/reports/problems/`. Temporary development logs are outside tracked files; this document records the durable summary.

## New tests and stable behavior IDs

| Suite | New tests | Real boundary and protected behavior |
| --- | ---: | --- |
| [DaniosManagerTest](../../app/src/test/java/com/juans/inspeccion/Mundo/DaniosManagerTest.java) | 10 | DAMAGE-002/004/005/006: required-field precedence, dimensions/size, material+labor+IVA, blank components, add/remove/renumber, copy album separation, repeated total/loaded-value accumulation |
| [DamageCostLookupTest](../../app/src/test/java/com/juans/inspeccion/Mundo/DamageCostLookupTest.java) | 5 | DAMAGE-004: real tariff calculation with supplied remote rates; worked total 33, NOAPLICA avoids reads, missing/failed tariff and blank quantity characterize zero outcomes |
| [InspectionRulesTest](../../app/src/test/java/com/juans/inspeccion/Mundo/InspectionRulesTest.java) | 11 | ENTRY-002/003, EXIT-001/002, CONTAINER-001/005: required/line/yard checks, membership/balance precedence, empty booking, ET/modification bypass, attended/type turno rejection |
| [FormBindingTest](../../app/src/test/java/com/juans/inspeccion/Interfaz/FormBindingTest.java) | 8 | ENTRY-001/003/006, EXIT-003/004, CONTAINER-003, SEARCH-002: four actual XML forms, invisible date/direction keys, CTIPOTURNO→CTIPOMOV alias, required fields, date conversion, same-day comparison and real read-only/modification menu handler |
| [InspeccionDataSourceTest](../../app/src/test/java/com/juans/inspeccion/Mundo/Data/InspeccionDataSourceTest.java) | 15 | ENTRY-004/006, EXIT-004/006, CONTAINER-004: real SQL builders/request composition for EP/ET entry/exit with/without turno; insert/modify date difference, numbering, linked container requests, failures, same-day modify and originating damage document |
| [InspectionSubmissionTest](../../app/src/test/java/com/juans/inspeccion/Mundo/Data/InspectionSubmissionTest.java) | 10 | ENTRY-004/005/006, EXIT-004/005/006, DRAFT-005: real extracted save orchestration with fake remote operations and real draft cleanup; number/agent assignment, remote timeout/duplicate error, photo-naming/local storage failure, repeated call and null-message exception quirks |
| [DraftRecoveryTest](../../app/src/test/java/com/juans/inspeccion/Mundo/Storage/DraftRecoveryTest.java) | 8 | DRAFT-001/002/003/004, MEDIA-008: real current writer/read-after-invalidation, modes/hidden fields/damage/album identity, atomic backup, corrupt/wrong type/I/O preservation, independent index and damaged queue separation |
| [StorageFormatProbeTest](../../app/src/test/java/com/juans/inspeccion/Mundo/Storage/StorageFormatProbeTest.java) | 13 | DRAFT-006/UPLOAD-004 (future contract preflight only): original legacy fixtures, location/signature recognition, truncated/unknown/empty data, old/newer schema, writer app-version independence, malformed envelope/trailing bytes and I/O |
| **Total added** | **80** | Existing 94 tests retained unchanged |

Documentation checks resolved all 245 local targets, with no missing links; `git diff --check` and shell syntax checks passed. Workflow YAML parsed locally; invalid local-runner arguments were verified to exit 2 before invoking Gradle. No hosted workflow or external documentation link execution was inferred from these checks.

SQL fakes replace the transport and metadata source, not the SQL builders, damage SQL-list assembly or business calculations. Assertions inspect meaningful requested effects and literal worked results instead of copying implementation formulas. Tests are not proofs that SQL Server executes these requests atomically/correctly; no H2/SQLite substitute was introduced.

The menu test calls the real handler on an attached activity without authenticated onCreate/loaders. XML/binding tests exercise actual resources and public form helpers. They do not establish full information-screen lifecycle, p1's container-versus-header routing, UI damage editing callbacks, operator read-only field handling or device acceptance. Those remain gated below.

## Supporting production seams

- [DAO](../../app/src/main/java/com/juans/inspeccion/Mundo/DAO.java): constructor becomes protected so a deterministic test transport can subclass it; one protected metadata accessor lets the original damage SQL-list assembly run against supplied schemas. No singleton/settings/driver/SQL behavior was changed.
- [InspeccionDataSource](../../app/src/main/java/com/juans/inspeccion/Mundo/Data/InspeccionDataSource.java): protected `dao`, `columns`, `currentDate` accessors preserve existing default collaborators. Tests override these boundaries for the exercised save/load methods. Other static Consultas/Contenedores calls remain; this is not complete repository injection.
- [DamageCostLookup](../../app/src/main/java/com/juans/inspeccion/Mundo/DamageCostLookup.java): the existing static tariff calculation moves intact to one method with a remote-read Source. DaniosManager keeps its public method and serialized surface, delegating to the default JDBC source. Current released deserialization tests remain passing.
- [InspectionRules](../../app/src/main/java/com/juans/inspeccion/Mundo/InspectionRules.java): existing p1 messages/precedence/bypasses and turno checks are callable without loaders/toasts; p1 delegates and still chooses its original toast context.
- [InspectionSubmission](../../app/src/main/java/com/juans/inspeccion/Mundo/Data/InspectionSubmission.java): existing background submission and post-save local removal/acknowledgement are separated from AsyncTask/UI. The activity retains lifecycle checks, toasts, print prompt and menu changes. No duplicate guard, replay or new retry mechanism was added.
- [StorageFormatProbe](../../app/src/main/java/com/juans/inspeccion/Mundo/Storage/StorageFormatProbe.java): **unwired**, read-only future preflight. Known legacy namespace plus serialization signature is older encoding, not payload integrity/app-version/draft-count evidence. Proposed JSON draft envelope distinguishes schema 0 (explicitly retired), 1 (supported envelope), >1 (newer), malformed, unknown, missing and I/O. No stored model is instantiated, writer changed, queue retired, notice shown or quarantine/reset performed.

`SUPPORTED_SCHEMA` means envelope support only. Record fields/semantic validation, strict encoding/resource limits, exact released schema retirement set, new store names, backups and complete cutover orchestration must be established in step 03 before this helper is used. It intentionally accepts an envelope containing unvalidated record data; that test names the limitation instead of falsely claiming current-format draft recovery. Current serialized draft round trips are tested separately. Original fixture bytes, the unrelated IDE diff and photo-gallery title prefix were hash-checked against the session start. The probe inspects one candidate file/stream; MISSING is not evidence that a store has no backups or journals. Step 03 must resolve those separately before any empty-state/cutover decision.

## Characterization is not approval of defects

Tests deliberately retain current outcomes: accumulated totals on repeated recalculation; dimensional branch bypass of the alternate size check; empty booking/transport/modification bypass and later-message precedence; blank quantity/failed tariffs yielding zero; copy-without-photos; empty GoneView destination; month 00 accepted; insertion versus modification date conversion; container update linking a changed code even without turno; repeated submission writing twice; exceptions without messages producing a null error.

These behaviors need business review or separate behavior-change tests/PRs. No calculation, validation, SQL, layout, queue or storage cutover bug was silently corrected. The empty update-button callback, video-only editing warning and receipt completion dialog remain UI/service acceptance gaps.

## Local runner and CI

[scripts/check.sh](../../scripts/check.sh) runs debug/test APK assembly, JVM tests and lint with the documented heap/workers. `--fresh` forces task execution; repeated `--tests PATTERN` selects only JVM tests. [test-report.py](../../scripts/test-report.py) summarizes XML and explicitly warns that XML alone cannot prove freshness. Configure JAVA_HOME and SDK locally; no personal path/settings are tracked. Python 3 is used only for reporting, not an app dependency.

```sh
./scripts/check.sh --fresh
./scripts/check.sh --tests '*InspectionRulesTest' --tests '*InspeccionDataSourceTest'
```

[GitHub Actions workflow](../../.github/workflows/android-regression.yml) runs the fresh runner on Ubuntu with Temurin 21 and SDK 34/build tools 35, without business credentials, service/device tests or deployment. Reports are retained separately as JVM/build/lint artifacts. Configuration was parsed locally and shell syntax checked; **hosted CI has not run**, so Temurin/hosted SDK provisioning is unverified. Official action interfaces were checked against [checkout](https://github.com/actions/checkout/tree/v4), [setup-java](https://github.com/actions/setup-java/tree/v4) and [upload-artifact](https://github.com/actions/upload-artifact/tree/v4). No actions or application dependency versions were upgraded in existing configuration.

Instrumentation commands/custom suite selectors remain in [baseline.md](baseline.md); test APK assembly is not execution. SQL dialect/atomicity/concurrency are step-05 isolated-service work. Physical camera/mic/printing and deployed upgrade acceptance remain ACCEPT-04/05/07/08. No percentages or hosted/device/service passes are inferred from JVM counts.

## Mandatory implementation gates and step-03 handoff

| Gate | Required evidence before its associated change |
| --- | --- |
| GATE-03-01 — new store/cutover | Implement the real record schema/validator, atomic separate namespace, durable classification/notice/cutover marker and error UX. Test supported records from another writer app version, retired/newer schemas, corrupt/unknown/I/O faults, failed isolation, interrupted/repeated cutover and new-draft save/reopen. Probe tests alone do not complete this gate. |
| GATE-03-02 — queue retirement and journals | Using the [inventory](../architecture/storage-and-side-effects.md), test that old queue work never replays/blocks inspection saving. Audit snapshot-bearing photo/video captures, deletion results/enums, publication maps, import mappings, index, backups and Android extras. Preserve public bytes/independent bookkeeping; visibly isolate unresolved old operations without stale replay. No blanket deletion or guessed ownership. |
| GATE-03-03 — package/Android moves | Cutover comes first in a separate change. Keep existing fixtures until recognition/error tests intentionally replace deserialization expectations. Re-run full JVM/build/lint and relevant existing local device suites after move groups; verify registered entry points, provider authority, runner/debug host, XML inflation and recreation on a dedicated test device before claiming restoration. No move occurred here. |
| GATE-04-01 — form/workflow changes | Before altering p1/p2 binding, no-turno initialization, damage update/copy/video-only warning, read-only fields or receipt completion, characterize real handlers/lifecycle with sanitized business fixtures and minimal additional injection. Current helper/menu tests do not establish these UI paths. Review intended fixes independently. |
| GATE-05-01 — SQL integration | Dedicated SQL Server version/schema and disposable records: validate dialect/functions/collation, affected rows, transaction rollback/atomicity, duplicate/timeout/uncertain commit and concurrency. Request-recording fakes cannot satisfy this gate. |

The storage exit criterion is **partial** by design: recognition/errors and current serialized recovery are tested, active media/printing checks retained, but the real future current-format cutover/new-draft recovery and deliberate replacement of deserialization requirements belong to GATE-03-01/02. Step 03 can implement that prerequisite; it cannot treat the preflight or a fake reset as a completed cutover and proceed directly to renaming persisted models.
