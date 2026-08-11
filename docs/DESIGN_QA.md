# Design QA

## Direction

- Screen job: Show live kitchen status first and keep the four repeated actions obvious.
- First visual priority: A compact text-only kitchen status Hero and readable action hierarchy.
- Density: Medium on home, medium-high on recipe browse.
- Variation: Low-medium.
- Motion: Medium-low.
- Shape language: Soft, compact geometry.
- Theme: Pale neutral canvas with one muted sage control family; independent dark skin.

## Source-Level Matrix

| Surface | Size | Theme | Text scale | Content case | Expected | Result | Evidence |
|---|---:|---|---:|---|---|---|---|
| Home | 320dp target | Light | 100% | Typical | Text-only Hero remains compact; four actions form two readable columns | Source passed | Wrap-content Hero copy and weighted 2×2 card rows |
| Home | 320dp target | Dark | 200% | Long Chinese labels | Hero text grows without fixed clipping or image competition | Source passed | Wrap-content vertical Hero and minimum-height cards |
| Home | 600dp+ | Light | 100% | Typical | Hero remains a readable text/status card without an empty artwork region | Source passed | Full-width wrap-content Hero copy |
| Recipe browse | Compact target | Light/Dark | 200% | Active query and filters | Results lead; search/filter remain 48dp icon controls | Source passed | Existing sheet flow and revised bordered icon buttons |
| Recipe browse | Compact target | Light/Dark | 200% | Cuisine grouping | Category button remains 48dp; group headings reflow and empty cuisines are omitted | Source passed | RecyclerView header items and wrap-content labels |
| Recipe collections | 320dp target | Light/Dark | 200% | Long collection names | Number, name, status, and actions reflow without fixed card height | Source passed | Wrap-content cards and native buttons |
| Recipe collections | 600dp+ | Light/Dark | 100% | Cached/offline content | Cached collections remain readable while refresh errors are non-destructive | Source passed | Local repository renders before network refresh |
| Empty/error | Compact target | Light/Dark | 100% | No recipe result | Actionable empty state remains visible | Source passed | Existing clickable empty state retained |
| Yunfeng Special | 320dp target | Light/Dark | 100% | 150 recipes | Ordered cards use one column with stable cover height | Source passed | RecyclerView with fixed-height cover and stable IDs |
| Yunfeng Special | 600dp+ | Light/Dark | 100% | 150 recipes | Cards use two balanced columns | Source passed | GridLayoutManager switches to two columns at 600dp |
| Yunfeng Special | Compact target | Light/Dark | 200% | Long title/offline cover | Title remains reachable and missing cover keeps a local placeholder | Source passed | Four-line title allowance, whole-card action, and non-resizing placeholder |
| System Back | Compact target | Light/Dark | 100% | Detail, form, Home | Previous surface is shown; Home requires two presses before exit confirmation | Source passed | Unified legacy/new back dispatch and two-step Home policy |

## Checks

- [x] Large deep-green top bar and Hero panel are removed.
- [x] Hero contains no character image or artwork-only accessibility node.
- [x] Home titles, descriptions, borders, skin control, and selected navigation have explicit contrast tokens.
- [x] Search, filter, theme, back, add, and navigation controls remain at least 48dp.
- [x] Search/filter state, back behavior, local data, and offline behavior are unchanged in source.
- [x] Motion keeps the system-disabled fallback and no continuous animation was added.
- [x] MainActivity Java parsing, 26 XML resources, removed-resource references, version identity, manifest permissions, diff checks, and 29 local unit tests passed.
- [x] Debug/Release Lint, Debug/Release builds, APK metadata, permissions, removed-asset scan, and signing passed in CI run `31209408942` and local artifact checks.
- [x] Yunfeng catalog contains 150 ordered entries, 150 unique IDs, canonical source links, and allowed cover hosts.
- [x] Remote covers use three worker threads, bounded memory/disk caches, HTTPS host restrictions, timeouts, a 5MB response limit, and recycled-view guards.
- [x] Yunfeng cards have whole-card accessibility descriptions and preserve the collection after configuration changes.
- [x] v0.6.4-Bata local Debug/Release Lint completed with no errors; 32 tests, both APK assemblies, version metadata, permission metadata, live 150-entry ordering, and the source cover response passed.
- [x] v0.6.4-Bata CI build, continuous release signing, and signed-APK certificate checks pass in run `31296224643`.
- [x] System Back source policy covers page hierarchy and Home exit confirmation; physical-device confirmation remains pending.
- [x] Recipe collection actions and category mode use 48dp native controls with content descriptions and explicit states.
- [x] Habit history, theme, favorites, pantry, shopping list, and selected ingredients remain outside recipe collection payloads.
- [x] Recipe collection refresh caches the complete cloud state while repository visibility, page restore, active collection, and recipe lookup exclude locked custom specials.
- [x] v0.6.7-Bata local Android verification passed 54 unit tests, Debug/Release Lint with 0 errors, and Release assembly with SDK 36; no debug APK was assembled.
- [x] CI run `31515499816` passed cloud access, signing, APK metadata, and artifact upload; the single published APK was re-downloaded with a matching SHA-256.
- [ ] Physical-device font-scale, orientation, and TalkBack checks pass.

## Known Limits

- GUI automation, screenshots, screen readers, and physical-device control were not used.
- Device-only visual and TalkBack checks remain unclaimed until performed by an authorized person.
