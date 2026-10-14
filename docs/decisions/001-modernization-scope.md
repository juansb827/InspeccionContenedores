# ADR 001 — Modernize the existing Java/XML application

Date: 2026-10-13. Status: accepted scope; later implementation pending. Authority: user's confirmed step-01 instructions. Documentation only in this step.

## Context and decision

The existing app supports yard/container inspection, transport documents, local media and printing in a single Android application module. Keep Java/XML and the single `app` module. Retain direct Android-to-SQL Server access. A backend API, Kotlin/Compose rewrite or module split is outside the authorized modernization scope.

Preserve application ID, deployed signing identity, Spanish operator workflows, entry/exit and document-type distinctions, read-only restrictions, validation/cost/save/modify/printing semantics unless separately approved behavior changes are documented. Preserve public photos/videos and media recovery work; uploads must not become automatically active. [ADR 002](002-storage-retirement.md) defines the explicit old draft/queue exception.

## Alternatives and consequences

A backend could centralize access but would require service design, hosting, deployment and an expanded project. It is not a dependency or exit criterion for any phase. Rewrites/splitting modules would multiply simultaneous change; incremental boundaries and tests fit the confirmed scope.

Future database work must therefore validate the real SQL Server dialect/transactions and JDBC/Android compatibility using a dedicated test service. No default test may discover production settings. Fleet/distribution/signing ownership remains an acceptance item; current release builds are unsigned, so assembly alone does not establish an installable in-place production update.

## Implementation state and references

Current architecture still couples widgets/resources to SQL generation and shared connection state; proposed separation has not landed. Step 01 inventories it, step 02 establishes regression seams/tests, later steps change boundaries. See [current architecture](../architecture/current-system.md), [master plan](../modernization/00-master-plan.md) and [acceptance register](../testing/acceptance.md).
