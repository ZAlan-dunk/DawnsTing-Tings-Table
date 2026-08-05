## Updates

- Compressed persistent recipe controls into icon-triggered search and filter panels.
- Added semantic home icons, an original sheep-chef hero, and directional page transitions.

## Functions

- Search and filter recipes without permanently occupying the result area.
- Open a pantry-aware recipe recommendation from the home hero.
- Preserve existing offline recipe, pantry, favorite, and shopping-list data.

## Verification

- GitHub Actions run `31004467704` passed unit tests, Debug/Release lint, and Debug/Release assembly.
- Manifest permission review found no requested permissions, including no `INTERNET` permission.
- The new v0.6 signing key is stored in GitHub encrypted Secrets; `v0.6.0-beta` is published as a Pre-release with the signed APK and SHA-256 file.
- Because this is a new signing identity, v0.5 and earlier installs must be uninstalled before installation.
