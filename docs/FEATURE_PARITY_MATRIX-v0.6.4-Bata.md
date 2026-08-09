# Feature Parity Matrix v0.6.4-Bata

| Flow | v0.6.3-Bata | v0.6.4-Bata | Verification |
|---|---|---|---|
| Built-in and custom recipes | Local and offline | Unchanged | Existing unit and CI tests |
| Recipe search and filters | Compact progressive disclosure | Unchanged | Existing state tests |
| Pantry and matching | Local and offline | Unchanged | Existing matcher tests |
| Shopping list | Local and offline | Unchanged | Existing repository behavior |
| Light/night skins | Persistent | Unchanged | Existing theme tests |
| Ting Special | Empty placeholder | Unchanged | Source inspection |
| Second special title | Fengyue Special | Yunfeng Special | Text assertion |
| Yunfeng content | Empty placeholder | 150 ordered source entries | JSON and CI catalog validation |
| Yunfeng covers | None | HTTPS source images with placeholder and bounded cache | Host tests and runtime request check |
| Yunfeng recipe action | None | Opens canonical mobile source URL | URL validation tests |
| Core offline behavior | Fully offline | Preserved; Yunfeng covers are optional network content | Manifest and flow review |
