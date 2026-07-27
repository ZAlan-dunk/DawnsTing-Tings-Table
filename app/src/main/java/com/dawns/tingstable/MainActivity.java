package com.dawns.tingstable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.dawns.tingstable.data.RecipeRepository;
import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int PAPER = Color.rgb(255, 249, 238);
    private static final int INK = Color.rgb(37, 53, 47);
    private static final int JADE = Color.rgb(85, 117, 104);
    private static final int JADE_DARK = Color.rgb(52, 78, 69);
    private static final int CINNABAR = Color.rgb(168, 75, 63);
    private static final int GOLD = Color.rgb(201, 154, 82);
    private static final int MUTED = Color.rgb(105, 112, 107);
    private static final int LINE = Color.rgb(224, 214, 195);

    private RecipeRepository repository;
    private FrameLayout content;
    private LinearLayout bottomNav;
    private TextView titleView;
    private Button backButton;
    private Runnable backAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new RecipeRepository(this);
        getWindow().setStatusBarColor(JADE_DARK);
        buildShell();
        showHome();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, this::handleBack);
        }
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(12), dp(8), dp(16), dp(8));
        top.setBackgroundColor(JADE_DARK);
        backButton = textButton("‹", true);
        backButton.setTextSize(30);
        backButton.setVisibility(View.GONE);
        top.addView(backButton, new LinearLayout.LayoutParams(dp(48), dp(48)));
        titleView = text("婷馔清欢", 21, Color.WHITE, true);
        top.addView(titleView, new LinearLayout.LayoutParams(0, dp(52), 1));
        TextView seal = text("婷", 18, Color.WHITE, true);
        seal.setGravity(Gravity.CENTER);
        seal.setBackground(roundRect(CINNABAR, 12, Color.TRANSPARENT));
        top.addView(seal, new LinearLayout.LayoutParams(dp(40), dp(40)));
        root.addView(top, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        bottomNav = new LinearLayout(this);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setPadding(dp(4), dp(5), dp(4), dp(5));
        bottomNav.setBackgroundColor(Color.WHITE);
        addNav(bottomNav, "首页", this::showHome);
        addNav(bottomNav, "配方", () -> showRecipes(false));
        addNav(bottomNav, "选菜", this::showIngredientPicker);
        addNav(bottomNav, "收藏", () -> showRecipes(true));
        addNav(bottomNav, "清单", this::showShoppingList);
        root.addView(bottomNav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(62)));
        setContentView(root);
    }

    private void addNav(LinearLayout nav, String label, Runnable action) {
        Button button = textButton(label, false);
        button.setTextSize(13);
        button.setTextColor(JADE_DARK);
        button.setOnClickListener(view -> action.run());
        nav.addView(button, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
    }

    private void setPage(String title, Runnable onBack, View view, boolean showNavigation) {
        titleView.setText(title);
        backAction = onBack;
        backButton.setVisibility(onBack == null ? View.GONE : View.VISIBLE);
        backButton.setOnClickListener(v -> { if (backAction != null) backAction.run(); });
        bottomNav.setVisibility(showNavigation ? View.VISIBLE : View.GONE);
        content.removeAllViews();
        content.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showHome() {
        LinearLayout body = vertical(dp(18));
        body.setPadding(dp(18), dp(20), dp(18), dp(28));

        LinearLayout hero = card();
        hero.setBackground(roundRect(JADE, 22, Color.TRANSPARENT));
        TextView small = text("TING'S TABLE · v0.1 测试版", 12, Color.rgb(235, 226, 202), true);
        hero.addView(small);
        TextView brand = text("婷馔清欢", 31, Color.WHITE, true);
        brand.setPadding(0, dp(8), 0, dp(4));
        hero.addView(brand);
        hero.addView(text("人间有味，四时清欢。", 17, Color.WHITE, false));
        TextView intro = text("从一桌家常滋味开始，也从手边已有的食材开始。", 14, Color.rgb(238, 242, 239), false);
        intro.setPadding(0, dp(14), 0, 0);
        hero.addView(intro);
        body.addView(hero, matchWrap(dp(18)));

        body.addView(section("今天想怎么做？"), matchWrap(dp(20)));
        body.addView(actionCard("配方表", "浏览家常菜谱，查看食材、用量与分步做法。", "查看全部配方", () -> showRecipes(false)), matchWrap(dp(10)));
        body.addView(actionCard("通过食材选择菜单", "勾选已经购入或家中现有的食材，看看现在能做什么。", "开始选择食材", this::showIngredientPicker), matchWrap(dp(12)));

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        Button favorites = outlineButton("我的收藏 · " + repository.getFavorites().size());
        favorites.setOnClickListener(v -> showRecipes(true));
        quick.addView(favorites, new LinearLayout.LayoutParams(0, dp(50), 1));
        addSpacer(quick, 10, 1);
        Button shopping = outlineButton("采购清单 · " + repository.getShoppingList().size());
        shopping.setOnClickListener(v -> showShoppingList());
        quick.addView(shopping, new LinearLayout.LayoutParams(0, dp(50), 1));
        body.addView(quick, matchWrap(dp(8)));

        TextView offline = text("离线可用 · 数据保存在本机 · 无需登录", 12, MUTED, false);
        offline.setGravity(Gravity.CENTER);
        offline.setPadding(0, dp(18), 0, 0);
        body.addView(offline);
        setPage("婷馔清欢", null, scroll(body), true);
    }

    private View actionCard(String title, String description, String buttonText, Runnable action) {
        LinearLayout box = card();
        box.addView(text(title, 22, INK, true));
        TextView desc = text(description, 14, MUTED, false);
        desc.setPadding(0, dp(8), 0, dp(14));
        box.addView(desc);
        Button button = primaryButton(buttonText);
        button.setOnClickListener(v -> action.run());
        box.addView(button, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        return box;
    }

    private void showRecipes(boolean favoritesOnly) {
        LinearLayout body = vertical(dp(12));
        body.setPadding(dp(16), dp(16), dp(16), dp(24));
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        EditText search = input("搜索菜名或食材");
        toolbar.addView(search, new LinearLayout.LayoutParams(0, dp(52), 1));
        addSpacer(toolbar, 10, 1);
        Button add = primaryButton("＋ 添加");
        add.setOnClickListener(v -> showRecipeForm(null));
        toolbar.addView(add, new LinearLayout.LayoutParams(dp(96), dp(52)));
        body.addView(toolbar);

        TextView hint = text(favoritesOnly ? "收藏的配方" : "内置家常菜与我的配方", 13, MUTED, false);
        hint.setPadding(0, dp(6), 0, 0);
        body.addView(hint);
        LinearLayout list = vertical(dp(10));
        body.addView(list);
        Runnable render = () -> renderRecipeRows(list, search.getText().toString(), favoritesOnly);
        render.run();
        search.addTextChangedListener(simpleWatcher(render));
        setPage(favoritesOnly ? "我的收藏" : "配方表", null, scroll(body), true);
    }

    private void renderRecipeRows(LinearLayout list, String query, boolean favoritesOnly) {
        list.removeAllViews();
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        Set<String> favorites = repository.getFavorites();
        int count = 0;
        for (Recipe recipe : repository.getAllRecipes()) {
            if (favoritesOnly && !favorites.contains(recipe.id)) continue;
            String haystack = recipe.name + " " + recipe.category + " " + recipe.flavor;
            for (Ingredient ingredient : recipe.ingredients) haystack += " " + ingredient.name;
            if (!normalized.isEmpty() && !haystack.toLowerCase(Locale.ROOT).contains(normalized)) continue;
            list.addView(recipeCard(recipe, () -> renderRecipeRows(list, query, favoritesOnly)), matchWrap(dp(10)));
            count++;
        }
        if (count == 0) list.addView(emptyState(favoritesOnly ? "还没有收藏配方" : "没有找到符合条件的配方"));
    }

    private View recipeCard(Recipe recipe, Runnable refresh) {
        LinearLayout box = card();
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout copy = vertical(2);
        copy.addView(text(recipe.name, 19, INK, true));
        copy.addView(text(recipe.category + " · " + recipe.flavor + " · " + recipe.minutes + "分钟 · " + recipe.difficulty, 12, MUTED, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button favorite = textButton(repository.isFavorite(recipe.id) ? "★" : "☆", false);
        favorite.setTextSize(25);
        favorite.setTextColor(repository.isFavorite(recipe.id) ? GOLD : MUTED);
        favorite.setOnClickListener(v -> { repository.toggleFavorite(recipe.id); refresh.run(); });
        row.addView(favorite, new LinearLayout.LayoutParams(dp(52), dp(48)));
        box.addView(row);
        TextView ingredientLine = text("食材：" + ingredientSummary(recipe), 13, MUTED, false);
        ingredientLine.setPadding(0, dp(10), 0, 0);
        box.addView(ingredientLine);
        if (recipe.custom) {
            TextView badge = text("我的配方", 11, CINNABAR, true);
            badge.setPadding(0, dp(8), 0, 0);
            box.addView(badge);
        }
        box.setOnClickListener(v -> showRecipeDetail(recipe));
        return box;
    }

    private String ingredientSummary(Recipe recipe) {
        List<String> names = new ArrayList<>();
        for (Ingredient ingredient : recipe.ingredients) if (!ingredient.staple) names.add(ingredient.name);
        return String.join("、", names);
    }

    private void showRecipeDetail(Recipe recipe) {
        Recipe fresh = repository.findById(recipe.id);
        if (fresh == null) { showRecipes(false); return; }
        LinearLayout body = vertical(dp(12));
        body.setPadding(dp(18), dp(18), dp(18), dp(30));

        LinearLayout heading = card();
        heading.addView(text(fresh.name, 28, INK, true));
        TextView meta = text(fresh.category + " · " + fresh.flavor + " · " + fresh.difficulty + " · " + fresh.minutes + "分钟 · " + fresh.servings + "人份", 13, MUTED, false);
        meta.setPadding(0, dp(8), 0, dp(12));
        heading.addView(meta);
        LinearLayout actions = new LinearLayout(this);
        Button favorite = primaryButton(repository.isFavorite(fresh.id) ? "取消收藏" : "收藏配方");
        favorite.setOnClickListener(v -> { repository.toggleFavorite(fresh.id); showRecipeDetail(fresh); });
        actions.addView(favorite, new LinearLayout.LayoutParams(0, dp(48), 1));
        if (fresh.custom) {
            addSpacer(actions, 10, 1);
            Button edit = outlineButton("编辑");
            edit.setOnClickListener(v -> showRecipeForm(fresh));
            actions.addView(edit, new LinearLayout.LayoutParams(0, dp(48), 1));
        }
        heading.addView(actions);
        body.addView(heading);

        body.addView(section("食材与用量"), matchWrap(dp(8)));
        LinearLayout ingredients = card();
        for (Ingredient ingredient : fresh.ingredients) {
            String suffix = ingredient.staple ? "  · 基础调味" : "";
            ingredients.addView(text("• " + ingredient.name + "　" + ingredient.amount + suffix, 15, INK, false), matchWrap(dp(4)));
        }
        body.addView(ingredients);

        body.addView(section("制作步骤"), matchWrap(dp(8)));
        int number = 1;
        for (String step : fresh.steps) {
            LinearLayout stepCard = card();
            TextView label = text(String.format(Locale.CHINA, "%02d", number), 13, CINNABAR, true);
            stepCard.addView(label);
            TextView detail = text(step, 16, INK, false);
            detail.setPadding(0, dp(7), 0, 0);
            stepCard.addView(detail);
            body.addView(stepCard, matchWrap(dp(8)));
            number++;
        }
        if (!fresh.tips.trim().isEmpty()) {
            body.addView(section("清欢小笺"), matchWrap(dp(8)));
            LinearLayout tips = card();
            tips.addView(text(fresh.tips, 15, INK, false));
            body.addView(tips);
        }
        if (fresh.custom) {
            Button delete = outlineButton("删除这道自定义配方");
            delete.setTextColor(CINNABAR);
            delete.setOnClickListener(v -> confirmDelete(fresh));
            body.addView(delete, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
        }
        setPage(fresh.name, () -> showRecipes(false), scroll(body), false);
    }

    private void confirmDelete(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("删除配方")
                .setMessage("确定删除“" + recipe.name + "”吗？此操作无法撤销。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    repository.deleteCustomRecipe(recipe.id);
                    toast("配方已删除");
                    showRecipes(false);
                }).show();
    }

    private void showIngredientPicker() {
        Set<String> selected = new LinkedHashSet<>(repository.getSelectedIngredients());
        LinearLayout body = vertical(dp(10));
        body.setPadding(dp(16), dp(16), dp(16), dp(28));
        LinearLayout intro = card();
        intro.addView(text("手边有什么，就从什么开始", 20, INK, true));
        TextView desc = text("基础油盐糖默认视为已有。匹配结果会区分“可以制作”和“还差一点”。", 13, MUTED, false);
        desc.setPadding(0, dp(8), 0, 0);
        intro.addView(desc);
        body.addView(intro);

        EditText search = input("搜索食材");
        body.addView(search, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        LinearLayout selectedSummary = vertical(0);
        LinearLayout checks = vertical(2);
        body.addView(selectedSummary);
        body.addView(checks);
        LinearLayout results = vertical(dp(8));

        Runnable updateSummary = () -> {
            selectedSummary.removeAllViews();
            TextView summary = text("已选择 " + selected.size() + " 种：" + (selected.isEmpty() ? "暂未选择" : String.join("、", selected)), 13, selected.isEmpty() ? MUTED : JADE_DARK, false);
            summary.setPadding(dp(4), dp(8), dp(4), dp(8));
            selectedSummary.addView(summary);
        };
        Runnable[] rebuildHolder = new Runnable[1];
        rebuildHolder[0] = () -> rebuildIngredientChecks(checks, search.getText().toString(), selected, updateSummary);
        rebuildHolder[0].run();
        updateSummary.run();
        search.addTextChangedListener(simpleWatcher(rebuildHolder[0]));

        LinearLayout actions = new LinearLayout(this);
        Button match = primaryButton("看看能做什么");
        match.setOnClickListener(v -> renderMatches(results, selected));
        actions.addView(match, new LinearLayout.LayoutParams(0, dp(50), 2));
        addSpacer(actions, 10, 1);
        Button clear = outlineButton("清空");
        clear.setOnClickListener(v -> {
            selected.clear();
            repository.saveSelectedIngredients(selected);
            rebuildHolder[0].run();
            updateSummary.run();
            results.removeAllViews();
        });
        actions.addView(clear, new LinearLayout.LayoutParams(0, dp(50), 1));
        body.addView(actions, matchWrap(dp(10)));
        body.addView(results);
        setPage("按食材选菜", null, scroll(body), true);
    }

    private void rebuildIngredientChecks(LinearLayout checks, String query, Set<String> selected, Runnable updateSummary) {
        checks.removeAllViews();
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        for (String name : repository.getAllIngredientNames()) {
            if (!normalized.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(normalized)) continue;
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(name);
            checkBox.setTextSize(15);
            checkBox.setTextColor(INK);
            checkBox.setPadding(dp(8), 0, dp(8), 0);
            checkBox.setBackground(roundRect(Color.WHITE, 12, LINE));
            checkBox.setChecked(selected.contains(name));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selected.add(name); else selected.remove(name);
                repository.saveSelectedIngredients(selected);
                updateSummary.run();
            });
            checks.addView(checkBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        }
    }

    private void renderMatches(LinearLayout results, Set<String> selected) {
        results.removeAllViews();
        if (selected.isEmpty()) {
            results.addView(emptyState("请至少选择一种食材"));
            return;
        }
        List<RecipeMatcher.Match> matches = RecipeMatcher.match(repository.getAllRecipes(), selected);
        if (matches.isEmpty()) {
            results.addView(emptyState("暂时没有接近的配方，可以继续添加食材或创建自己的配方。"));
            return;
        }
        results.addView(section("匹配结果 · " + matches.size() + " 道"));
        boolean shownReadyTitle = false;
        boolean shownAlmostTitle = false;
        for (RecipeMatcher.Match match : matches) {
            if (match.canCook() && !shownReadyTitle) {
                results.addView(text("可以制作", 15, JADE_DARK, true));
                shownReadyTitle = true;
            }
            if (!match.canCook() && !shownAlmostTitle) {
                TextView almost = text("还差一点", 15, CINNABAR, true);
                almost.setPadding(0, dp(10), 0, 0);
                results.addView(almost);
                shownAlmostTitle = true;
            }
            LinearLayout box = card();
            box.addView(text(match.recipe.name, 19, INK, true));
            String status = match.canCook() ? "食材已满足 · 可以制作" : "匹配 " + match.percent + "% · 缺少：" + String.join("、", match.missing);
            TextView statusView = text(status, 13, match.canCook() ? JADE_DARK : CINNABAR, false);
            statusView.setPadding(0, dp(7), 0, dp(8));
            box.addView(statusView);
            if (!match.missing.isEmpty()) {
                Button buy = outlineButton("加入采购清单");
                buy.setOnClickListener(v -> {
                    repository.addShoppingItems(match.missing);
                    toast("已加入采购清单");
                });
                box.addView(buy, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
            }
            box.setOnClickListener(v -> showRecipeDetail(match.recipe));
            results.addView(box, matchWrap(dp(8)));
        }
    }

    private void showShoppingList() {
        LinearLayout body = vertical(dp(10));
        body.setPadding(dp(16), dp(16), dp(16), dp(26));
        Set<String> shopping = repository.getShoppingList();
        LinearLayout top = card();
        top.addView(text("采购清单", 23, INK, true));
        top.addView(text("勾选已经买到的食材，即可从清单移除。", 13, MUTED, false));
        body.addView(top);
        if (shopping.isEmpty()) {
            body.addView(emptyState("采购清单还是空的。可以在食材匹配结果中加入缺少的食材。"));
        } else {
            for (String item : new ArrayList<>(shopping)) {
                CheckBox box = new CheckBox(this);
                box.setText(item);
                box.setTextSize(16);
                box.setTextColor(INK);
                box.setPadding(dp(12), 0, dp(12), 0);
                box.setBackground(roundRect(Color.WHITE, 12, LINE));
                box.setOnCheckedChangeListener((button, checked) -> {
                    if (checked) {
                        repository.removeShoppingItem(item);
                        showShoppingList();
                    }
                });
                body.addView(box, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
            }
            Button clear = outlineButton("清空采购清单");
            clear.setTextColor(CINNABAR);
            clear.setOnClickListener(v -> {
                repository.clearShoppingList();
                showShoppingList();
            });
            body.addView(clear, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)));
        }
        setPage("采购清单", null, scroll(body), true);
    }

    private void showRecipeForm(Recipe existing) {
        boolean editing = existing != null;
        LinearLayout body = vertical(dp(10));
        body.setPadding(dp(16), dp(16), dp(16), dp(28));
        body.addView(text(editing ? "编辑我的配方" : "记录一道新配方", 23, INK, true));

        EditText name = labeledInput(body, "菜名 *", editing ? existing.name : "");
        EditText category = labeledInput(body, "分类", editing ? existing.category : "家常菜");
        EditText flavor = labeledInput(body, "口味", editing ? existing.flavor : "家常");
        EditText difficulty = labeledInput(body, "难度", editing ? existing.difficulty : "简单");
        EditText minutes = labeledInput(body, "烹饪时间（分钟）", editing ? String.valueOf(existing.minutes) : "20");
        minutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText servings = labeledInput(body, "适合人数", editing ? String.valueOf(existing.servings) : "2");
        servings.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText ingredients = labeledArea(body, "食材 *（每行一种，格式：名称 | 用量；基础调味料前加 *）", editing ? ingredientsForEdit(existing) : "");
        EditText steps = labeledArea(body, "制作步骤 *（每行一步）", editing ? String.join("\n", existing.steps) : "");
        EditText tips = labeledArea(body, "小贴士", editing ? existing.tips : "");

        Button save = primaryButton(editing ? "保存修改" : "添加到配方表");
        save.setOnClickListener(v -> {
            String recipeName = name.getText().toString().trim();
            List<Ingredient> parsedIngredients = parseIngredients(ingredients.getText().toString());
            List<String> parsedSteps = parseLines(steps.getText().toString());
            if (recipeName.isEmpty() || parsedIngredients.isEmpty() || parsedSteps.isEmpty()) {
                toast("请填写菜名、食材和制作步骤");
                return;
            }
            Recipe recipe = new Recipe(
                    editing ? existing.id : "",
                    recipeName,
                    valueOr(category, "家常菜"), valueOr(flavor, "家常"), valueOr(difficulty, "简单"),
                    positiveInt(minutes, 20), positiveInt(servings, 2),
                    parsedIngredients, parsedSteps, tips.getText().toString().trim(), true
            );
            repository.saveCustomRecipe(recipe);
            toast(editing ? "配方已更新" : "配方已添加");
            showRecipes(false);
        });
        body.addView(save, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        setPage(editing ? "编辑配方" : "添加配方", () -> { if (editing) showRecipeDetail(existing); else showRecipes(false); }, scroll(body), false);
    }

    private String ingredientsForEdit(Recipe recipe) {
        List<String> lines = new ArrayList<>();
        for (Ingredient ingredient : recipe.ingredients) lines.add((ingredient.staple ? "*" : "") + ingredient.name + " | " + ingredient.amount);
        return String.join("\n", lines);
    }

    private List<Ingredient> parseIngredients(String value) {
        List<Ingredient> result = new ArrayList<>();
        for (String raw : value.split("\r?\n")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            boolean staple = line.startsWith("*");
            if (staple) line = line.substring(1).trim();
            String[] parts = line.split("\\|", 2);
            String name = parts[0].trim();
            if (!name.isEmpty()) result.add(new Ingredient(name, parts.length > 1 ? parts[1].trim() : "适量", staple));
        }
        return result;
    }

    private List<String> parseLines(String value) {
        List<String> result = new ArrayList<>();
        for (String line : value.split("\r?\n")) if (!line.trim().isEmpty()) result.add(line.trim());
        return result;
    }

    private EditText labeledInput(LinearLayout parent, String label, String value) {
        parent.addView(text(label, 13, MUTED, true));
        EditText field = input(label);
        field.setText(value);
        parent.addView(field, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        return field;
    }

    private EditText labeledArea(LinearLayout parent, String label, String value) {
        parent.addView(text(label, 13, MUTED, true));
        EditText field = input(label);
        field.setSingleLine(false);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        field.setGravity(Gravity.TOP | Gravity.START);
        field.setMinLines(4);
        field.setPadding(dp(14), dp(12), dp(14), dp(12));
        field.setText(value);
        parent.addView(field, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return field;
    }

    private String valueOr(EditText field, String fallback) {
        String value = field.getText().toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private int positiveInt(EditText field, int fallback) {
        try { return Math.max(1, Integer.parseInt(field.getText().toString().trim())); }
        catch (Exception ignored) { return fallback; }
    }

    private TextWatcher simpleWatcher(Runnable action) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { action.run(); }
            @Override public void afterTextChanged(Editable s) { }
        };
    }

    private LinearLayout vertical(int spacingDp) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        if (spacingDp > 0) layout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        if (spacingDp > 0) {
            GradientDrawable divider = new GradientDrawable();
            divider.setSize(1, dp(spacingDp));
            divider.setColor(Color.TRANSPARENT);
            layout.setDividerDrawable(divider);
        }
        return layout;
    }

    private LinearLayout card() {
        LinearLayout card = vertical(0);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(roundRect(Color.WHITE, 18, LINE));
        return card;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(0, 1.15f);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private TextView section(String value) {
        TextView view = text(value, 17, JADE_DARK, true);
        view.setPadding(dp(2), dp(8), 0, dp(6));
        return view;
    }

    private EditText input(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(15);
        field.setTextColor(INK);
        field.setHintTextColor(Color.rgb(145, 145, 140));
        field.setSingleLine(true);
        field.setPadding(dp(14), 0, dp(14), 0);
        field.setBackground(roundRect(Color.WHITE, 12, LINE));
        return field;
    }

    private Button primaryButton(String label) {
        Button button = textButton(label, true);
        button.setTextColor(Color.WHITE);
        button.setBackground(roundRect(CINNABAR, 12, Color.TRANSPARENT));
        return button;
    }

    private Button outlineButton(String label) {
        Button button = textButton(label, false);
        button.setTextColor(JADE_DARK);
        button.setBackground(roundRect(Color.WHITE, 12, JADE));
        return button;
    }

    private Button textButton(String label, boolean bold) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(14);
        button.setTextColor(INK);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackgroundColor(Color.TRANSPARENT);
        if (bold) button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return button;
    }

    private GradientDrawable roundRect(int fill, int radiusDp, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(dp(radiusDp));
        if (stroke != Color.TRANSPARENT) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private View emptyState(String message) {
        TextView view = text(message, 14, MUTED, false);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(24), dp(36), dp(24), dp(36));
        return view;
    }

    private ScrollView scroll(View child) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(child, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private LinearLayout.LayoutParams matchWrap(int bottomMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(bottomMarginDp);
        return params;
    }

    private void addSpacer(LinearLayout layout, int widthDp, int heightDp) {
        View spacer = new View(this);
        layout.addView(spacer, new LinearLayout.LayoutParams(dp(widthDp), dp(heightDp)));
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_SHORT).show(); }

    private void handleBack() {
        if (backAction != null) backAction.run();
        else if (!titleView.getText().toString().equals("婷馔清欢")) showHome();
        else moveTaskToBack(true);
    }

    @SuppressLint("GestureBackNavigation")
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        handleBack();
    }
}
