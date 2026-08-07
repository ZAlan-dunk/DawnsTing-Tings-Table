# App Brief

## Task

- Date: 2026-08-05
- Requested outcome: Compress recipe search and filters into icon-triggered panels, add distinct home icons, strengthen the sheep-chef visual identity, and add clearer page transitions.
- Primary user and context: A single person using an offline Android cooking utility repeatedly at home.
- Non-goals: Accounts, analytics, network features, commercial modules, a Compose migration, or changes to repository/package identity.

## Project Boundary

- Resolved project path: `D:\Agent\Githubstorage\DawnsTing-Tings-Table`
- Active workspace root: The repository is an explicitly authorized external project for this iteration.
- External-write approval: The user requested fetching and iterating this exact repository. No neighboring path is authorized.
- Repository owner/name: `ZAlan-dunk/DawnsTing-Tings-Table` (public)
- Branch and upstream: `main` -> `origin/main`
- Cloud comparison result: Fetched 2026-08-05; local `HEAD` matched `origin/main` before edits.

## Stable Identity

- Package ID: `com.dawns.tingstable`
- Namespace: `com.dawns.tingstable`
- Artifact base name: `LazySheepChef`
- Installed display name: `懒羊羊当大厨～`
- In-app display name: `懒羊羊当大厨～`

## Experience

- Primary flow: Open recipes, scan results immediately, and reveal search or filters only when needed.
- Retained secondary flows: Home dashboard, recipe details and editing, pantry, pantry matching, specials, favorites, and shopping list.
- Information priority: Recipe results first; query/filter controls second; summaries remain visible in compact form.
- Visual direction: Warm paper, jade, cinnabar, old gold, and an original sleepy sheep-chef motif; quiet and tactile rather than promotional.
- Shape language: Soft geometry with consistent compact radii and 24dp line icons.
- Theme plan: Retain the existing light theme and semantic palette.
- Motion: Medium-low. Short directional navigation, sheet reveal, press feedback, and static fallback when animations are disabled.

## Data and Device Conditions

- Local data: Existing SharedPreferences and JSON data remain compatible and on-device.
- Network behavior: No runtime network dependency and no `INTERNET` permission.
- Target surface: Native Android APK, Android 8.0 and later.
- Layout: Compact phones through tablets, portrait and landscape, touch input.
- Text scale: 100% to 200% without essential content loss.
- Offline requirement: Complete.

## Acceptance Criteria

- Given the recipe list, when no search or filter panel is open, then results occupy the primary screen area and persistent controls remain under 96dp where practical.
- Given a saved query or filter, when recipe details are opened and closed, then the query, filters, result summary, and source page remain intact.
- Given search or filter icons, when activated, then a labeled dismissible panel exposes the corresponding controls, including clear/reset behavior.
- Given the home screen, when it opens, then four main actions have distinct semantic icons and the hero communicates live pantry/cookable status.
- Given system animations are disabled, when navigating or changing filters, then the final state appears immediately and remains operable.
- Given 200% text scale, when controls reflow, then essential labels are not silently clipped and all actions remain reachable.

## Delivery

- Next unused version: `v0.6.2-Bata` / `versionCode 9`
- Artifact: `LazySheepChef-v0.6.2-Bata-release.apk`
- Verification: Unit tests, lint, debug/release assembly, APK metadata, permission and signature checks.
- Historical releases: Preserve `v0.1-beta` through `v0.5-beta` and all assets unchanged.
- Development memory: `docs/APP_DEVELOPMENT_MEMORY.md`
