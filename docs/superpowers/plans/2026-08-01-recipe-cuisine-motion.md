# 菜系组合筛选与交互动效 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为懒羊羊当大厨 v0.5 增加兼容旧数据的菜系与烹饪方式组合筛选，并让菜谱列表、收藏和页面切换获得稳定、克制、可降级的交互反馈。

**Architecture:** 保留原生 Android Views 和单 Activity 架构，把菜系归一化、组合筛选和浏览状态抽成纯 Java 单元；菜谱页使用 `RecyclerView + DiffUtil` 局部更新；`MotionSpec` 统一管理 Android 动画并尊重系统动画开关。现有 `category` JSON 键保持不变，新增 `cuisine` 键实现向后兼容。

**Tech Stack:** Java 17、Android Views、AndroidX Core 1.17.0、RecyclerView 1.4.0、JUnit 4.13.2、Gradle 8.14.3、AGP 8.13.0、Android SDK 36。

## Global Constraints

- 最低系统保持 Android 8.0 / API 26，目标 SDK 保持 36。
- `applicationId` 保持 `com.dawns.tingstable`；不新增 `INTERNET` 或其他权限。
- `versionCode` 更新为 6，`versionName` 更新为 `0.5-beta`。
- 旧收藏、自定义菜谱、菜篮和采购清单必须继续读取；缺失菜系的旧菜谱归入“家常融合”。
- 不重写为 Compose，不重做菜篮、特典和清单业务布局，不加入循环装饰动画。
- 所有构建缓存、测试输出、APK 和发布暂存位于 `F:\AAAASMWORK\AgentProject\DawnsTing-Tings-Table`。
- 发布前新 APK 证书 SHA-256 必须与 v0.4 发布 APK一致；只新增 `v0.5-beta`，不删除或修改旧 Release/Tag。

---

### Task 1: 菜系模型与旧数据归一化

**Files:**
- Create: `app/src/main/java/com/dawns/tingstable/util/RecipeCuisines.java`
- Create: `app/src/test/java/com/dawns/tingstable/util/RecipeCuisinesTest.java`
- Modify: `app/src/main/java/com/dawns/tingstable/model/Recipe.java`
- Modify: `app/src/main/java/com/dawns/tingstable/data/RecipeRepository.java`
- Modify: `app/src/test/java/com/dawns/tingstable/util/RecipeCategoriesTest.java`
- Modify: `app/src/test/java/com/dawns/tingstable/util/RecipeMatcherTest.java`

**Interfaces:**
- Produces: `RecipeCuisines.normalize(String)`, `RecipeCuisines.all()`, `RecipeCuisines.editable()`。
- Produces: `Recipe.cuisine`，并让 `toJson()`/`fromJson()` 读写 `cuisine`。
- Consumes: 后续筛选器与表单只使用 `RecipeCuisines` 的固定值，不自行拼写菜系。

- [ ] **Step 1: 写菜系归一化失败测试**

```java
public class RecipeCuisinesTest {
    @Test public void missingCuisineMigratesToHomeFusion() {
        assertEquals("家常融合", RecipeCuisines.normalize(null));
        assertEquals("家常融合", RecipeCuisines.normalize("  "));
    }

    @Test public void unknownCuisineFallsBackToOther() {
        assertEquals("其他", RecipeCuisines.normalize("私房菜系"));
    }

    @Test public void editableCuisinesKeepProductOrder() {
        assertEquals(Arrays.asList("家常融合", "川菜", "湘菜", "粤菜", "江浙菜", "北方菜", "西式", "其他"),
                RecipeCuisines.editable());
    }
}
```

- [ ] **Step 2: 运行测试并确认因类缺失而失败**

Run: `gradlew.bat testDebugUnitTest --tests com.dawns.tingstable.util.RecipeCuisinesTest`

Expected: FAIL，编译器报告 `RecipeCuisines` 不存在。

- [ ] **Step 3: 实现固定菜系与归一化**

