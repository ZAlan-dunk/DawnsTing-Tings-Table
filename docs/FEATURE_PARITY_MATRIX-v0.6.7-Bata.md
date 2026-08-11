# Feature Parity Matrix v0.6.7-Bata

| Flow | v0.6.6-Bata | v0.6.7-Bata | Verification |
|---|---|---|---|
| Built-in recipes | Local catalog with search/filter | Preserved; adds optional cuisine grouping and habit sorting | Filter, grouping, and sorting tests |
| Custom recipes | Local plus whole-device backup | Assigned to numbered recipe collections and synchronized by UUID | Collection model and API tests |
| Cloud data | Recipes, favorites, pantry, shopping, selected ingredients, theme | Collection metadata and custom recipes only | Payload source audit and API contract |
| Restore behavior | Confirmed whole-device replacement | Revision-aware recipe sync; conflict requires explicit choice | Dirty-state and conflict flow review |
| Favorites/pantry/shopping/theme | Included in cloud backup | Local-only and unchanged by recipe sync | Source audit and retained tests |
| Existing specials | Two APK-managed special slots | Preserved and separate from cloud custom collections | Special catalog checks |
| Protected custom collection | Not available | Hidden from public catalog; key unlock returns scoped token | Worker tests and live API checks |
| Navigation and Back | Previous surface; Home double-Back exit | Preserved for collection hub, collection recipes, detail, and form | Back state/source review |
| Release upgrade | Version 13 and repository certificate | Version 14 with the same repository certificate | APK metadata and certificate checks |
