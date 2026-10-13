# 04 — Data and business boundaries

Status: proposed. Depends on step 03. Outcome: UI layout and database transport can change independently.

## Problem and target

`InspeccionDataSource` reads values from `ArrayList<CustomView>` and uses `Resources` for database identifiers. `Formularios.recorrerTableLayout` assumes a specific child hierarchy. `DaniosManager` combines persisted data, calculations, resources, and remote lookups. These dependencies are more consequential than package names.

Proposed flow:

```text
Activity/Fragment → form binding → business input / UI controller
                                  ↓
                           repository contract
                                  ↓
                        JDBC or local adapter
```

Form binding belongs to UI code and runs on the main thread. Repositories receive plain values, stable identifiers, and explicit commands; they never receive a View, Activity, or Fragment. Repository contracts live in `data/repository` for this small app; reusable pure rules/models do not import data implementations. Constructor injection and a small application composition point are sufficient initially.

## Work sequence

1. Pilot inspector lookup: return a typed result distinguishing found, missing/disabled, and connection failure while preserving current login rules. This is an access lookup, not proof of secure authentication.
2. Introduce only needed operation contracts: inspection read/submit/modify, container/booking lookup/update, reference data, and draft storage. Wrap the existing implementation first and verify equivalent results before rewriting SQL.
3. Create explicit form-to-input mapping for information and damage fields, including hidden calculation metadata, destination keys, dates, and noneditable values. Replace reliance on `TableLayout` traversal for migrated forms. Test that both compact and wide layouts can produce identical inputs later.
4. Extract validation and cost calculations into plain Java collaborators around the business models. Keep explicit storage mapping independent of class/package names under the step 03 format contract. Preserve current amounts, rounding and validation order. A future decimal-money change needs its own expected-output fixtures and business decision.
5. Model outcomes deliberately: validation failure, unavailable connection, not found, duplicate/conflict, confirmed save, and unknown save outcome. Translate them to existing or documented Spanish messages at the UI boundary.
6. Apply the pattern to the remaining operation inventory. Introduce test fakes and controllable execution without a full dependency-injection framework. Constrain direct legacy access to documented adapters until replaced.

## Suggested review slices

- Inspector lookup interface + legacy adapter + tests.
- Damage/information field mapping and pure rules, preserving the current explicit storage schema.
- Inspection submission/modify and container/booking repository contracts.
- Remaining search/settings/reference-data consumers and boundary checks.

Do not redesign all screens, replace the driver, introduce Room, or convert all background work in this step. Operations already requiring background execution must remain off the UI thread. Step 05 gives the remote adapter explicit execution/connection ownership; step 07 standardizes screen lifecycles.

## Exit criteria

- [ ] Every operation being migrated in step 05 has a typed/testable contract and a known legacy implementation.
- [ ] Compact/wide form work no longer depends on a hard cast to `TableLayout` or a fixed nesting depth for migrated screens.
- [ ] All existing business keys and hidden fields are represented in mapping tests.
- [ ] New data code has no dependencies on `ui` classes; pure rules have no dependency on Android Views/resources.
- [ ] UI callers use repositories for migrated operations, and the remaining exception list is explicit.
- [ ] Tests demonstrate unchanged business outcomes, current-format draft recovery, and intentional recognition of retired older formats.

Rollback: switch an operation back to its legacy adapter without changing storage formats or UI contracts. Avoid two active implementations writing the same operation.
