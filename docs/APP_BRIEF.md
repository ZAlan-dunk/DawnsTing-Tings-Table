# App Brief

## Task

- Date: 2026-08-11
- Requested outcome: Add one-tap private cloud backup and confirmed restore so personal recipe data survives uninstall, non-coverable signature changes, or device replacement.
- Primary user and context: One recipient per APK, using a local-first Android cooking utility without a GitHub login.
- Non-goals: Accounts, automatic background sync, bundled-recipe upload, analytics, commercial modules, package-ID changes, or a UI redesign.

## Project Boundary

- Resolved project path: `D:\Agent\Githubstorage\DawnsTing-Tings-Table`
- Workspace status: This exact external repository is explicitly authorized for implementation, build output, push, and release.
- Repository owner/name: `ZAlan-dunk/DawnsTing-Tings-Table` (public)
- Branch and upstream: `main` -> `origin/main`
- Cloud comparison: Fetched on 2026-08-11; task-start `HEAD` matched `origin/main`. Three uncommitted backup files from the active task were preserved and reviewed.

## Stable Identity

- Package ID and namespace: `com.dawns.tingstable`
- Artifact base: `LazySheepChef`
- Installed and in-app display name: `懒羊羊当大厨~`
- Signing: Continue the Ting repository's existing release certificate.
- Lulu rule: The paired repository keeps the same runtime app identity and behavior; only repository identity, APK filename, cloud profile, and its existing signing chain differ.

## Experience

- Primary flow: Open Home, tap Upload, wait for success, and verify the recent-upload time.
- Restore flow: Tap Restore, download and validate the latest backup, inspect its time and summary, confirm replacement, then reopen Home with restored data.
- Retained flows: Recipes, favorites, recipe editing, pantry, pantry matching, shopping list, specials, themes, source covers, and system Back behavior.
- Visual direction: Preserve the existing pale/dark restrained system. Add one unframed compact data row with a familiar cloud icon and 48dp Upload/Restore actions.
- Motion: Existing short press/page feedback only; network completion never depends on animation.

## Data Boundary

- Local-first: Existing SharedPreferences remain authoritative during normal use.
- Uploaded only on explicit action: Custom recipes, favorite IDs, pantry, shopping list, selected ingredients, and theme.
- Excluded: Built-in recipes, Yunfeng catalog and image cache, device/account identifiers, logs, and app files.
- Remote: Private GitHub repository, versioned JSON schema, maximum 900 KB, separate `tings` and `lulu` paths.
- Credential: Fine-grained token restricted to that repository with Contents read/write, injected through encrypted GitHub Secrets into release builds and never committed.

## Device Conditions

- Target: Native Android Java, Android 8.0/API 26 and later, compact phones through tablets.
- Input/layout: Touch, system font scaling up to 200%, light/night skins, status/navigation/IME insets.
- Offline: All existing core flows remain usable. Cloud actions show a readable error and leave local data unchanged.

## Acceptance Criteria

- Given personal data exists, when Upload is tapped, then the corresponding profile file is created or replaced and Home shows the completion time.
- Given an upload is in progress, when the request is pending, then a non-ambiguous progress state prevents duplicate actions.
- Given a valid cloud backup exists, when Restore is tapped, then time and item summary appear before any local write.
- Given the user cancels restore, when the dialog closes, then local data remains byte-for-byte unchanged.
- Given restore is confirmed, when all writes succeed, then personal data and theme persist after Activity recreation while built-in recipes remain available.
- Given network, authorization, profile, schema, size, or parsing validation fails, when the operation ends, then local data remains unchanged and a readable error is shown.
- Given a write fails after restore begins, when rollback runs, then the pre-restore snapshot is attempted and failure is reported.
- Given 200% font scale on a compact device, when Home renders, then both cloud actions and status remain reachable without covering other controls.
- Given a non-Home page or Home double-Back flow, when system Back is used, then v0.6.5 behavior remains unchanged.
- Given v0.6.5-Bata release is installed, when the matching v0.6.6-Bata release is installed, then package ID, increased version code, and unchanged repository-specific signature permit cover installation.

## Delivery

- Version/tag: `v0.6.6-Bata`, `versionCode 13`, `versionName 0.6.6-Bata`
- Artifact: `LazySheepChef-v0.6.6-Bata-release.apk`
- Build output: Release APK only; no debug APK is generated or handed off.
- Verification: Backup unit tests, existing unit tests, Debug/Release lint, release assembly, APK metadata, permission, token-presence, signature, API smoke, and retained-release inventory.
- Development memory: `docs/APP_DEVELOPMENT_MEMORY.md`
