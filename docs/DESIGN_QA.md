# Design QA

## Direction

- Screen job: Show live kitchen status first and keep the four repeated actions obvious.
- First visual priority: The supplied low-saturation Hero image and readable action hierarchy.
- Density: Medium on home, medium-high on recipe browse.
- Variation: Low-medium.
- Motion: Medium-low.
- Shape language: Soft, compact geometry.
- Theme: Pale neutral canvas with one muted sage control family; independent dark skin.

## Source-Level Matrix

| Surface | Size | Theme | Text scale | Content case | Expected | Result | Evidence |
|---|---:|---|---:|---|---|---|---|
| Home | 320dp target | Light | 100% | Typical | Image Hero remains pale; four actions form two readable columns | Source passed | Content-driven Hero and weighted 2×2 card rows |
| Home | 320dp target | Dark | 200% | Long Chinese labels | Hero stacks vertically and card text grows without fixed clipping | Source passed | Compact Hero breakpoint and minimum-height cards |
| Home | 600dp+ | Light | 100% | Typical | Copy and image share the Hero horizontally | Source passed | 600dp adaptive orientation |
| Recipe browse | Compact target | Light/Dark | 200% | Active query and filters | Results lead; search/filter remain 48dp icon controls | Source passed | Existing sheet flow and revised bordered icon buttons |
| Empty/error | Compact target | Light/Dark | 100% | No recipe result | Actionable empty state remains visible | Source passed | Existing clickable empty state retained |

## Checks

- [x] Large deep-green top bar and Hero panel are removed.
- [x] Hero uses separate light/night 1440×810 resources and is decorative in the accessibility tree.
- [x] Home titles, descriptions, borders, skin control, and selected navigation have explicit contrast tokens.
- [x] Search, filter, theme, back, add, and navigation controls remain at least 48dp.
- [x] Search/filter state, back behavior, local data, and offline behavior are unchanged in source.
- [x] Motion keeps the system-disabled fallback and no continuous animation was added.
- [x] Unit tests, Debug/Release Lint, Debug/Release builds, APK metadata, permissions, and signing passed in CI run `31199878686` and local artifact checks.
- [ ] Physical-device font-scale, orientation, image decode, and TalkBack checks pass.

## Known Limits

- GUI automation, screenshots, screen readers, and physical-device control were not used.
- Device-only visual and TalkBack checks remain unclaimed until performed by an authorized person.
