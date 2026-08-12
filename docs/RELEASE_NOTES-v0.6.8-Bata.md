# v0.6.8-Bata Web Version Notes

## Updates

- Added an installable iPhone/iPad PWA deployed through GitHub Pages.
- Added standalone metadata, Apple touch icons, safe-area layout, light/night skins, reduced motion, and offline application-shell caching.
- Migrated recipe browsing, compact search/filter, cuisine grouping, habit sorting, pantry matching, shopping list, built-in specials, and personal recipe collections.
- Added local JSON export/import for PWA data recovery.

## Functions

- Install from iPhone Safari with Share -> Add to Home Screen.
- Use bundled recipes and the cached application shell after the first successful online load.
- Optionally configure a GitHub token on the current device to synchronize private numbered recipe collections.
- Keep theme, favorites, pantry, shopping list, and usage history local to the current PWA installation.

## Limits

- This web version is served only from the repository's GitHub Pages URL. It has no ZIP, IPA, Git tag, or GitHub Release.
- GitHub Releases are reserved for signed Android APK downloads.
- iOS may evict browser storage. Export important local data or use the optional recipe-collection sync.
- Physical iPhone installation, VoiceOver, keyboard, orientation, and storage-eviction checks remain device verification items.
