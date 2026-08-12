# App Development Memory

## Stable Identity

- Repository: `ZAlan-dunk/DawnsTing-Tings-Table`
- Package ID and namespace: `com.dawns.tingstable`
- Artifact convention: `LazySheepChef-vMAJOR.MINOR.PATCH-Bata-variant.apk`
- Installed display name: `懒羊羊当大厨~`
- In-app display name: `懒羊羊当大厨~`
- iOS delivery: GitHub Pages PWA installed through Safari Add to Home Screen; not a native IPA.

## Stable Preferences

- Platform and data: Native Android Java, local-first, no account, analytics, or ads; cloud synchronization contains numbered recipe collections and custom recipes only. Settings, favorites, pantry, shopping data, selected ingredients, and usage history remain local.
- UI and UX: Recipe content takes priority over persistent controls; advanced controls use progressive disclosure.
- Theme and typography: Warm paper, jade, cinnabar, old gold, readable Chinese system typography, and a text-first Android home Hero without character artwork.
- Motion and performance: Short purposeful motion with an immediate reduced-motion fallback; no continuous decorative animation.
- Visual system: Default to a restrained low-saturation palette; support a persistent in-app night skin without relying on system mode.
- Release: Preserve package identity, local-data compatibility, each repository's signing continuity, and all historical GitHub Releases. Do not build, upload, or hand off a debug APK unless explicitly requested.
- Cloud access: The release build receives the private GitHub token from the repository Actions Secret. Complete recipe-collection state is cached locally; a packaged key controls only whether a custom special is shown and editable on that device.
- PWA storage: Keep the optional GitHub token device-local and out of public Pages assets. Provide JSON export/import because iOS can evict browser storage.

## Iterations

### 2026-08-12 v0.6.8-Bata

- Changes: Added an iPhone/iPad installable PWA with standalone manifest, Apple touch icons, offline app shell, safe-area layout, light/night skins, compact recipe tools, local recipe/pantry/shopping flows, built-in specials, personal collections, optional direct GitHub sync, and JSON recovery.
- Decisions and new preferences: Keep Android v0.6.7 unchanged. Treat the iOS build as a PWA rather than a native app. Never inject the private repository token into public Pages JavaScript; users may store it locally on a device when cloud collections are needed.
- Verification: Eleven Node tests passed for sequential collection IDs, UUID preservation, special visibility, habit sorting, import/export, grouping, manifest, service-worker assets, JavaScript parsing, 15 built-ins, and 150 Yunfeng entries. GitHub Pages run `31564357076` passed after one-time Pages enablement; the public HTTPS page, manifest, service worker, profile, 15 built-ins, and 150 Yunfeng entries returned the expected values. Physical iPhone install and VoiceOver remain user/device checks.
- Commit, tag, and Release: Implementation commit `5be32ae` is pushed. Final `v0.6.8-Bata` tag and PWA source archive publication follow this verification record.

### 2026-08-11 v0.6.7-Bata

- Changes: Replaced whole-device restore with recipe-only collections; added sequential `Dew-xxxx` allocation, direct GitHub state synchronization, local `KKLLTL` visibility gating, explicit revision conflict handling, cuisine grouping, and local habit sorting.
- Decisions and new preferences: Existing built-in specials remain separate. Complete collection data may be downloaded before a key is entered; the App hides custom-special metadata and recipes until the packaged key is validated locally. Cloudflare Worker, scoped tokens, recovery codes, and Android Keystore collection credentials are not used.
- Verification: On 2026-08-12, 54 unit tests, Debug/Release Lint with 0 errors, and Release assembly passed with SDK 36; no debug APK was assembled. GitHub Actions run `31515499816` passed the cloud write probe, release signing, tests, Lint, assembly, and APK metadata checks. The signed APK keeps certificate SHA-256 `ae06e4523f23cd177fe22081c5ae9150b5e9533478de53584566ac22013f6752`; its SHA-256 is `b05254695526cc519e4e79dbd1adb88f18a84a679d1ac6e1e9fb9ab6d47214c7`. The private cloud state contains `KKLLTL` without an access key, and the published APK was re-downloaded byte-identically.
- Commit, tag, and Release: Collection commit `cc5441c` and direct-GitHub follow-up `751d8a2` are pushed. Tag `v0.6.7-Bata` points to `751d8a2`; its GitHub Pre-release is published with only `LazySheepChef-v0.6.7-Bata-release.apk`.

### 2026-08-11 v0.6.6-Bata

