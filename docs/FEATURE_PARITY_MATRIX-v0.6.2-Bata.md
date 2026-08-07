# Feature Parity Matrix — v0.6.2-Bata

| Area | Existing behavior | Target behavior | Status | Data migration | Verification | Notes |
|---|---|---|---|---|---|---|
| Home Hero | Live pantry and recipe recommendation with vector art | Same behavior with approved raster image and pale surface | Changed | None | Source and resource checks pending CI | Click target and accessibility label retained |
| Home actions | Four single-row actions | Four two-column cards with the same destinations | Changed | None | Source checks pending CI | Semantic icons retained |
| Recipe browse | Icon-triggered search/filter sheets | Unchanged behavior with clearer icon boundaries | Existing | None | Existing unit tests and CI | Query/filter state retained |
| Theme | Persistent light/night selection | Same persistence with revised tokens and explicitly selected light/night Hero resources | Changed | Existing preference key retained | ThemeMode tests and CI | Does not follow system mode |
| Navigation | Five native bottom destinations | Same destinations and back behavior with clearer selected state | Existing | None | Existing navigation source checks | No route removed |
| Local data | SharedPreferences and JSON repositories | Unchanged | Existing | None | Existing tests | Offline-only |
| Permissions | No network permission | Unchanged | Existing | None | Manifest/APK inspection | No new permission |

## Migration Decisions

- Intentionally changed: Home Hero artwork, palette, top bar, home action layout, control contrast.
- Deferred with user approval: Physical-device screenshot and TalkBack verification.
- Removed with user approval: Flat vector Hero is no longer displayed on the home screen.
- Compatibility risks: The new bitmap increases APK size and decode memory; the Android asset is capped at 1440×810.

## Exit Gate

- [x] Every existing user-facing capability is represented.
- [x] Local data migration or reset behavior is explicit.
- [ ] Navigation, back behavior, offline use, and permissions pass CI checks.
- [x] The Web prototype remains available until APK acceptance.