```java
public final class RecipeCuisines {
    public static final String ALL = "全部菜系";
    public static final String HOME_FUSION = "家常融合";
    public static final String SICHUAN = "川菜";
    public static final String HUNAN = "湘菜";
    public static final String CANTONESE = "粤菜";
    public static final String JIANGZHE = "江浙菜";
    public static final String NORTHERN = "北方菜";
    public static final String WESTERN = "西式";
    public static final String OTHER = "其他";

    private RecipeCuisines() {}

    public static List<String> editable() {
        return Arrays.asList(HOME_FUSION, SICHUAN, HUNAN, CANTONESE, JIANGZHE, NORTHERN, WESTERN, OTHER);
    }

    public static List<String> all() {
        List<String> values = new ArrayList<>();
        values.add(ALL);
        values.addAll(editable());
        return values;
    }

    public static String normalize(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) return HOME_FUSION;
        return editable().contains(trimmed) ? trimmed : OTHER;
    }
}
```

- [ ] **Step 4: 扩展 Recipe JSON 与内置菜谱标注**

为构造函数增加 `cuisine` 参数并执行 `RecipeCuisines.normalize(cuisine)`；`toJson()` 写入 `cuisine`；`fromJson()` 读取 `json.optString("cuisine")`。更新仓库 `r(...)` 辅助方法和 15 道内置菜谱，按设计规范中的映射传入菜系。更新已有测试构造器，显式传入 `RecipeCuisines.HOME_FUSION`。

- [ ] **Step 5: 运行菜系、分类和匹配测试**

Run: `gradlew.bat testDebugUnitTest --tests com.dawns.tingstable.util.RecipeCuisinesTest --tests com.dawns.tingstable.util.RecipeCategoriesTest --tests com.dawns.tingstable.util.RecipeMatcherTest`

Expected: PASS，0 failures。

- [ ] **Step 6: 提交数据模型变更**

```bash
git add app/src/main/java/com/dawns/tingstable/model/Recipe.java app/src/main/java/com/dawns/tingstable/data/RecipeRepository.java app/src/main/java/com/dawns/tingstable/util/RecipeCuisines.java app/src/test/java/com/dawns/tingstable/util
git commit -m "feat: add compatible recipe cuisine metadata"
```

### Task 2: 四维组合筛选与浏览状态

**Files:**
- Create: `app/src/main/java/com/dawns/tingstable/util/RecipeFilters.java`
- Create: `app/src/main/java/com/dawns/tingstable/model/RecipeBrowseState.java`
- Create: `app/src/test/java/com/dawns/tingstable/util/RecipeFiltersTest.java`
- Create: `app/src/test/java/com/dawns/tingstable/model/RecipeBrowseStateTest.java`

**Interfaces:**
- Produces: `RecipeFilters.filter(List<Recipe>, Set<String>, RecipeBrowseState)`。
- Produces: `RecipeBrowseState` 的范围、菜系、做法、查询和当前分类维度；`summary()` 与 `resetFilters()`。
- Consumes: `RecipeCategories.categoryFor(Recipe)`、`RecipeCuisines.normalize(String)`、`RecipeMatcher.queryMatchesIngredient(...)`。

- [ ] **Step 1: 写组合筛选失败测试**

测试数据包含家常炒菜、川菜炒菜、川菜炖煮和粤菜蒸菜，手工断言以下结果：

```java
@Test public void combinesScopeCuisineMethodAndQuery() {
    RecipeBrowseState state = new RecipeBrowseState();
    state.setScope(RecipeBrowseState.SCOPE_FAVORITES);
    state.setCuisine(RecipeCuisines.SICHUAN);
    state.setCookingMethod(RecipeCategories.STIR_FRY);
    state.setQuery("豆腐");

    List<Recipe> result = RecipeFilters.filter(recipes, Collections.singleton("mapo"), state);

    assertEquals(Collections.singletonList("麻婆豆腐"), names(result));
}

@Test public void queryMatchesCuisineMethodAndIngredientAlias() {
    assertEquals(1, RecipeFilters.filter(recipes, Collections.emptySet(), stateWithQuery("粤菜")).size());
    assertEquals(2, RecipeFilters.filter(recipes, Collections.emptySet(), stateWithQuery("炒菜")).size());
    assertEquals(1, RecipeFilters.filter(recipes, Collections.emptySet(), stateWithQuery("番茄")).size());
}
```

