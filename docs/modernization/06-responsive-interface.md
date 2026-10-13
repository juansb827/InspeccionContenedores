# 06 — Responsive interface with familiar styling

Status: proposed. Depends on steps 01–05, particularly step 04's form-binding separation. Outcome: readable labels, reachable controls, and consistent field alignment across agreed window sizes and text settings.

## Diagnosis and design direction

The supplied screenshot shows an ellipsized inspection title, `Componente` broken inside the word, `Metodo` against the edge, cramped action rows, and substantial empty list space above the form. Source inspection identifies likely contributors:

- `nuevo_danio.xml` uses ten table columns, with the four labels spanning 3/3/2/2 columns.
- `MyLabelStyle` applies large bold text without shared horizontal padding; some input text sizes use `dp` instead of `sp`.
- `fragment_entrada_pag2.xml` reserves 4/10 of flexible height for the list and 6/10 for a separate form scroller, even when the list is empty.
- `InspeccionActivity` gives the standard single-line action bar a long full title.
- `p2` casts the form to `TableLayout`; `Formularios` gathers fields by its exact nesting shape. The same interaction contract must survive a layout replacement.

Preserve the blue section headers, Spanish labels, information/damage tabs, field order, recognizable buttons, validation, and action availability. Keep Views/XML. Use a mixture of simple vertical layouts and ConstraintLayout where relationships require it; changing layout technology alone does not make text fit. Android supports adaptable Views through flexible sizing and width-qualified resource variants. [Adaptive layouts with Views](https://developer.android.com/develop/ui/views/layout/responsive-adaptive-design-with-views)

## Proposed behavior for the reported defects

| Element | Proposed treatment | Acceptance |
| --- | --- | --- |
| Long title | Compact action-bar title such as `Inspección · Entrada`, with the full contextual heading in content; alternative is a measured two-line custom toolbar | Direction/document context remains visible, including at large text; overflow menu stays reachable. Proposed wording requires review against the workflow vocabulary. |
| Location/component/damage/method | Each field is a label+value group. Use a single column on the narrowest layouts, two columns where they fit, and four only when measured space permits | `Componente` stays intact; no label or value is clipped; multiline values expand their own field group |
| Header/button edge spacing | Shared start/end padding, screen gutters and inter-field gaps | `Método` and every outer label have visible end padding and consistent alignment |
| Empty damage list | A compact empty state; the editor uses the released space | Empty list does not reserve a fixed fraction of the screen |
| Populated list and editor | On compact windows, a single vertical scroll owner for list summary/items and editor, preferably RecyclerView sections if list size warrants it; wide layouts can separate panes | All damage items and editor controls remain reachable without competing unconstrained vertical scrollers |
| Action and camera rows | Wrap into rows or stack according to available width, retaining order and button meanings | No clipped action labels or offscreen actions; enabled/disabled behavior is unchanged |

Changing the title presentation and action arrangement is a small proposed polish, not an already approved vocabulary change. Document the final choice and before/after examples during this step. Do not globally shrink text, disable font scaling, or force every label onto one line to hide overflow.

## Work sequence

1. Reproduce the baseline with a debug-only inspection fixture host and realistic short/long values, no production login. Record actual window dp and text settings; do not infer them from the 609-pixel screenshot.
2. Define shared dimensions and text appearances: proposed 16dp outer gutters, 8–16dp internal gaps, `sp` typography and content-driven heights. Validate them against the actual forms before applying globally. Make interactive targets at least 48×48dp and associate labels with fields. Check the blue/white combination for contrast and preserve its character with a shade adjustment only if needed. [Android accessibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps)
3. Pilot the damage screen and `nuevo_danio` using explicit field binding from step 04. Preserve resource IDs when useful, include hidden metadata fields in binding tests, and remove table casts/traversal assumptions. Test both entry and exit because screen code shares behavior/resources.
4. Start with `res/layout` for compact windows and add `layout-w600dp` where available width supports another arrangement. Treat 600dp as a starting breakpoint, not a guarantee that large text fits. Use current available width, not device type or orientation alone; collapse columns further when content/text scaling needs it.
5. Solve scrolling, keyboard visibility and system-bar insets together. Keep the focused field and validation message reachable; preserve scroll position, selected damage and input during resize/recreation. Avoid putting an unconstrained ListView inside a ScrollView.
6. Extend the accepted patterns to inspection information, container forms, search/lookup dialogs, settings, pending lists and printing selection. Review shared-style effects on camera/gallery and read-only modes.

## Verification matrix

| Axis | Initial coverage |
| --- | --- |
| Available width | 320, 360, 411, 600 and 840dp, plus one less/more than each chosen breakpoint |
| Text scaling | 1.0, 1.3, 1.5 and 2.0; test OS display scaling too |
| Window shape | Portrait, short landscape, split-screen/resizing, keyboard shown/hidden |
| Content | Empty/one/many damages; long Spanish labels/values; validation errors; loading and failed lookups |
| Workflow | Entry/exit, supported document types, editable/read-only, selected/unselected damage, active media/printing restrictions |
| Runtime | API 29 and 34 baseline; deployed devices and newly supported API from step 09 |

Run representative combinations for all screens and the full stress combinations for the damage pilot. Screenshots establish appearance; interaction checks establish that fields/actions still work. Check TalkBack order and focus after reflow. A screenshot comparison alone cannot verify submission inputs.

## Review slices and exit criteria

Suggested PRs: scoped typography/spacing; damage binding/layout pilot; compact/wide scrolling; remaining forms/dialogs; visual and interaction acceptance evidence.

- [ ] Full required context is visible without relying on a clipped title.
- [ ] No mid-word splitting of `Componente`, clipped field labels, or edge-touching `Método` in the agreed matrix.
- [ ] All controls remain reachable with large text and keyboard; form entry requires no horizontal scrolling.
- [ ] All variants produce equivalent business inputs and preserve hidden fields, validation, disabled states and read-only rules.
- [ ] Resize/recreation preserves draft edits, active tab, selected damage and media context.
- [ ] Before/after images show modest visual changes and are reviewed against actual operator workflows.

Rollback: revert layout/style groups together with any corresponding binding changes needed for compatibility. No database or durable-storage migration belongs in this step.
