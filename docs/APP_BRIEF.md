# App Brief

## Task

- Date: 2026-08-11
- Requested outcome: Add cloud recipe collections, sequential standard numbering, key-gated custom collections, cuisine grouping, and local habit sorting without synchronizing personal settings.
- Primary user and context: A small number of personal Android users who browse shared recipes without a GitHub or app account.
- Non-goals: Social feeds, comments, analytics, advertising, automatic background sync, built-in-special migration, package-ID changes, or uploading personal settings.

## Project Boundary

- Resolved project path: `D:\Agent\Githubstorage\DawnsTing-Tings-Table`
- Paired copy: `D:\Agent\Githubstorage\Dawnsdew-Lulu-s-Table`
- Cloud repository: `ZAlan-dunk/Dawnsdew-Recipe-Cloud` remains the private recipe data store and Worker source repository.
- Repository state: Both public app repositories started clean on `main` with `HEAD` matching `origin/main` after fetch.

## Stable Identity

- Package ID and namespace: `com.dawns.tingstable`
- Artifact base: `LazySheepChef`
- Installed and in-app display name: `懒羊羊当大厨~`
- Signing: Continue each repository's existing independent release certificate.
- Copy rule: Runtime behavior stays aligned; only repository identity, APK filename, sync profile, and signing chain differ.

## Experience

- Primary flow: Open Home, enter Personal Recipe Collections, choose a numbered collection, and browse it using search, filters, cuisine groups, or habit sorting.
- Creation flow: Fetch the cloud catalog, create a standard collection, receive its server-assigned number and one-time management recovery code, then upload existing custom recipes.
- Protected flow: Enter a custom-collection key; the server returns only the matching protected collection and a scoped token.
- Retained flows: Built-in recipes, local favorites, recipe editing, pantry, ingredient matching, shopping list, existing specials, themes, remote covers, and system Back behavior.
- Visual direction: Preserve the restrained light/night system. Keep recipe content dominant and use compact 48dp icon actions instead of a persistent filter panel.

## Data Boundary

- Synced: Collection ID, name, type, revision, timestamps, and custom recipes.
- Local only: Theme, favorites, pantry, shopping list, selected ingredients, view mode, usage counts, last-opened times, and encrypted scoped tokens.
- Legacy: Historical v0.6.6 files remain unchanged. The service imports `customRecipes` once and ignores every other legacy field.
- Security: GitHub data credentials and custom keys remain server-side. The APK contains only the HTTPS API endpoint and encrypted scoped tokens issued by the service.
- Offline: Cached collections remain readable; pending local edits remain marked until a later explicit sync.

## Acceptance Criteria

- Given two concurrent standard-collection creations, when both complete, then each receives a unique `Dew-xxxx` number and occupied numbers are never reused.
- Given same-name recipes have different UUIDs, when synchronized, then both remain present.
- Given an unauthenticated catalog request, when protected collections exist, then their IDs, names, and recipes are absent.
- Given a valid protected key, when unlock succeeds, then only that collection receives a scoped token and the key is not persisted by the app.
- Given local and remote revisions differ, when sync runs, then local data remains intact until the user explicitly chooses the local or cloud version.
- Given theme, favorite, pantry, shopping, selected ingredient, or usage history changes, when recipes sync, then those fields are absent from the request.
- Given a compact screen at 200% font scale, when collection and recipe controls render, then all actions remain reachable and recipe content does not overlap.
- Given the app is offline, when a cached collection is opened, then recipes remain readable and refresh failure is non-destructive.
- Given v0.6.6-Bata release is installed, when the matching v0.6.7-Bata release is installed, then package ID, increased version code, and repository-specific certificate allow cover installation.

## Delivery

- Version/tag: `v0.6.7-Bata`, `versionCode 14`, `versionName 0.6.7-Bata`
- Artifact: `LazySheepChef-v0.6.7-Bata-release.apk`
- Build output: Release APK only; no debug APK is generated, uploaded, or handed off.
- Verification: Core unit tests, Worker tests, XML parsing, independent Android source compilation, full Android CI, Worker health/API checks, release signing, metadata, asset inventory, and published-download hash comparison.
- Development memory: `docs/APP_DEVELOPMENT_MEMORY.md`
