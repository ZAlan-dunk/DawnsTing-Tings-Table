# App Brief

## Task

- Date: 2026-08-09
- Requested outcome: Rename the second special collection to Yunfeng Special and add all 150 recipes from the supplied public favorites list in the original order with source-linked covers.
- Primary user and context: A single person using a local-first Android cooking utility repeatedly at home, with optional network access for the source-linked special collection.
- Non-goals: Accounts, analytics, unrelated network features, commercial modules, a Compose migration, or changes to repository/package identity.

## v0.6.5 Navigation Addendum

- Requested outcome: Make the Android system Back action navigate to the previous in-app surface; require two consecutive Back actions on Home before showing an exit confirmation.
- Upgrade constraint: Keep the continuous release signing certificate and increment the version code so the new release APK can cover-install v0.6.4-Bata.

## Project Boundary

- Resolved project path: `D:\Agent\Githubstorage\DawnsTing-Tings-Table`
- Active workspace root: The repository is an explicitly authorized external project for this iteration.
- External-write approval: The user requested fetching and iterating this exact repository. No neighboring path is authorized.
- Repository owner/name: `ZAlan-dunk/DawnsTing-Tings-Table` (public)
- Branch and upstream: `main` -> `origin/main`
- Cloud comparison result: Fetched 2026-08-09 with process-local Git HTTP/1.1; local `HEAD` matched `origin/main` before edits.

## Stable Identity

- Package ID: `com.dawns.tingstable`
- Namespace: `com.dawns.tingstable`
- Artifact base name: `LazySheepChef`
- Installed display name: `懒羊羊当大厨～`
- In-app display name: `懒羊羊当大厨～`

## Experience

- Primary flow: Open Specials, enter Yunfeng Special, scan 150 ordered cover cards, and open the selected original recipe.
- Retained secondary flows: Home dashboard, recipe details and editing, pantry, pantry matching, specials, favorites, and shopping list.
- Information priority: Yunfeng recipe cover and title first, order and source action second; existing recipe results and compact controls remain unchanged.
- Visual direction: Pale neutral canvas, restrained rose accent, single-column phone cards, and two-column tablet cards; quiet and readable rather than promotional.
- Shape language: Soft geometry with consistent compact radii and 24dp line icons.
- Theme plan: Default pale skin plus a persistent true dark skin, using shared content and skin-specific control contrast without Hero artwork.
- Motion: Medium-low. Short directional navigation, sheet reveal, press feedback, and static fallback when animations are disabled.

## Data and Device Conditions

- Local data: Existing SharedPreferences and JSON data remain compatible and on-device; source cover responses use a bounded app cache.
- Network behavior: Core app flows remain offline; Yunfeng covers load from `i2.chuimg.com` over HTTPS and original recipes open on `m.xiachufang.com` through the system browser.
- Target surface: Native Android APK, Android 8.0 and later.
- Layout: Compact phones through tablets, portrait and landscape, touch input.
- Text scale: 100% to 200% without essential content loss.
- Offline requirement: Existing core flows remain complete; Yunfeng cards keep placeholders while offline and original source pages require connectivity.

## Acceptance Criteria

- Given the recipe list, when no search or filter panel is open, then results occupy the primary screen area and persistent controls remain under 96dp where practical.
- Given a saved query or filter, when recipe details are opened and closed, then the query, filters, result summary, and source page remain intact.
- Given search or filter icons, when activated, then a labeled dismissible panel exposes the corresponding controls, including clear/reset behavior.
- Given the home screen, when it opens, then four main actions have distinct semantic icons and the hero communicates live pantry/cookable status.
- Given the home screen in either skin, when it opens, then no character image is shown above or inside the Hero.
- Given Specials, when the second collection is shown, then its title is “楚天云岫 · 云峰特典” and its count is 150.
- Given Yunfeng Special, when the list opens, then all 150 recipes appear in the supplied collection order with unique stable IDs.
- Given a cover request succeeds, when its card is visible, then the corresponding source image fills the fixed cover area without changing card dimensions.
- Given a cover request fails or the device is offline, when its card is visible, then a local placeholder remains and the list stays operable.
- Given a Yunfeng card, when activated, then the system browser opens its canonical mobile source URL; invalid hosts are rejected.
- Given a 600dp or wider screen, when Yunfeng Special opens, then cards use two columns; compact screens use one column.
- Given the home screen in either skin, when it opens, then the Hero avoids a large deep-green panel and the four actions remain visibly bounded with readable titles and descriptions.
- Given system animations are disabled, when navigating or changing filters, then the final state appears immediately and remains operable.
- Given 200% text scale, when controls reflow, then essential labels are not silently clipped and all actions remain reachable.
- Given any non-Home surface, when the Android system Back action is invoked, then the previous in-app surface is shown instead of finishing the Activity.
- Given Home, when Back is invoked once, then an exit hint is shown; when invoked again within the short confirmation window, then an exit confirmation dialog is shown.
- Given the release APK, when installed over v0.6.4-Bata, then the package, signing certificate, and incremented version code permit an in-place upgrade.

## Delivery

- Next unused version: `v0.6.5-Bata` / `versionCode 12`
- Artifact: `LazySheepChef-v0.6.5-Bata-release.apk`
- Verification: Unit tests, lint, debug/release assembly, APK metadata, permission and signature checks.
- Historical releases: Preserve `v0.1-beta` through `v0.6.3-Bata` and all assets unchanged.
- Development memory: `docs/APP_DEVELOPMENT_MEMORY.md`
