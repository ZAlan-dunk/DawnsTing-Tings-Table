# Feature Parity Matrix v0.6.6-Bata

| Flow | v0.6.5-Bata | v0.6.6-Bata | Verification |
|---|---|---|---|
| Personal data persistence | Device-local only | Device-local plus explicit private cloud upload/restore | Payload tests and restore smoke test |
| Upload | Unavailable | One tap from Home with progress and result states | API integration smoke test |
| Restore | Unavailable | Download, identity/schema validation, preview, confirmation, rollback attempt | Unit tests and clean-install smoke test |
| Built-in recipes and specials | Bundled/source-linked as before | Excluded from personal backup and preserved during restore | Payload inspection |
| Ting/Lulu isolation | Separate recipients but no backup identity | Separate `tings` and `lulu` cloud paths | Profile mismatch test |
| Display name | Historical full-width tilde or Lulu repository label | Both show `懒羊羊当大厨~` | Resource and APK label checks |
| Existing navigation and local flows | Available | Preserved | Existing tests and Android CI |
| Release output | Release plus development debug output in Ting CI | Release APK only | Workflow and Release asset inspection |