- Changes: Added one-tap private cloud upload, backup preview and confirmed restore for custom recipes, favorites, pantry, shopping list, selected ingredients, and theme; added separate Ting/Lulu cloud profiles and rollback-on-failure local restoration.
- Decisions and new preferences: Both repositories display `懒羊羊当大厨~`; Lulu differs only in repository identity, cloud profile, APK filename, and its existing independent signing chain. Cloud credentials remain encrypted repository Secrets and are never committed. Only release APKs are produced and published.
- Verification: GitHub Actions run `31455759267` passed the private-repository write probe, backup payload tests (round-trip data, profile mismatch, unsupported schema, and deduplication), catalog validation, Debug/Release Lint, release assembly, APK signing, and package/name/version metadata checks. The published APK uses package `com.dawns.tingstable`, version 13 / `0.6.6-Bata`, certificate SHA-256 `ae06e4523f23cd177fe22081c5ae9150b5e9533478de53584566ac22013f6752`, and APK SHA-256 `49dec99487ea27fb8394735b89403516b0a2e97d79c2cf59aa8f2de0f449f2ee`.
- Commit, tag, and Release: Implementation commit `6ff163f` and verification-record commit `991ffc5` are pushed to `main`; tag `v0.6.6-Bata` and its GitHub Pre-release are published with only `LazySheepChef-v0.6.6-Bata-release.apk`.

### 2026-08-09 v0.6.5-Bata

- Changes: Unified legacy and Android 13+ system Back dispatch, restored previous-surface navigation, and added a two-step Home exit confirmation.
- Decisions and new preferences: Keep release and debug APKs clearly separated; only the release APK is intended for cover installation. For future handoffs, provide the release APK only unless debug output is explicitly requested. Preserve the v0.6.0 release certificate and increment the version code.
- Verification: 36 unit tests passed; Debug/Release Lint passed with 0 errors; Debug/Release APK assembly passed; package `com.dawns.tingstable` reports version 12 / `0.6.5-Bata`; the release certificate SHA-256 remains `ae06e4523f23cd177fe22081c5ae9150b5e9533478de53584566ac22013f6752`; the release APK SHA-256 is `543dc86690431cfda348607cbed50aa60184577b3fb73c8991ba6293d9682fad`; the remote Release asset was re-downloaded and matched the local hash.
- Commit, tag, and Release: Source commit `f4086bf` is pushed to `main`; tag `v0.6.5-Bata` and its GitHub Pre-release are published with only `LazySheepChef-v0.6.5-Bata-release.apk`.

### 2026-08-09 v0.6.4-Bata

- Changes: Renamed Fengyue Special to Yunfeng Special; imported 150 ordered recipe titles, canonical source links, and remote cover references; added adaptive source cards, bounded cover caching, invalid-host rejection, and restored special-detail state after configuration changes.
- Decisions and new preferences: Do not bundle third-party recipe text, author information, or cover files in the public APK. Keep source covers remote and open full recipes in the system browser.
- Verification: The structured importer reproduced all 150 IDs, titles, and cover URLs in the same online order; the first cover returned JPEG successfully. Local and CI checks passed: 32 unit tests, Debug/Release Lint, Debug/Release APK assembly, catalog validation, and signed-APK verification. The release APK reports package `com.dawns.tingstable`, version 11 / `0.6.4-Bata`, the expected `INTERNET` permission, and certificate SHA-256 `ae06e4523f23cd177fe22081c5ae9150b5e9533478de53584566ac22013f6752`.
- Commit, tag, and Release: Source commit `490d979` pushed to `main`; CI run `31296224643` passed; tag `v0.6.4-Bata` and its GitHub Pre-release are published.

### 2026-08-08 v0.6.3-Bata

- Changes: Removed the character image from the Android home Hero, simplified the Hero to text and live kitchen status, and removed the two unused Android raster resources.
- Decisions and new preferences: Keep the Android home Hero text-first in both skins; preserve the user-provided image only in the historical v0.6.2 Web prototype and historical releases.
- Verification: MainActivity Java parsing, 26 XML resources, removed-resource references, version identity, manifest permissions, diff checks, and 29 local unit tests passed. GitHub Actions run `31209408942` passed unit tests, Debug/Release Lint, Debug/Release assembly, artifact upload, and release signing. The release APK reports version 10 / 0.6.3-Bata, contains no removed Hero asset, requests no network permission, and uses certificate SHA-256 `ae06e4523f23cd177fe22081c5ae9150b5e9533478de53584566ac22013f6752`.
- Commit, tag, and Release: Implementation commit `3177608` and metadata commit `5e2b225` are pushed; tag `v0.6.3-Bata` and its GitHub Pre-release are published after final CI run `31209824142`.

