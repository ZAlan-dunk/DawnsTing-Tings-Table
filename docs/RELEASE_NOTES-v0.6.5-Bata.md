## Updates

- Fixed Android system Back navigation so in-app pages return to the previous surface.
- Added a two-step Home exit flow: first Back shows a hint, second Back opens an exit confirmation.
- Incremented the Android version to `versionCode 12` / `versionName 0.6.5-Bata` while retaining the continuous release signing certificate.

## Functions

- Preserves recipe, pantry, specials, shopping list, and form navigation state while moving backward.
- Allows the v0.6.5-Bata release APK to upgrade v0.6.4-Bata in place when the release APK is used.

## Verification

- Back state tests, unit tests, Debug/Release Lint, Debug/Release assembly, APK metadata, signing, and upgrade identity checks are required before publication.
