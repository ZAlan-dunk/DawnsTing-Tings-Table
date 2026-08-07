# App Development Memory

## Stable Identity

- Repository: `ZAlan-dunk/DawnsTing-Tings-Table`
- Package ID and namespace: `com.dawns.tingstable`
- Artifact convention: `LazySheepChef-vMAJOR.MINOR.PATCH-Bata-variant.apk`
- Installed display name: `懒羊羊当大厨～`
- In-app display name: `懒羊羊当大厨～`

## Stable Preferences

- Platform and data: Native Android Java, local-first, no account, analytics, ads, or runtime network dependency.
- UI and UX: Recipe content takes priority over persistent controls; advanced controls use progressive disclosure.
- Theme and typography: Warm paper, jade, cinnabar, old gold, readable Chinese system typography, and restrained original sheep-chef artwork.
- Motion and performance: Short purposeful motion with an immediate reduced-motion fallback; no continuous decorative animation.
- Visual system: Default to a restrained low-saturation palette; support a persistent in-app night skin without relying on system mode.
- Release: Preserve package identity, local-data compatibility, signing continuity, and all historical GitHub Releases.

## Iterations

### 2026-08-07 v0.6.1-Bata

- Changes: Reduced the palette to muted sage, dusty rose, and oat accents; added persistent light/night skin switching; refined the sheep-chef hero, launcher art, and home icon family; added the “漂亮嘞女明星～” microcopy.
- Decisions and new preferences: Keep third-party character images out of the APK; use original vector refinements informed by public visual research. The default skin remains light and quiet.
- Verification: XML/resource checks and theme-state unit tests are pending CI; device screenshots and TalkBack remain unavailable.
- Commit, tag, and Release: Pending for v0.6.1-Bata.

### 2026-08-05 v0.6.0-Bata

- Changes: Added compact recipe search/filter panels, semantic home icons, an original sheep-chef hero, directional navigation motion, and recipe-list position restoration.
- Decisions and new preferences: Keep the Java/View stack; use local vector assets rather than adding a UI framework.
- Verification: 7 browse-state tests passed; 24 XML resources parsed; drawable references, manifest permissions, version identity, diff checks, and independent static review passed. GitHub Actions run `31004467704` also passed unit tests, Debug/Release lint, and Debug/Release assembly.
- Commit, tag, and Release: Feature commit `f93a7fa`, CI signing commit `5fcc29f`, and certificate verification commit `2e7f9e8` are pushed to `main`; `v0.6.0-beta` is published as a GitHub Pre-release.
- Known gaps: Physical-device visual/TalkBack checks remain unavailable in the current environment. The new release signing key is stored only in GitHub encrypted Secrets.