### 2026-08-07 v0.6.1-Bata

- Changes: Reduced the palette to muted sage, dusty rose, and oat accents; added persistent light/night skin switching; refined the sheep-chef hero, launcher art, and home icon family; added the “漂亮嘞女明星～” microcopy.
- Decisions and new preferences: Keep third-party character images out of the APK; use original vector refinements informed by public visual research. The default skin remains light and quiet.
- Verification: 26 XML resources parsed; `ThemeMode` compiled locally; GitHub Actions run `31190373790` passed unit tests, Debug/Release lint, Debug/Release assembly, and `apksigner` verification. Device screenshots and TalkBack remain unavailable.
- Commit, tag, and Release: Commit `3ca17f4` pushed to `main`; tag `v0.6.1-Bata` and its GitHub Pre-release are published.

### 2026-08-05 v0.6.0-Bata

- Changes: Added compact recipe search/filter panels, semantic home icons, an original sheep-chef hero, directional navigation motion, and recipe-list position restoration.
- Decisions and new preferences: Keep the Java/View stack; use local vector assets rather than adding a UI framework.
- Verification: 7 browse-state tests passed; 24 XML resources parsed; drawable references, manifest permissions, version identity, diff checks, and independent static review passed. GitHub Actions run `31004467704` also passed unit tests, Debug/Release lint, and Debug/Release assembly.
- Commit, tag, and Release: Feature commit `f93a7fa`, CI signing commit `5fcc29f`, and certificate verification commit `2e7f9e8` are pushed to `main`; `v0.6.0-beta` is published as a GitHub Pre-release.
- Known gaps: Physical-device visual/TalkBack checks remain unavailable in the current environment. The new release signing key is stored only in GitHub encrypted Secrets.

### 2026-08-07 Web prototype v0.6.2 draft

- Changes: Added a standalone low-color web prototype with a pale neutral Hero, one restrained accent, monochrome entry cards, compact recipe controls, and a real light/night skin toggle.
- Decision: Do not continue polishing the APK from the current dark Hero; obtain visual approval on the prototype first, then back-port the confirmed tokens and layout.
- Verification: HTML parsed and inline JavaScript syntax checked. Screenshots and GUI automation were not run under the active safety policy.
- Commit and release: Prototype commit pending; no APK release created for this draft.

### 2026-08-08 Web prototype image pass

- Changes: Replaced the flat vector Hero mascot with a user-provided raster image, locally upscaled to 2400x1350 and color-adjusted for the light and night skins.
- Decision: Prefer the supplied character image over further flat redraws. Keep the asset in the Web prototype until visual approval and source-permission confirmation; do not package it in Android yet.
- Verification: Asset dimensions, PNG decode, HTML references, and JavaScript syntax checked. Screenshots and GUI automation were not run under the active safety policy.
- Commit and release: Pending visual approval; no APK release created for this pass.

### 2026-08-08 Web prototype contrast pass

- Changes: Darkened action-card borders, icon wells, selected navigation, skin control, and supporting text; fixed action-card titles inheriting the muted description color.
- Decision: Increase control discoverability without returning to a multi-color or high-contrast visual system.
- Verification: CSS token references, inline JavaScript syntax, and diff checks passed. Screenshots and GUI automation were not run under the active safety policy.
- Commit and release: Pending visual approval; no APK release created for this pass.

### 2026-08-08 v0.6.2-Bata

- Changes: Back-ported the approved Web direction to native Android: pale top bar and Hero surface, explicit light/night raster Hero resources, restrained semantic tokens, two-column home actions, and clearer button/navigation contrast.
- Decisions and new preferences: Prefer the supplied character image over flat redraws; select light/night artwork through the persistent in-app `ThemeMode`, not the system night resource qualifier. Preserve the Java/View stack, package identity, data keys, offline behavior, and continuous test signing route.
- Verification: MainActivity Java parsing, 27 XML resources, image dimensions and decode, resource references, HTML/JavaScript, manifest permissions, version identity, diff checks, and light/night contrast checks passed locally. GitHub Actions run `31199878686` passed unit tests, Debug/Release Lint, Debug/Release assembly, artifact upload, and `apksigner`; its certificate SHA-256 `ae06e4523f23cd177fe22081c5ae9150b5e9533478de53584566ac22013f6752` matches v0.6.1. APK metadata and the no-network-permission check passed locally. Device screenshots and TalkBack remain unavailable.
- Commit, tag, and Release: Implementation commit `f2e1846` is pushed; the final metadata follow-up is tagged `v0.6.2-Bata` and published after its CI pass.
