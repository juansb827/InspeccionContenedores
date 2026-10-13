# Feature behavior document template

This is a planning template for future `docs/features/*.md` files, not a statement that the example behavior below is already implemented or confirmed.

```markdown
# <Feature name>

Status: observed / operator-confirmed / proposed
Last reviewed: <date and app version/commit>
Owner/reviewer: <role or person>

## Purpose and users

What the operator is trying to accomplish and who may perform the action.

## Entry points and prerequisites

Where the user starts, required context, and supported modes.
Include read-only restrictions and relevant permission/connectivity requirements.

## Normal behavior

Describe the sequence in user terms, using actual Spanish UI labels.
Give requirements stable IDs such as DAMAGE-001 or INSPECTION-004.
State visible results, calculations and the meaning of success.

## Validation and special cases

List required values, business restrictions, empty states and mode differences.
Distinguish observed current behavior from intended changes.

## Cancellation, failures and recovery

Explain what is retained, what the user sees, and what can be retried.
Cover interrupted work, unavailable services and uncertain outcomes where relevant.

## Persistence and side effects

Describe what remains after leaving/reopening/updating and what explicit deletion does.
Explain externally visible effects without naming SQL tables, Java classes or libraries.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| <FEATURE-001> | <starting state> | <user action> | <observable result> | <test/manual case> |

## Open questions and evidence

Record unresolved business decisions and whether evidence is a walkthrough,
test, source inspection, screenshot or historical document.
Link implementation/architecture details separately when helpful.
```

Example of the desired level: “When an operator removes a damage record, the displayed total is recalculated. Whether its public media is deleted is stated separately in the media retention rules.” Verify both statements for the actual workflow before making them requirements. Avoid explanations such as “the Fragment calls the DAO and refreshes the adapter” in feature behavior prose.

Documentation upkeep belongs in each feature PR: change a behavior requirement only when behavior intentionally changes; update its acceptance case in the same PR. Refactoring without behavior changes should preserve the existing requirements.
