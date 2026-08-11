# App Brief

## Task

- Date: 2026-08-12
- Requested outcome: Add shared cloud recipe collections, sequential standard numbering, a locally unlocked custom special, cuisine grouping, and local habit sorting.
- Primary users: A small group of personal Android users who do not need an app account.
- Non-goals: Social features, analytics, advertising, background synchronization, built-in-special migration, package-ID changes, or uploading personal settings.

## Project Boundary

- App repository: `D:\\Agent\\Githubstorage\\DawnsTing-Tings-Table`
- Paired repository: `D:\\Agent\\Githubstorage\\Dawnsdew-Lulu-s-Table`
- Private data repository: `ZAlan-dunk/Dawnsdew-Recipe-Cloud`
- Cloud file: `collections/state.json`
- Runtime route: The App calls the GitHub Contents API directly. It does not require a Cloudflare Worker.

## Stable Identity

- Package ID and namespace: `com.dawns.tingstable`
- Artifact base: `LazySheepChef`
- Installed and in-app display name: `懒羊羊当大厨~`
- Signing: Continue this repository's existing release certificate.
- Copy rule: Ting and Lulu keep the same runtime behavior; only repository identity, APK filename, cloud migration profile, and signing chain differ.

## Experience

- Home exposes Personal Recipe Collections as a first-level action.
- The collection hub first renders the local cache, then explicitly refreshes the complete cloud state.
- Standard collections are numbered `Dew-0001`, `Dew-0002`, and so on using optimistic-lock retries.
- Entering the packaged special key unlocks `KKLLTL` locally and reveals `露露的小厨房`; its name and recipes remain editable.
- Without the key, custom-special data may be present in the private local cache but is excluded from collection lists, restored pages, and recipe lookup.
- Recipe browsing retains compact search and filter icons, cuisine grouping, habit sorting, and hierarchical Back behavior.

## Data Boundary

- Synced: Collection ID, name, type, revision, timestamps, and custom recipes.
- Local only: Theme, favorites, pantry, shopping list, selected ingredients, view mode, usage counts, and last-opened times.
- Legacy: Historical v0.6.6 backup files remain unchanged. The first standard collection for each profile may import `customRecipes` once.
- Build configuration: The GitHub token is supplied by the existing repository Actions Secret and injected into the release build. The custom-special ID, default name, and key are packaged in the App for local validation.
- Offline: Cached visible collections remain readable. Failed refreshes do not discard local data.

## Acceptance Criteria

- Concurrent standard creation retries GitHub SHA conflicts and never reuses an occupied or earlier `Dew-xxxx` number.
- Same-name recipes with different UUIDs remain distinct.
- A refresh without a special key downloads and caches the complete state but does not display custom-special metadata or recipes.
- A correct local key reveals `KKLLTL`; an incorrect key does not reveal it.
- Saved page state and direct recipe lookup cannot bypass the special visibility rule.
- A revision conflict preserves local edits until the user chooses the cloud or local version.
- Theme, favorites, pantry, shopping, selected ingredients, and usage history are absent from collection payloads.
- v0.6.7-Bata cover-installs v0.6.6-Bata when the matching repository certificate is used.

## Delivery

- Version/tag: `v0.6.7-Bata`, `versionCode 14`, `versionName 0.6.7-Bata`
- Artifact: `LazySheepChef-v0.6.7-Bata-release.apk`
- Build output: Release APK only. No debug APK is assembled, uploaded, or handed off.
- Verification: Unit tests, Debug/Release Lint, Release assembly, GitHub write probe, release signing, APK metadata, asset inventory, and published-download hash comparison.
- Development memory: `docs/APP_DEVELOPMENT_MEMORY.md`
