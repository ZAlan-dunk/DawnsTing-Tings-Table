# Asset License Ledger

| Asset / use | Source or provenance | License / permission | Bundled | Decision | Verified |
|---|---|---|---|---|---|
| Sheep-chef visual direction | [Wikipedia: 懒羊羊](https://zh.wikipedia.org/wiki/%E6%87%92%E7%BE%8A%E7%BE%8A), accessed 2026-08-07 | Research reference only; no image copied | No | Use broad visual traits only | 2026-08-07 |
| `hero_lazy_sheep_chef.xml` | Original project vector refinement based on broad research cues | Original project work; no third-party file embedded | Yes | Approved for personal APK | 2026-08-07 |
| `ic_home_*.xml`, `ic_launcher_foreground.xml` | Original project vector refinement | Original project work; no third-party file embedded | Yes | Approved for personal APK | 2026-08-07 |
| Search-result screenshots and character artwork | Web search results | Terms not verified | No | Blocked from packaging | 2026-08-07 |
| `lazy-sheep-hero-v2.png` | User-provided chat attachment; locally upscaled and color-adjusted | User-authorized personal app use; original source and redistribution terms not verified | Historical Web prototype only | Retained in `docs/prototype/v0.6.2`; no longer bundled in Android from v0.6.3-Bata | 2026-08-08 |
| `lazy_sheep_hero_light.png`, `lazy_sheep_hero_night.png` | Android derivatives of the user-provided attachment | User-authorized personal app use; original source and redistribution terms not verified | Historical v0.6.2-Bata release only | Removed from current Android source and package in v0.6.3-Bata | 2026-08-08 |
| `yunfeng_special.json` recipe titles and source URLs | User-supplied public [Xiachufang favorites list](https://www.xiachufang.com/recipe_list/701949383779815425/); mobile pages accessed 2026-08-09 | Publicly visible factual directory metadata; recipe text and author information excluded | Yes, metadata only | Keep canonical source links and original collection order; do not present as original project recipes | 2026-08-09 |
| Yunfeng recipe cover URLs | `https://i2.chuimg.com/` URLs exposed by the supplied public list | Image redistribution terms not verified | No; loaded directly at runtime | Do not copy cover files into source, APK, CI artifacts, or Releases; show a local placeholder when unavailable | 2026-08-09 |
