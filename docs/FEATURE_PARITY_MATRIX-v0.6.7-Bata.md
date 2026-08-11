# Feature Parity Matrix v0.6.7-Bata

| Flow | v0.6.6-Bata | v0.6.7-Bata | Verification |
|---|---|---|---|
| Built-in recipes | Local catalog with search/filter | Preserved; adds cuisine grouping and habit sorting | Filter, grouping, and sorting tests |
| Custom recipes | Local plus whole-device backup | Assigned to numbered recipe collections and synchronized by UUID | Collection state tests |
| Cloud data | Recipes and personal device state | Collection metadata and custom recipes only | Payload source audit |
| Cloud route | GitHub Contents API for profile backup | GitHub Contents API for complete `collections/state.json` | Client source and CI write probe |
| Restore behavior | Confirmed whole-device replacement | Non-destructive refresh with revision conflict choice | Dirty-state and conflict flow review |
| Personal settings | Included in backup | Favorites, pantry, shopping, theme, selected ingredients, and history stay local | Source audit |
| Existing specials | Two APK-managed special slots | Preserved and separate from custom cloud specials | Special catalog checks |
| Custom special | Not available | Complete data is cached; local packaged key controls visibility of `KKLLTL` | State and visibility tests |
| Navigation and Back | Previous surface; Home double-Back exit | Preserved for collection hub, collection recipes, detail, and form | Back state/source review |
| Release upgrade | Version 13 and repository certificate | Version 14 with the same repository certificate | APK metadata and certificate checks |
