# 09 — Platform and dependency maintenance

Status: proposed. Depends on step 08's decision/implementation. Outcome: a supportable platform baseline without mixing compatibility upgrades into business refactors.

## Work sequence

1. Confirm how this app is deployed and the actual Android/device fleet. Select supported minimum/target/compile SDKs from those facts and current distribution requirements at implementation time. The baseline is min 29 / target 33 / compile 34; do not choose a moving “latest” version in advance.
2. Audit bundled libraries and actual usages: jTDS, old Apache networking, commons-net/IO, iText, POI/JXL, and the Godex printer SDK. Record maintenance/support, licenses, runtime compatibility and replacement cost. Remove an unused library only after proving its consumers are absent. Do not modernize the printer SDK without testing the deployed printers.
3. Reassess build/Gradle/JDK and AndroidX compatibility together. Upgrade in small groups, preserving a recorded dependency graph and known working toolchain. Maintain the historical Robolectric resource setup until an equivalent compatibility test strategy is demonstrated.
4. Review permissions, exported components, backups of credentials/drafts, file-provider scope, network policy and logging against real integrations. The baseline declares cleartext compatibility and an exported network receiver; establish which callers/protocols require them before narrowing access. `usesCleartextTraffic` is not a universal switch that secures raw JDBC/FTP traffic: verify each transport's own TLS/authentication behavior.
5. Separate any server-side TLS/authentication or FTP replacement project from local dependency updates. Direct SQL Server access retains a device-side database credential requirement; cosmetic obfuscation does not remove it. A backend API could change that architecture in a future project, but its implementation is outside this plan. Coordinate credential changes with owners if exposure is confirmed; never place real secrets in docs, test fixtures or logs.
6. Address existing lint issues by category, particularly lifecycle/navigation and private-API reflection. Keep new lint errors failing; do not regenerate a larger baseline merely to obtain a green check.
7. Exercise behavior affected by the selected target SDK: insets/edge-to-edge, keyboard, back handling, permissions, media ownership, receivers and background execution. Update the step 06 device matrix and baseline screenshots accordingly.

Steps 05–07 should already have addressed unsafe SQL/logging, responsive layout and critical lifecycle coupling. This phase handles remaining platform concerns; it is not a reason to postpone those known defects until the end.

## Review slices

- Dependency/usage register and target-platform decision.
- One independently testable dependency family or build-tool upgrade per PR.
- Network/permission/backup/component changes grouped by actual behavior.
- Remaining lint cleanup and refreshed compatibility evidence.

## Exit criteria

- [ ] Supported devices, OS versions, build environment and distribution assumptions are documented.
- [ ] Each retained legacy library has a reason/owner; replacement versions are validated, not just declared.
- [ ] All automated regression suites still pass on the selected toolchain.
- [ ] Camera/microphone, mixed-media playback/deletion, physical printing and exports work on the agreed devices.
- [ ] No new lint errors are hidden by a blanket baseline update.
- [ ] Target-SDK behavior changes have explicit acceptance evidence.
- [ ] Network/credential limitations of the selected database/upload architecture remain visible until actually resolved.

Rollback: keep each toolchain/dependency change independently reversible. Validate that a previous APK can still operate with any changed server policy. Production update/downgrade constraints require a properly versioned signed build; a simple local git revert is not a deployment procedure.
