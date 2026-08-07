# 懒羊羊当大厨～ v0.6.3-Bata 首页图片移除

## 版本与边界

- `versionCode 10` / `versionName 0.6.3-Bata`。
- 包名与命名空间保持 `com.dawns.tingstable`。
- 保持现有本地数据、菜谱搜索筛选、菜篮、特典和采购清单兼容。
- 不增加账号、联网、分析、广告或 `INTERNET` 权限。

## 首页

- 移除 Android 首页顶部 Hero 中的懒羊羊角色图片。
- Hero 在淡色和黑夜皮肤中均使用纯文字与实时厨房状态布局。
- 保留“漂亮嘞女明星～”、菜篮数量、可制作数量和推荐入口。
- 保留 Hero 点击后打开推荐菜谱或菜篮的行为及无障碍描述。
- 删除 Android 中不再使用的淡色和黑夜角色位图，不修改历史 Web 原型。

## 验收

- 首页源码不再引用 `lazy_sheep_hero_light` 或 `lazy_sheep_hero_night`。
- Android 当前资源和构建产物不包含两张 Hero 角色位图。
- 单元测试、Debug/Release Lint、Debug/Release 构建通过。
- Release APK 使用连续测试签名，元数据为版本 10 / 0.6.3-Bata，且没有网络权限。
- 旧 GitHub Releases 和安装包保持不变，新建 `v0.6.3-Bata` Pre-release。