- [ ] **Step 2: 写浏览状态失败测试**

```java
@Test public void changingVisibleDimensionKeepsBothSelections() {
    RecipeBrowseState state = new RecipeBrowseState();
    state.setCuisine(RecipeCuisines.SICHUAN);
    state.setCookingMethod(RecipeCategories.STIR_FRY);
    state.setDimension(RecipeBrowseState.DIMENSION_METHOD);
    assertEquals("川菜 · 炒菜", state.summary());
}

@Test public void resetKeepsScopeButClearsQueryAndFacets() {
    RecipeBrowseState state = populatedFavoriteState();
    state.resetFilters();
    assertEquals(RecipeBrowseState.SCOPE_FAVORITES, state.getScope());
    assertEquals(RecipeCuisines.ALL, state.getCuisine());
    assertEquals(RecipeCategories.ALL, state.getCookingMethod());
    assertEquals("", state.getQuery());
}
```

- [ ] **Step 3: 运行两个新测试并确认因类缺失而失败**

Run: `gradlew.bat testDebugUnitTest --tests com.dawns.tingstable.util.RecipeFiltersTest --tests com.dawns.tingstable.model.RecipeBrowseStateTest`

Expected: FAIL，编译器报告 `RecipeFilters` 和 `RecipeBrowseState` 不存在。

- [ ] **Step 4: 实现 RecipeBrowseState**

状态类提供 `SCOPE_ALL`、`SCOPE_FAVORITES`、`SCOPE_CUSTOM`、`DIMENSION_CUISINE`、`DIMENSION_METHOD` 常量；setter 对 null/未知值回退到默认值。`summary()` 忽略“全部”项，无筛选时返回“全部菜系 · 全部做法”；`resetFilters()` 保留 scope 和 dimension，仅清除 query/cuisine/cookingMethod。

- [ ] **Step 5: 实现 RecipeFilters**

遍历输入列表，依次检查范围、`RecipeCuisines.normalize(recipe.cuisine)`、`RecipeCategories.categoryFor(recipe)` 和搜索匹配；匹配项按输入顺序加入新列表。查询统一 `trim().toLowerCase(Locale.ROOT)`，并覆盖菜名、口味、菜系、做法和食材。

- [ ] **Step 6: 运行全部单元测试**

Run: `gradlew.bat testDebugUnitTest`

Expected: PASS，0 failures。

- [ ] **Step 7: 提交筛选逻辑**

```bash
git add app/src/main/java/com/dawns/tingstable/model/RecipeBrowseState.java app/src/main/java/com/dawns/tingstable/util/RecipeFilters.java app/src/test/java/com/dawns/tingstable/model/RecipeBrowseStateTest.java app/src/test/java/com/dawns/tingstable/util/RecipeFiltersTest.java
git commit -m "feat: add composable recipe filters"
```

### Task 3: 菜谱列表、筛选控件与动效

**Files:**
- Create: `app/src/main/java/com/dawns/tingstable/util/MotionSpec.java`
- Modify: `app/src/main/java/com/dawns/tingstable/MainActivity.java`
- Modify: `app/build.gradle`

**Interfaces:**
- Consumes: `RecipeBrowseState` 保存和恢复所有菜谱浏览状态。
- Consumes: `RecipeFilters.filter(...)` 为 RecyclerView 提供结果。
- Produces: `MotionSpec.enter(View, float)`、`MotionSpec.crossfade(View, Runnable)`、`MotionSpec.favorite(View)`、`MotionSpec.press(View)`。

- [ ] **Step 1: 添加 RecyclerView 依赖并建立稳定列表外壳**

在 `app/build.gradle` 添加：

```gradle
implementation 'androidx.recyclerview:recyclerview:1.4.0'
```

把菜谱页改为固定筛选头 + 占满剩余高度的 RecyclerView；配置 `LinearLayoutManager`、稳定 ID 和默认 item animator。空状态作为列表区域的覆盖 View，结果为空时显示。

