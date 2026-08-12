# App Brief

## Task

- Date: 2026-08-12
- Requested outcome: Add an iPhone/iPad version that opens in Safari and installs through Add to Home Screen.
- Primary users: A small group using the recipe app on personal iOS devices without an App Store account or native signing flow.
- Non-goals: Native IPA/SwiftUI packaging, App Store distribution, background synchronization, analytics, ads, account registration, or Android package changes.

## Project Boundary

- Repository: `D:\Agent\Githubstorage\DawnsTing-Tings-Table`
- Paired repository: `D:\Agent\Githubstorage\Dawnsdew-Lulu-s-Table`
- Delivery surface: GitHub Pages PWA from `docs/pwa`.
- Public URL: `https://zalan-dunk.github.io/DawnsTing-Tings-Table/`
- Android source, package `com.dawns.tingstable`, signing, APK naming, and historical releases remain unchanged.

## Primary Flow

- Open the Pages URL in iPhone Safari.
- Use Share -> Add to Home Screen.
- Open the standalone PWA, browse a recipe, and use browser history to return.
- Add personal recipes, pantry items, and shopping entries that persist locally and remain usable after an offline restart.

## Experience

- Text-first compact home Hero with no character image.
- Restrained pale default skin and a neutral dark skin.
- Bottom navigation for Home, Recipes, Pantry, Specials, and Shopping.
- Search, filter, category mode, and add actions use 48px icon controls; search and filter content opens in a bottom sheet.
- iPhone safe-area insets, iPad two-column layouts, readable Chinese system typography, visible focus, and reduced-motion fallback.

## Data Boundary

- Local: Custom recipes, favorites, pantry, shopping list, usage counts, last-opened time, theme, cloud cache, and optional GitHub token.
- Cloud: Only numbered recipe collections and their custom recipes when the user explicitly configures and triggers sync.
- Recovery: JSON export/import is available because iOS may evict PWA browser storage.
- Security boundary: A private GitHub token is never injected into public Pages JavaScript. Optional cloud sync stores the user-entered token only in that browser profile.

## Acceptance Criteria

- Given Safari on iOS, when the Pages URL is added to the Home Screen, then the app opens in standalone mode with the declared name and icon.
- Given an offline restart after one successful load, when the PWA opens, then the local application shell and bundled recipe catalogs remain available.
- Given a narrow iPhone viewport or 200% text scale, when recipe tools render, then controls reflow without overlap and retain at least 48px targets.
- Given search or filter activation, when the sheet opens, then the recipe list is not permanently displaced by a large search bar.
- Given a recipe detail or form, when Back is used, then the previous PWA surface is restored through browser history.
- Given no special key, when cloud state contains `KKLLTL`, then its metadata and recipes stay hidden; `TL123` unlocks it locally.
- Given two same-name recipes with different UUIDs, when saved or imported, then both remain present.
- Given browser storage loss, when a valid exported JSON file is imported, then the local PWA data is restored without changing the locally stored cloud token.

## Delivery

- Version/tag: `v0.6.8-Bata`
- Artifact: `LazySheepChef-PWA-v0.6.8-Bata.zip`
- Deployment: GitHub Pages through `.github/workflows/pages.yml`.
- Verification: Node data-rule tests, manifest and service-worker checks, catalog counts, JavaScript parsing, HTTP deployment checks, and GitHub Pages workflow result.
- Known device boundary: Physical iPhone installation, VoiceOver, orientation, keyboard, and storage eviction are not claimed without user/device verification.
