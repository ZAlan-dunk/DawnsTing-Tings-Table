# Design QA

## Direction

- Screen job: Let recipe content lead; reveal search and filters only when requested.
- First visual priority: Recipe results on browse, live kitchen status on home.
- Density: Medium-high on recipe browse, medium on home.
- Variation: Medium.
- Motion: Medium-low.
- Shape language: Soft, compact geometry.
- Theme: Warm paper, jade, cinnabar, old gold, and restrained category accents.

## Source-Level Matrix

| Surface | Size | Theme | Text scale | Content case | Expected | Result | Evidence |
|---|---:|---|---:|---|---|---|---|
| Recipe browse | 320dp target | Light | 100% | Typical | Persistent controls stay compact and results fill remaining height | Source passed | 66dp programmatic header constraint |
| Recipe panels | Compact target | Light | 200% | Long mixed labels | Content scrolls; fixed actions remain reachable | Pending device check | Scroll container and weighted sheet content |
| Home | 320dp target | Light | 200% | Empty pantry | Hero grows with content and links to pantry | Pending device check | Content-driven hero height |
| Recipe browse | Tablet target | Light | 100% | Empty result | Centered content and actionable empty state | Source passed | 820dp content maximum and clickable empty state |

## Checks

- [x] Recipe content is no longer preceded by an oversized persistent filter stack.
- [x] Search, filters, close, reset, add, home actions, and navigation use at least 48dp targets.
- [x] Search and filter panels have explicit close and back behavior.
- [x] Active search/filter state is expressed through text, state description, and color.
- [x] Motion has a system-disabled fallback and no continuous animation.
- [x] Hero art is decorative in the accessibility tree; the containing action has one useful label.
- [ ] Light and night skins preserve surface, text, divider, status, and focus contrast.
- [ ] Android resource compilation, unit tests, Debug/Release lint, and Debug/Release assembly pass in GitHub Actions.
- [ ] Physical/device font-scale and orientation checks pass.

## Known Limits

- GUI screenshots, screen readers, and physical-device interaction are not authorized in this task.
- Device-only visual and TalkBack checks must remain unclaimed until run by an authorized person.