- [ ] **Step 2: 实现 RecipeListAdapter**

在 `MainActivity` 内增加私有 adapter 和 holder，条目包含 `Recipe` 与 `favorite`。`submitRecipes(...)` 使用 `DiffUtil.calculateDiff(...)`，ID 按 `recipe.id`，内容比较覆盖菜名、菜系、做法、口味、难度、时间、食材摘要和收藏状态。`onBindViewHolder` 复用现有卡片工厂，卡片点击打开详情，星标点击切换收藏并重新提交结果。

- [ ] **Step 3: 接入范围、维度、分类、摘要和重置**

范围按钮只修改 state.scope；“按菜系/按做法”只修改 state.dimension；分类选项分别修改 state.cuisine 或 state.cookingMethod。每次状态变化执行统一 `renderRecipeResults(boolean animate)`：更新所有选择态、摘要、结果数量、重置按钮、空状态和 adapter 数据，不调用 `showRecipes()` 重建页面。

- [ ] **Step 4: 接入搜索和自定义表单**

搜索 TextWatcher 更新 state.query 并提交结果，保持原 EditText 实例。表单新增菜系选择行，默认家常融合；编辑时读取 `existing.cuisine`；保存时同时传入 `formCuisine` 和 `formCategory`；未保存签名同时包含菜系和做法。

- [ ] **Step 5: 实现统一 MotionSpec**

```java
public final class MotionSpec {
    public static final long PAGE = 180L;
    public static final long FILTER = 140L;
    public static final long FAVORITE = 160L;
    public static final long PRESS = 90L;

    public static boolean enabled() {
        return ValueAnimator.areAnimatorsEnabled();
    }
}
```

`enter` 执行 alpha + 8dp translation；`crossfade` 在 70ms 淡出后执行更新，再 70ms 淡入；`favorite` 执行 0.86→1.08→1.0；`press` 用 touch down/up 在 90ms 内切换 0.98/1.0。所有方法在动画关闭时立即设置最终状态并执行回调。

- [ ] **Step 6: 更新状态保存与系统返回链路**

在 `onSaveInstanceState` 与 `restorePage` 保存/恢复 cuisine、cookingMethod、dimension、scope 和 query。详情返回菜谱页时使用恢复状态，导航栏重新进入菜谱页时保留当前筛选，首页“翻菜谱”显式重置为全部范围但保留用户选择的菜系/做法。

- [ ] **Step 7: 编译和 Lint 验证界面接线**

Run: `gradlew.bat testDebugUnitTest lintDebug assembleDebug`

Expected: BUILD SUCCESSFUL，Lint 0 errors，Debug APK 生成。

- [ ] **Step 8: 提交菜谱交互实现**

```bash
git add app/build.gradle app/src/main/java/com/dawns/tingstable/MainActivity.java app/src/main/java/com/dawns/tingstable/util/MotionSpec.java
git commit -m "feat: refine recipe filtering and motion"
```

### Task 4: 无障碍、版本与产品文档

**Files:**
- Modify: `app/src/main/java/com/dawns/tingstable/MainActivity.java`
- Modify: `app/build.gradle`
- Modify: `README.md`
- Modify: `CHANGELOG.md`
- Create: `docs/requirements-v0.5.md`

**Interfaces:**
- Produces: v0.5 的版本元数据、验收说明和发布说明来源。

- [ ] **Step 1: 修正颜色与触控尺寸**

把 `MUTED` 调整为 `#5D655F`、`GOLD` 调整为 `#806126`、`CINNABAR` 调整为 `#98493D`；重新计算它们在 PAPER/WHITE 上的对比度并确保小字使用的组合至少 4.5:1。筛选按钮、收藏、返回、新增和底部导航全部设置最小高度 48dp，维度与选择态提供 `contentDescription` 或可见文字。

- [ ] **Step 2: 更新版本号**

```gradle
versionCode 6
versionName '0.5-beta'
```

- [ ] **Step 3: 写 v0.5 需求与更新记录**

`docs/requirements-v0.5.md` 完整记录菜系列表、四维组合、动效时长、48dp、对比度、兼容和发布验收。README 当前版本改为 v0.5 并增加菜系组合筛选说明。CHANGELOG 在顶部新增 v0.5 测试版，保留所有旧版本内容原样。

- [ ] **Step 4: 运行文档与静态检查**

Run: `git diff --check`

Run: `rg -n "versionCode 6|versionName '0.5-beta'|v0.5|家常融合|组合筛选" app/build.gradle README.md CHANGELOG.md docs/requirements-v0.5.md`

Expected: 无空白错误，所有版本与功能标记命中。

- [ ] **Step 5: 提交版本与文档**

```bash
git add app/build.gradle app/src/main/java/com/dawns/tingstable/MainActivity.java README.md CHANGELOG.md docs/requirements-v0.5.md
git commit -m "docs: prepare Lazy Sheep Chef v0.5 beta"
```

### Task 5: 完整验证、合并、推送与发布

**Files:**
- Generate outside repo: `F:/AAAASMWORK/AgentProject/DawnsTing-Tings-Table/release/v0.5-beta/LazySheepChef-v0.5-Beta.apk`
- Generate outside repo: `F:/AAAASMWORK/AgentProject/DawnsTing-Tings-Table/release/v0.5-beta/SHA256.txt`

**Interfaces:**
- Consumes: v0.4 GitHub Release APK 证书和本机可用签名材料。
- Produces: main 分支提交、`v0.5-beta` Tag、GitHub Pre-release 和两个附件。

- [ ] **Step 1: 运行完整验证**

Run: `gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleRelease`

Expected: BUILD SUCCESSFUL，全部测试通过，Lint 0 errors，Debug/Release APK 均生成。

- [ ] **Step 2: 验证 APK 元数据与权限**

使用 SDK 36 的 `aapt2 dump badging`/`apkanalyzer manifest` 检查包名、versionCode 6、versionName 0.5-beta、minSdk 26、targetSdk 36，并确认清单没有 `android.permission.INTERNET`。

- [ ] **Step 3: 验证签名连续性**

下载 v0.4 Release APK 到 F 盘，使用 `apksigner verify --print-certs` 获取证书 SHA-256。用同一证书签署 v0.5 APK后再次检查；两个 SHA-256 必须完全一致。若本机没有匹配证书，停止 APK 发布，不上传不可覆盖安装的包。

- [ ] **Step 4: 合并到 main 并重复完整验证**

从主工作区拉取远端 main，快进或普通合并 `feat/v0.5-recipe-ux`，在合并后的 main 上重新执行 Step 1。任何失败都停止推送与发布。

- [ ] **Step 5: 推送 main 与新 Tag**

```bash
git push origin main
git tag -a v0.5-beta -m "懒羊羊当大厨 v0.5 测试版"
git push origin v0.5-beta
```

- [ ] **Step 6: 生成校验文件并创建新 Pre-release**

`SHA256.txt` 包含发布 APK 的小写 SHA-256 与文件名。使用：

```bash
gh release create v0.5-beta LazySheepChef-v0.5-Beta.apk SHA256.txt --repo ZAlan-dunk/DawnsTing-Tings-Table --prerelease --title "懒羊羊当大厨～ v0.5 测试版" --notes-file F:/AAAASMWORK/AgentProject/DawnsTing-Tings-Table/release/v0.5-beta/release-notes.md
```

发布说明从 CHANGELOG v0.5 段落生成到 F 盘暂存文件。命令只创建新 Release，不调用 `gh release delete`、`gh release edit` 或任何旧 Tag 删除命令。

- [ ] **Step 7: 远端只读复核**

Run: `gh release list --repo ZAlan-dunk/DawnsTing-Tings-Table --limit 10`

Run: `gh release view v0.5-beta --repo ZAlan-dunk/DawnsTing-Tings-Table --json url,isPrerelease,tagName,assets`

Expected: v0.1-beta 至 v0.5-beta 全部存在；v0.5 为 Pre-release，包含 APK 和 SHA256.txt。
