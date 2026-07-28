package com.dawns.tingstable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.dawns.tingstable.data.PantryRepository;
import com.dawns.tingstable.data.RecipeRepository;
import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.PantryItem;
import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.SpecialCollection;
import com.dawns.tingstable.util.RecipeCategories;
import com.dawns.tingstable.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressLint("GestureBackNavigation")
public class MainActivity extends Activity {
    private static final int PAPER = Color.rgb(246, 239, 228);
    private static final int INK = Color.rgb(36, 51, 47);
    private static final int JADE = Color.rgb(40, 83, 74);
    private static final int JADE_DARK = Color.rgb(23, 58, 53);
    private static final int JADE_LIGHT = Color.rgb(228, 238, 231);
    private static final int CINNABAR = Color.rgb(182, 93, 76);
    private static final int CINNABAR_LIGHT = Color.rgb(246, 230, 225);
    private static final int GOLD = Color.rgb(168, 132, 66);
    private static final int GOLD_LIGHT = Color.rgb(244, 236, 214);
    private static final int MUTED = Color.rgb(105, 113, 106);
    private static final int LINE = Color.rgb(220, 210, 194);
    private static final int WHITE = Color.rgb(255, 253, 248);

    private static final String SCOPE_ALL = "ALL";
    private static final String SCOPE_FAVORITES = "FAVORITES";
    private static final String SCOPE_CUSTOM = "CUSTOM";

    private RecipeRepository repository;
    private PantryRepository pantryRepository;
    private FrameLayout root;
    private LinearLayout topBar;
    private LinearLayout bottomNav;
    private FrameLayout content;
    private TextView titleView;
    private Button backButton;
    private final Button[] navButtons = new Button[5];
    private Runnable backAction;

    private String currentPage = "HOME";
    private String currentRecipeId = "";
    private String recipeQuery = "";
    private String recipeScope = SCOPE_ALL;
    private String recipeCategory = RecipeCategories.ALL;
    private String pantryFilter = "ALL";
    private String detailReturnPage = "RECIPES";

    private int systemTopInset;
    private int systemBottomInset;
    private int systemLeftInset;
    private int systemRightInset;
    private int imeBottomInset;

    private EditText formName;
    private EditText formFlavor;
    private EditText formDifficulty;
    private EditText formMinutes;
    private EditText formServings;
    private EditText formMainIngredients;
    private EditText formStaples;
    private EditText formSteps;
    private EditText formTips;
    private String formCategory = RecipeCategories.STIR_FRY;
    private Recipe formExisting;
    private String formInitialSignature = "";
    private boolean formSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new RecipeRepository(this);
        pantryRepository = new PantryRepository(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        configureSystemBars();
        buildShell();
        showHome();
        if (savedInstanceState != null) restorePage(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, this::handleBack);
        }
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(true);
        }
    }

    private void buildShell() {
        root = new FrameLayout(this);
        root.setBackgroundColor(PAPER);
        root.setFitsSystemWindows(false);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(PAPER);

        topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(JADE_DARK);
        topBar.setMinimumHeight(dp(62));

        backButton = textButton("‹", true);
        backButton.setTextSize(27);
        backButton.setContentDescription("返回");
        backButton.setVisibility(View.GONE);
        topBar.addView(backButton, new LinearLayout.LayoutParams(dp(48), dp(48)));

        titleView = text("懒羊羊当大厨～", 20, WHITE, true);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setMaxLines(1);
        topBar.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        shell.addView(topBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content = new FrameLayout(this);
        content.setBackgroundColor(PAPER);
        shell.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        bottomNav = new LinearLayout(this);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setBackgroundColor(WHITE);
        bottomNav.setElevation(dp(8));
        addNav(0, "首页", R.drawable.ic_nav_home, this::showHome);
        addNav(1, "菜谱", R.drawable.ic_nav_recipes, () -> showRecipes(SCOPE_ALL));
        addNav(2, "菜篮", R.drawable.ic_nav_ingredients, this::showPantry);
        addNav(3, "特典", R.drawable.ic_nav_special, this::showSpecials);
        addNav(4, "清单", R.drawable.ic_nav_shopping, this::showShoppingList);
        shell.addView(bottomNav, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(shell, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets system = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            systemTopInset = system.top;
            systemBottomInset = system.bottom;
            systemLeftInset = system.left;
            systemRightInset = system.right;
            imeBottomInset = ime.bottom;
            applySafeInsets();
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void addNav(int index, String label, int iconRes, Runnable action) {
        Button button = textButton(label, false);
        button.setTextSize(11);
        button.setMinHeight(dp(58));
        button.setCompoundDrawablesWithIntrinsicBounds(0, iconRes, 0, 0);
        button.setCompoundDrawablePadding(dp(1));
        button.setContentDescription(label);
        button.setOnClickListener(view -> action.run());
        navButtons[index] = button;
        bottomNav.addView(button, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    }

    private void applySafeInsets() {
        if (topBar == null) return;
        topBar.setPadding(dp(8) + systemLeftInset, dp(6) + systemTopInset, dp(14) + systemRightInset, dp(6));
        bottomNav.setPadding(dp(3) + systemLeftInset, dp(3), dp(3) + systemRightInset, dp(3) + systemBottomInset);
        int bottom = bottomNav.getVisibility() == View.VISIBLE ? 0 : Math.max(systemBottomInset, imeBottomInset);
        content.setPadding(systemLeftInset, 0, systemRightInset, bottom);
    }

    private void setPage(String page, String title, Runnable onBack, View view, boolean showNavigation) {
        currentPage = page;
        titleView.setText(title);
        backAction = onBack;
        backButton.setVisibility(onBack == null ? View.GONE : View.VISIBLE);
        backButton.setOnClickListener(v -> { if (backAction != null) backAction.run(); });
        bottomNav.setVisibility(showNavigation ? View.VISIBLE : View.GONE);
        setNavSelection(showNavigation ? navIndex(page) : -1);
        content.removeAllViews();
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        int width = screenWidthDp >= 600 ? dp(Math.min(820, Math.max(360, screenWidthDp - 40))) : ViewGroup.LayoutParams.MATCH_PARENT;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL);
        content.addView(view, params);
        view.setAlpha(0f);
        view.setTranslationY(dp(5));
        view.animate().alpha(1f).translationY(0f).setDuration(160L).start();
        applySafeInsets();
    }

    private void setNavSelection(int selected) {
        for (int i = 0; i < navButtons.length; i++) {
            Button button = navButtons[i];
            if (button == null) continue;
            boolean active = i == selected;
            button.setTextColor(active ? JADE_DARK : MUTED);
            button.setCompoundDrawableTintList(ColorStateList.valueOf(active ? JADE_DARK : MUTED));
            button.setBackground(ripple(active ? JADE_LIGHT : Color.TRANSPARENT, 13, Color.TRANSPARENT));
        }
    }

    private int navIndex(String page) {
        if ("HOME".equals(page)) return 0;
        if ("RECIPES".equals(page)) return 1;
        if ("PANTRY".equals(page)) return 2;
        if ("SPECIALS".equals(page)) return 3;
        if ("SHOPPING".equals(page)) return 4;
        return -1;
    }

    private void showHome() {
        LinearLayout body = pageBody();
        LinearLayout hero = vertical();
        hero.setPadding(dp(22), dp(24), dp(22), dp(22));
        hero.setBackground(roundRect(JADE, 24, Color.TRANSPARENT));
        TextView eyebrow = text("今日厨房", 12, Color.rgb(227, 208, 163), true);
        eyebrow.setLetterSpacing(0.12f);
        hero.addView(eyebrow);
        TextView brand = text("懒羊羊当大厨～", 29, WHITE, true);
        brand.setPadding(0, dp(10), 0, dp(5));
        hero.addView(brand);
        hero.addView(text("漂亮勒女明星～", 17, Color.rgb(246, 237, 214), false));
        body.addView(hero, spaced(16));

        List<PantryItem> pantry = pantryRepository.getItems();
        int available = 0;
        int low = 0;
        for (PantryItem item : pantry) {
            if (item.available()) available++;
            if (PantryItem.STATUS_LOW.equals(item.status)) low++;
        }
        List<RecipeMatcher.Match> matches = RecipeMatcher.match(repository.getAllRecipes(), pantryRepository.getAvailableIngredientNames());
        int canCook = 0;
        for (RecipeMatcher.Match match : matches) if (match.canCook()) canCook++;

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.addView(statCard("菜篮", available + " 种", available > 0 ? "尚有食材" : "等你添菜"), weighted());
        addGap(stats, 10);
        stats.addView(statCard("可做", canCook + " 道", "按现有食材"), weighted());
        addGap(stats, 10);
        stats.addView(statCard("提醒", low + " 种", "余量不多"), weighted());
        body.addView(stats, spaced(22));

        body.addView(sectionTitle("今晚做什么"));
        body.addView(homeAction("翻菜谱", "按烹饪方式寻找一餐", "菜谱", () -> showRecipes(SCOPE_ALL)), spaced(10));
        body.addView(homeAction("打开菜篮", "看看余量与本周新购", "菜篮", this::showPantry), spaced(10));
        body.addView(homeAction("就用现有食材", "匹配现在可以做的菜", "开做", this::showPantryMatches), spaced(10));
        body.addView(homeAction("特典菜谱", "两席私藏，静候入席", "特典", this::showSpecials), spaced(16));

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        Button favorites = outlineButton("收藏 · " + repository.getFavorites().size());
        favorites.setOnClickListener(v -> showRecipes(SCOPE_FAVORITES));
        quick.addView(favorites, weighted());
        addGap(quick, 10);
        Button shopping = outlineButton("清单 · " + repository.getShoppingItems().size());
        shopping.setOnClickListener(v -> showShoppingList());
        quick.addView(shopping, weighted());
        body.addView(quick, spaced(12));

        setPage("HOME", "懒羊羊当大厨～", null, scroll(body), true);
    }

    private View statCard(String title, String value, String note) {
        LinearLayout box = vertical();
        box.setPadding(dp(13), dp(14), dp(12), dp(13));
        box.setBackground(roundRect(WHITE, 18, Color.TRANSPARENT));
        box.addView(text(title, 12, MUTED, false));
        TextView number = text(value, 21, JADE_DARK, true);
        number.setPadding(0, dp(4), 0, dp(3));
        box.addView(number);
        box.addView(text(note, 10, MUTED, false));
        return box;
    }

    private View homeAction(String title, String description, String marker, Runnable action) {
        LinearLayout box = new LinearLayout(this);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(16), dp(15), dp(14), dp(15));
        box.setBackground(ripple(WHITE, 18, Color.TRANSPARENT));
        box.setOnClickListener(v -> action.run());
        TextView mark = text(marker, 12, WHITE, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(roundRect(JADE, 13, Color.TRANSPARENT));
        box.addView(mark, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout words = vertical();
        words.setPadding(dp(14), 0, dp(8), 0);
        words.addView(text(title, 17, INK, true));
        TextView desc = text(description, 13, MUTED, false);
        desc.setPadding(0, dp(3), 0, 0);
        words.addView(desc);
        box.addView(words, weighted());
        box.addView(text("›", 25, GOLD, false));
        return box;
    }

    private void showRecipes(String scope) {
        recipeScope = scope == null ? SCOPE_ALL : scope;
        LinearLayout body = pageBody();

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titleBox = vertical();
        titleBox.addView(text("家常菜谱", 25, INK, true));
        titleBox.addView(text("按分类慢慢挑，也可从食材直达。", 13, MUTED, false));
        heading.addView(titleBox, weighted());
        Button add = primaryButton("＋ 自定义");
        add.setOnClickListener(v -> showRecipeForm(null));
        heading.addView(add);
        body.addView(heading, spaced(14));

        LinearLayout scopeRow = horizontalChipRow();
        scopeRow.addView(filterChip("全部菜谱", SCOPE_ALL.equals(recipeScope), () -> showRecipes(SCOPE_ALL)));
        scopeRow.addView(filterChip("我的收藏", SCOPE_FAVORITES.equals(recipeScope), () -> showRecipes(SCOPE_FAVORITES)));
        scopeRow.addView(filterChip("自定义", SCOPE_CUSTOM.equals(recipeScope), () -> showRecipes(SCOPE_CUSTOM)));
        body.addView(horizontalScroll(scopeRow), spaced(10));

        LinearLayout categoryRow = horizontalChipRow();
        for (String category : RecipeCategories.all()) {
            categoryRow.addView(filterChip(category, category.equals(recipeCategory), () -> {
                recipeCategory = category;
                showRecipes(recipeScope);
            }));
        }
        body.addView(horizontalScroll(categoryRow), spaced(12));

        EditText search = input("搜索菜名、食材或口味");
        search.setSingleLine(true);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setText(recipeQuery);
        search.setSelection(search.length());
        body.addView(search, spaced(12));

        TextView count = text("", 12, MUTED, false);
        body.addView(count, spaced(8));
        LinearLayout rows = vertical();
        body.addView(rows);
        renderRecipeRows(rows, count);

        search.addTextChangedListener(new SimpleWatcher() {
            @Override public void afterTextChanged(Editable editable) {
                recipeQuery = editable.toString();
                renderRecipeRows(rows, count);
            }
        });
        setPage("RECIPES", "菜谱", null, scroll(body), true);
    }

    private void renderRecipeRows(LinearLayout rows, TextView count) {
        rows.removeAllViews();
        List<Recipe> filtered = new ArrayList<>();
        Set<String> favorites = repository.getFavorites();
        for (Recipe recipe : repository.getAllRecipes()) {
            if (SCOPE_FAVORITES.equals(recipeScope) && !favorites.contains(recipe.id)) continue;
            if (SCOPE_CUSTOM.equals(recipeScope) && !recipe.custom) continue;
            if (!RecipeCategories.ALL.equals(recipeCategory)
                    && !recipeCategory.equals(RecipeCategories.categoryFor(recipe))) continue;
            if (!recipeMatchesQuery(recipe, recipeQuery)) continue;
            filtered.add(recipe);
        }
        count.setText(getString(R.string.recipe_count, filtered.size(), recipeCategory));
        if (filtered.isEmpty()) {
            rows.addView(emptyState("没有找到合适的菜谱", "换个分类或搜索词试试。"), spaced(12));
            return;
        }
        for (Recipe recipe : filtered) rows.addView(recipeCard(recipe), spaced(10));
    }

    private boolean recipeMatchesQuery(Recipe recipe, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return true;
        if (safe(recipe.name).toLowerCase(Locale.ROOT).contains(q)
                || safe(recipe.flavor).toLowerCase(Locale.ROOT).contains(q)
                || RecipeCategories.categoryFor(recipe).contains(q)) return true;
        for (Ingredient ingredient : recipe.ingredients) {
            if (RecipeMatcher.queryMatchesIngredient(q, ingredient.name)) return true;
        }
        return false;
    }

    private View recipeCard(Recipe recipe) {
        int tone = categoryTone(RecipeCategories.categoryFor(recipe));
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.HORIZONTAL);
        outer.setBackground(ripple(WHITE, 18, Color.TRANSPARENT));
        outer.setClipToOutline(true);
        outer.setOnClickListener(v -> openRecipeDetail(recipe, "RECIPES"));

        View accent = new View(this);
        accent.setBackgroundColor(tone);
        outer.addView(accent, new LinearLayout.LayoutParams(dp(5), ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout box = vertical();
        box.setPadding(dp(15), dp(14), dp(13), dp(14));
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(text(recipe.name, 18, INK, true), weighted());
        Button favorite = textButton(repository.isFavorite(recipe.id) ? "★" : "☆", false);
        favorite.setTextSize(22);
        favorite.setTextColor(repository.isFavorite(recipe.id) ? GOLD : MUTED);
        favorite.setContentDescription(repository.isFavorite(recipe.id) ? "取消收藏" : "收藏");
        favorite.setOnClickListener(v -> {
            repository.toggleFavorite(recipe.id);
            showRecipes(recipeScope);
        });
        top.addView(favorite, new LinearLayout.LayoutParams(dp(46), dp(42)));
        box.addView(top);

        String meta = RecipeCategories.categoryFor(recipe) + "  ·  " + recipe.minutes + " 分钟  ·  " + recipe.difficulty;
        box.addView(text(meta, 12, tone, true));
        TextView ingredients = text(ingredientSummary(recipe), 13, MUTED, false);
        ingredients.setPadding(0, dp(8), 0, 0);
        ingredients.setMaxLines(2);
        box.addView(ingredients);
        outer.addView(box, weighted());
        return outer;
    }

    private String ingredientSummary(Recipe recipe) {
        List<String> names = new ArrayList<>();
        for (Ingredient ingredient : recipe.ingredients) {
            if (!ingredient.staple) names.add(ingredient.name);
            if (names.size() == 4) break;
        }
        return names.isEmpty() ? "食材待补充" : String.join("、", names);
    }

    private void openRecipeDetail(Recipe recipe, String returnPage) {
        if (recipe == null) return;
        currentRecipeId = recipe.id;
        detailReturnPage = returnPage;
        showRecipeDetail(recipe);
    }

    private void showRecipeDetail(Recipe recipe) {
        LinearLayout body = pageBody();
        int tone = categoryTone(RecipeCategories.categoryFor(recipe));

        LinearLayout hero = vertical();
        hero.setPadding(dp(20), dp(20), dp(20), dp(18));
        hero.setBackground(roundRect(tone, 22, Color.TRANSPARENT));
        hero.addView(text(RecipeCategories.categoryFor(recipe), 12, Color.argb(220, 255, 255, 255), true));
        TextView name = text(recipe.name, 28, WHITE, true);
        name.setPadding(0, dp(8), 0, dp(8));
        hero.addView(name);
        hero.addView(text(recipe.flavor + "  ·  " + recipe.minutes + " 分钟  ·  " + recipe.servings + " 人份", 14, WHITE, false));
        body.addView(hero, spaced(15));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button favorite = outlineButton(repository.isFavorite(recipe.id) ? "★ 已收藏" : "☆ 收藏");
        favorite.setOnClickListener(v -> {
            repository.toggleFavorite(recipe.id);
            showRecipeDetail(repository.findById(recipe.id));
        });
        actions.addView(favorite, weighted());
        if (recipe.custom) {
            addGap(actions, 10);
            Button edit = outlineButton("编辑");
            edit.setOnClickListener(v -> showRecipeForm(recipe));
            actions.addView(edit, weighted());
        }
        body.addView(actions, spaced(16));

        body.addView(sectionTitle("食材"));
        for (Ingredient ingredient : recipe.ingredients) body.addView(ingredientRow(ingredient), spaced(7));

        body.addView(sectionTitle("做法"), spaced(14));
        for (int i = 0; i < recipe.steps.size(); i++) body.addView(stepRow(i + 1, recipe.steps.get(i)), spaced(8));

        if (!safe(recipe.tips).trim().isEmpty()) {
            body.addView(sectionTitle("小提示"), spaced(14));
            TextView tips = text(recipe.tips, 14, INK, false);
            tips.setPadding(dp(15), dp(14), dp(15), dp(14));
            tips.setBackground(roundRect(GOLD_LIGHT, 16, Color.TRANSPARENT));
            body.addView(tips, spaced(10));
        }

        if (recipe.custom) {
            Button delete = textButton("删除这道自定义菜谱", false);
            delete.setTextColor(CINNABAR);
            delete.setOnClickListener(v -> confirmDelete(recipe));
            body.addView(delete, spaced(18));
        }

        setPage("DETAIL", recipe.name, this::returnFromRecipeDetail, scroll(body), false);
    }

    private View ingredientRow(Ingredient ingredient) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(11), dp(8), dp(11));
        row.setBackground(ripple(WHITE, 15, Color.TRANSPARENT));
        row.setOnClickListener(v -> showRecipesForIngredient(ingredient.name));
        LinearLayout words = vertical();
        words.addView(text(ingredient.name, 15, INK, true));
        words.addView(text(ingredient.staple ? "常备调料" : "点击查看相关菜谱", 11, MUTED, false));
        row.addView(words, weighted());
        row.addView(text(ingredient.amount, 13, JADE, true));
        Button add = textButton("＋清单", false);
        add.setTextColor(CINNABAR);
        add.setOnClickListener(v -> {
            repository.addShoppingItems(Collections.singletonList(ingredient.name));
            toast("已加入采购清单");
        });
        row.addView(add);
        return row;
    }

    private View stepRow(int index, String step) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.TOP);
        TextView number = text(String.valueOf(index), 13, WHITE, true);
        number.setGravity(Gravity.CENTER);
        number.setBackground(roundRect(JADE, 14, Color.TRANSPARENT));
        row.addView(number, new LinearLayout.LayoutParams(dp(28), dp(28)));
        TextView words = text(step, 15, INK, false);
        words.setPadding(dp(12), dp(3), 0, dp(4));
        row.addView(words, weighted());
        return row;
    }

    private void returnFromRecipeDetail() {
        if ("PANTRY_MATCHES".equals(detailReturnPage)) showPantryMatches();
        else if ("SPECIALS".equals(detailReturnPage)) showSpecials();
        else showRecipes(recipeScope);
    }

    private void confirmDelete(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("删除菜谱")
                .setMessage("确定删除“" + recipe.name + "”吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    repository.deleteCustomRecipe(recipe.id);
                    toast("已删除");
                    showRecipes(SCOPE_CUSTOM);
                }).show();
    }

    private void showRecipesForIngredient(String ingredient) {
        recipeQuery = ingredient;
        recipeScope = SCOPE_ALL;
        recipeCategory = RecipeCategories.ALL;
        showRecipes(SCOPE_ALL);
    }

    private void showPantry() {
        LinearLayout body = pageBody();
        List<PantryItem> items = pantryRepository.getItems();
        int available = 0;
        int low = 0;
        int week = 0;
        for (PantryItem item : items) {
            if (item.available()) available++;
            if (PantryItem.STATUS_LOW.equals(item.status)) low++;
            if (isThisWeek(item.purchasedAt)) week++;
        }

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titleBox = vertical();
        titleBox.addView(text("我的菜篮", 25, INK, true));
        titleBox.addView(text("按冰箱分层整理这一周的食材。", 13, MUTED, false));
        heading.addView(titleBox, weighted());
        Button add = primaryButton("＋ 添食材");
        add.setOnClickListener(v -> showPantryItemDialog(null));
        heading.addView(add);
        body.addView(heading, spaced(14));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.addView(statCard("可用", available + " 种", "充足或不多"), weighted());
        addGap(stats, 8);
        stats.addView(statCard("本周", week + " 种", "新购入"), weighted());
        addGap(stats, 8);
        stats.addView(statCard("余量", low + " 种", "需要留意"), weighted());
        body.addView(stats, spaced(14));

        Button match = primaryButton("看看现在能做什么");
        match.setOnClickListener(v -> showPantryMatches());
        body.addView(match, spaced(12));

        LinearLayout filters = horizontalChipRow();
        filters.addView(filterChip("全部", "ALL".equals(pantryFilter), () -> { pantryFilter = "ALL"; showPantry(); }));
        filters.addView(filterChip("本周新购", "WEEK".equals(pantryFilter), () -> { pantryFilter = "WEEK"; showPantry(); }));
        filters.addView(filterChip("余量不多", "LOW".equals(pantryFilter), () -> { pantryFilter = "LOW"; showPantry(); }));
        filters.addView(filterChip("已经用完", "EMPTY".equals(pantryFilter), () -> { pantryFilter = "EMPTY"; showPantry(); }));
        body.addView(horizontalScroll(filters), spaced(14));

        Map<String, List<PantryItem>> groups = new LinkedHashMap<>();
        for (String category : pantryCategories()) groups.put(category, new ArrayList<>());
        for (PantryItem item : items) {
            if (!pantryVisible(item)) continue;
            groups.computeIfAbsent(item.category, key -> new ArrayList<>()).add(item);
        }

        boolean any = false;
        for (Map.Entry<String, List<PantryItem>> entry : groups.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            any = true;
            entry.getValue().sort(Comparator.comparing(item -> item.name));
            body.addView(pantryShelf(entry.getKey(), entry.getValue()), spaced(12));
        }
        if (!any) body.addView(emptyState("菜篮还是空的", "添入本周买到的食材，或切换筛选。"), spaced(12));
        setPage("PANTRY", "菜篮", null, scroll(body), true);
    }

    private View pantryShelf(String category, List<PantryItem> items) {
        LinearLayout shelf = vertical();
        shelf.setPadding(dp(15), dp(15), dp(15), dp(9));
        shelf.setBackground(roundRect(WHITE, 19, Color.TRANSPARENT));
        LinearLayout title = new LinearLayout(this);
        title.setGravity(Gravity.CENTER_VERTICAL);
        TextView symbol = text(pantrySymbol(category), 17, categoryTone(category), true);
        symbol.setGravity(Gravity.CENTER);
        symbol.setBackground(roundRect(softTone(categoryTone(category)), 12, Color.TRANSPARENT));
        title.addView(symbol, new LinearLayout.LayoutParams(dp(38), dp(38)));
        TextView name = text(category, 17, INK, true);
        name.setPadding(dp(10), 0, 0, 0);
        title.addView(name, weighted());
        title.addView(text(items.size() + " 种", 12, MUTED, false));
        shelf.addView(title, spaced(8));
        View line = new View(this);
        line.setBackgroundColor(LINE);
        shelf.addView(line, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        for (PantryItem item : items) shelf.addView(pantryItemRow(item));
        return shelf;
    }

    private View pantryItemRow(PantryItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(11), 0, dp(8));
        row.setOnClickListener(v -> showRecipesForIngredient(item.name));
        LinearLayout words = vertical();
        words.addView(text(item.name, 15, INK, true));
        String note = item.amountLabel();
        if (isThisWeek(item.purchasedAt)) note += " · 本周购入";
        if (!safe(item.note).isEmpty()) note += " · " + item.note;
        words.addView(text(note, 11, MUTED, false));
        row.addView(words, weighted());
        TextView status = text(item.status, 11, statusTone(item.status), true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(dp(9), dp(5), dp(9), dp(5));
        status.setBackground(roundRect(statusSoft(item.status), 12, Color.TRANSPARENT));
        row.addView(status);
        Button edit = textButton("调整", false);
        edit.setTextColor(JADE);
        edit.setOnClickListener(v -> showPantryItemDialog(item));
        row.addView(edit);
        return row;
    }

    private boolean pantryVisible(PantryItem item) {
        if ("WEEK".equals(pantryFilter)) return isThisWeek(item.purchasedAt);
        if ("LOW".equals(pantryFilter)) return PantryItem.STATUS_LOW.equals(item.status);
        if ("EMPTY".equals(pantryFilter)) return PantryItem.STATUS_EMPTY.equals(item.status);
        return true;
    }

    private void showPantryItemDialog(PantryItem existing) {
        boolean editing = existing != null;
        PantryItem source = editing ? existing.copy() : new PantryItem("", "", "时蔬", "", "",
                PantryItem.STATUS_FULL, System.currentTimeMillis(), "");
        LinearLayout form = dialogBody();
        EditText name = input("食材名称");
        name.setText(source.name);
        form.addView(labeled("食材", name));

        LinearLayout amountRow = new LinearLayout(this);
        amountRow.setOrientation(LinearLayout.HORIZONTAL);
        EditText quantity = input("数量");
        quantity.setText(source.quantity);
        amountRow.addView(quantity, weighted());
        addGap(amountRow, 8);
        EditText unit = input("单位，如 个 / g");
        unit.setText(source.unit);
        amountRow.addView(unit, weighted());
        form.addView(labeled("数量与单位", amountRow));

        String[] categoryValue = {source.category};
        form.addView(text("分类", 12, MUTED, true));
        form.addView(choiceRow(pantryCategories().toArray(new String[0]), categoryValue), spaced(10));

        String[] statusValue = {source.status};
        form.addView(text("余量状态", 12, MUTED, true));
        form.addView(choiceRow(new String[]{PantryItem.STATUS_FULL, PantryItem.STATUS_LOW, PantryItem.STATUS_EMPTY}, statusValue), spaced(10));

        EditText note = input("备注，可留空");
        note.setText(source.note);
        form.addView(labeled("备注", note));
        CheckBox purchased = new CheckBox(this);
        purchased.setText("记为本周购入");
        purchased.setTextColor(INK);
        purchased.setChecked(!editing || isThisWeek(source.purchasedAt));
        form.addView(purchased, spaced(4));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editing ? "调整食材" : "添入菜篮")
                .setView(scroll(form))
                .setNegativeButton("取消", null)
                .setNeutralButton(editing ? "删除" : null, null)
                .setPositiveButton("保存", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String ingredientName = name.getText().toString().trim();
                if (ingredientName.isEmpty()) {
                    name.setError("请填写食材名称");
                    return;
                }
                PantryItem item = source.copy();
                if (!editing) {
                    PantryItem duplicate = pantryRepository.findByName(ingredientName);
                    if (duplicate != null) item.id = duplicate.id;
                }
                item.name = ingredientName;
                item.quantity = quantity.getText().toString().trim();
                item.unit = unit.getText().toString().trim();
                item.category = categoryValue[0];
                item.status = statusValue[0];
                item.note = note.getText().toString().trim();
                if (purchased.isChecked()) {
                    if (!isThisWeek(item.purchasedAt)) item.purchasedAt = System.currentTimeMillis();
                } else item.purchasedAt = 0L;
                pantryRepository.saveItem(item);
                dialog.dismiss();
                showPantry();
            });
            if (editing) dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("移出菜篮")
                        .setMessage("确定移除“" + source.name + "”吗？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("移除", (d, w) -> {
                            pantryRepository.deleteItem(source.id);
                            dialog.dismiss();
                            showPantry();
                        }).show();
            });
        });
        dialog.show();
    }

    private View choiceRow(String[] choices, String[] selected) {
        LinearLayout row = horizontalChipRow();
        List<Button> buttons = new ArrayList<>();
        for (String choice : choices) {
            Button button = filterChip(choice, choice.equals(selected[0]), () -> {});
            buttons.add(button);
            button.setOnClickListener(v -> {
                selected[0] = choice;
                for (Button item : buttons) styleFilterChip(item, item.getText().toString().equals(selected[0]));
            });
            row.addView(button);
        }
        return horizontalScroll(row);
    }

    private void showPantryMatches() {
        Set<String> available = pantryRepository.getAvailableIngredientNames();
        List<RecipeMatcher.Match> matches = RecipeMatcher.match(repository.getAllRecipes(), available);
        LinearLayout body = pageBody();
        body.addView(text("现有食材能做什么", 25, INK, true));
        body.addView(text("“充足”和“不多”会参与匹配，“用完”不会参与。", 13, MUTED, false), spaced(8));

        if (available.isEmpty()) {
            body.addView(emptyState("菜篮里还没有可用食材", "先添入食材，再来看看能做什么。"), spaced(18));
        } else if (matches.isEmpty()) {
            body.addView(emptyState("暂时没有合适的组合", "继续添几样主料，匹配结果会更丰富。"), spaced(18));
        } else {
            int canCook = 0;
            for (RecipeMatcher.Match match : matches) if (match.canCook()) canCook++;
            body.addView(text("可直接做 " + canCook + " 道 · 接近可做 " + (matches.size() - canCook) + " 道", 12, MUTED, false), spaced(12));
            for (RecipeMatcher.Match match : matches) body.addView(matchCard(match), spaced(10));
        }
        setPage("PANTRY_MATCHES", "食材匹配", this::showPantry, scroll(body), false);
    }

    private View matchCard(RecipeMatcher.Match match) {
        LinearLayout box = vertical();
        box.setPadding(dp(15), dp(14), dp(15), dp(13));
        box.setBackground(ripple(WHITE, 18, Color.TRANSPARENT));
        box.setOnClickListener(v -> openRecipeDetail(match.recipe, "PANTRY_MATCHES"));
        LinearLayout title = new LinearLayout(this);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.addView(text(match.recipe.name, 18, INK, true), weighted());
        TextView state = text(match.canCook() ? "可以开做" : match.percent + "%", 11,
                match.canCook() ? JADE : CINNABAR, true);
        state.setPadding(dp(9), dp(5), dp(9), dp(5));
        state.setBackground(roundRect(match.canCook() ? JADE_LIGHT : CINNABAR_LIGHT, 12, Color.TRANSPARENT));
        title.addView(state);
        box.addView(title);
        box.addView(text(RecipeCategories.categoryFor(match.recipe) + " · " + match.recipe.minutes + " 分钟", 12, MUTED, false));
        if (!match.missing.isEmpty()) {
            LinearLayout missing = new LinearLayout(this);
            missing.setGravity(Gravity.CENTER_VERTICAL);
            missing.setPadding(0, dp(9), 0, 0);
            missing.addView(text("还缺：" + String.join("、", match.missing), 12, CINNABAR, false), weighted());
            Button add = textButton("加入清单", false);
            add.setTextColor(CINNABAR);
            add.setOnClickListener(v -> {
                repository.addShoppingItems(match.missing);
                toast("缺少食材已加入清单");
            });
            missing.addView(add);
            box.addView(missing);
        }
        return box;
    }

    private void showSpecials() {
        LinearLayout body = pageBody();
        body.addView(text("特典菜谱", 25, INK, true));
        body.addView(text("两席私藏，留待慢慢成篇。", 13, MUTED, false), spaced(14));
        for (SpecialCollection collection : specialCollections()) {
            LinearLayout card = vertical();
            card.setPadding(dp(20), dp(20), dp(20), dp(18));
            int tone = "ting".equals(collection.id) ? JADE : CINNABAR;
            card.setBackground(ripple(WHITE, 21, Color.TRANSPARENT));
            TextView mark = text(collection.subtitle, 11, tone, true);
            mark.setLetterSpacing(0.1f);
            card.addView(mark);
            TextView title = text(collection.title, 23, INK, true);
            title.setPadding(0, dp(10), 0, dp(7));
            card.addView(title);
            card.addView(text(collection.quote, 14, MUTED, false));
            TextView enter = text("入席  ›", 13, tone, true);
            enter.setPadding(0, dp(14), 0, 0);
            card.addView(enter);
            card.setOnClickListener(v -> showSpecialDetail(collection));
            body.addView(card, spaced(12));
        }
        setPage("SPECIALS", "特典", null, scroll(body), true);
    }

    private void showSpecialDetail(SpecialCollection collection) {
        LinearLayout body = pageBody();
        LinearLayout hero = vertical();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(22), dp(30), dp(22), dp(28));
        hero.setBackground(roundRect("ting".equals(collection.id) ? JADE : CINNABAR, 23, Color.TRANSPARENT));
        hero.addView(text(collection.subtitle, 12, Color.rgb(242, 224, 184), true));
        TextView title = text(collection.title, 27, WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(12), 0, dp(9));
        hero.addView(title);
        hero.addView(text(collection.quote, 15, WHITE, false));
        body.addView(hero, spaced(18));
        body.addView(emptyState("尚待入席", ""), spaced(22));
        setPage("SPECIAL_DETAIL", collection.title, this::showSpecials, scroll(body), false);
    }

    private List<SpecialCollection> specialCollections() {
        return Arrays.asList(
                new SpecialCollection("ting", "婷馔清欢", "四时特典", "人间有味，四时清欢。", Collections.emptyList()),
                new SpecialCollection("feng", "楚天云岫 · 峰岳特典", "荆楚特典", "楚水有味，云峰藏香。", Collections.emptyList())
        );
    }

    private void showShoppingList() {
        LinearLayout body = pageBody();
        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout words = vertical();
        words.addView(text("采购清单", 25, INK, true));
        words.addView(text("买到后可以直接放入菜篮。", 13, MUTED, false));
        heading.addView(words, weighted());
        Button add = primaryButton("＋ 添加");
        add.setOnClickListener(v -> showAddShoppingDialog());
        heading.addView(add);
        body.addView(heading, spaced(16));

        List<String> items = repository.getShoppingItems();
        if (items.isEmpty()) {
            body.addView(emptyState("清单空空的", "从菜谱添加缺少食材，或手动记下一样。"), spaced(16));
        } else {
            for (String item : items) {
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(12), dp(9), dp(7), dp(9));
                row.setBackground(roundRect(WHITE, 15, Color.TRANSPARENT));
                CheckBox check = new CheckBox(this);
                check.setText(item);
                check.setTextSize(15);
                check.setTextColor(INK);
                check.setPadding(0, 0, dp(8), 0);
                row.addView(check, weighted());
                Button remove = textButton("移除", false);
                remove.setTextColor(CINNABAR);
                remove.setOnClickListener(v -> {
                    repository.removeShoppingItem(item);
                    showShoppingList();
                });
                row.addView(remove);
                check.setOnCheckedChangeListener((button, checked) -> {
                    if (checked) showShoppingAction(item, check);
                });
                body.addView(row, spaced(8));
            }
            Button clear = textButton("清空采购清单", false);
            clear.setTextColor(CINNABAR);
            clear.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("清空清单")
                    .setMessage("确定移除全部采购项目吗？")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("清空", (d, w) -> { repository.clearShoppingList(); showShoppingList(); })
                    .show());
            body.addView(clear, spaced(14));
        }
        setPage("SHOPPING", "清单", null, scroll(body), true);
    }

    private void showAddShoppingDialog() {
        EditText input = input("食材名称");
        LinearLayout wrap = dialogBody();
        wrap.addView(input);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("添加采购项目")
                .setView(wrap)
                .setNegativeButton("取消", null)
                .setPositiveButton("添加", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) { input.setError("请输入食材"); return; }
            repository.addShoppingItems(Collections.singletonList(value));
            dialog.dismiss();
            showShoppingList();
        }));
        dialog.show();
    }

    private void showShoppingAction(String item, CheckBox check) {
        new AlertDialog.Builder(this)
                .setTitle("“" + item + "”已经买到？")
                .setItems(new String[]{"放入菜篮", "仅从清单移除"}, (dialog, which) -> {
                    if (which == 0) {
                        check.setChecked(false);
                        showAddShoppingToPantryDialog(item);
                    } else {
                        repository.removeShoppingItem(item);
                        showShoppingList();
                    }
                })
                .setOnCancelListener(dialog -> check.setChecked(false))
                .setNegativeButton("取消", (dialog, which) -> check.setChecked(false))
                .show();
    }

    private void showAddShoppingToPantryDialog(String item) {
        LinearLayout form = dialogBody();
        EditText quantity = input("数量，可留空");
        EditText unit = input("单位，如 个 / g");
        form.addView(labeled("数量", quantity));
        form.addView(labeled("单位", unit));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("放入菜篮 · " + item)
                .setView(form)
                .setNegativeButton("取消", null)
                .setPositiveButton("放入", (d, w) -> {
                    pantryRepository.addOrRestock(item, quantity.getText().toString(), unit.getText().toString());
                    repository.removeShoppingItem(item);
                    toast("已放入菜篮");
                    showShoppingList();
                }).create();
        dialog.show();
    }

    private void showRecipeForm(Recipe existing) {
        currentRecipeId = existing == null ? "" : existing.id;
        formExisting = existing;
        formSaved = false;
        formCategory = existing == null ? RecipeCategories.STIR_FRY : RecipeCategories.categoryFor(existing);
        LinearLayout body = pageBody();
        body.addView(text(existing == null ? "新建菜谱" : "编辑菜谱", 25, INK, true));
        body.addView(text("每行一项，名称与用量用“|”分开。", 13, MUTED, false), spaced(12));

        formName = input("例如：番茄炒鸡蛋");
        formName.setText(existing == null ? "" : existing.name);
        body.addView(labeled("菜名", formName), spaced(8));

        body.addView(text("烹饪分类", 12, MUTED, true));
        String[] categoryHolder = {formCategory};
        View categoryChoices = choiceRow(RecipeCategories.editable().toArray(new String[0]), categoryHolder);
        body.addView(categoryChoices, spaced(10));

        LinearLayout meta = new LinearLayout(this);
        meta.setOrientation(LinearLayout.HORIZONTAL);
        formFlavor = input("口味");
        formFlavor.setText(existing == null ? "家常" : existing.flavor);
        meta.addView(formFlavor, weighted());
        addGap(meta, 8);
        formDifficulty = input("难度");
        formDifficulty.setText(existing == null ? "简单" : existing.difficulty);
        meta.addView(formDifficulty, weighted());
        body.addView(labeled("口味与难度", meta), spaced(8));

        LinearLayout numbers = new LinearLayout(this);
        numbers.setOrientation(LinearLayout.HORIZONTAL);
        formMinutes = input("分钟");
        formMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        formMinutes.setText(existing == null ? "20" : String.valueOf(existing.minutes));
        numbers.addView(formMinutes, weighted());
        addGap(numbers, 8);
        formServings = input("人份");
        formServings.setInputType(InputType.TYPE_CLASS_NUMBER);
        formServings.setText(existing == null ? "2" : String.valueOf(existing.servings));
        numbers.addView(formServings, weighted());
        body.addView(labeled("时间与份量", numbers), spaced(8));

        formMainIngredients = area("番茄 | 2个\n鸡蛋 | 3个", 5);
        formMainIngredients.setText(existing == null ? "" : ingredientsForEdit(existing, false));
        body.addView(labeled("主要食材", formMainIngredients), spaced(8));

        formStaples = area("盐 | 适量\n食用油 | 适量", 4);
        formStaples.setText(existing == null ? "" : ingredientsForEdit(existing, true));
        body.addView(labeled("常备调料", formStaples), spaced(8));

        formSteps = area("切配食材\n热锅下油\n翻炒并调味", 6);
        formSteps.setText(existing == null ? "" : String.join("\n", existing.steps));
        body.addView(labeled("制作步骤", formSteps), spaced(8));

        formTips = area("可选的小提示", 3);
        formTips.setText(existing == null ? "" : existing.tips);
        body.addView(labeled("小提示", formTips), spaced(10));

        Button save = primaryButton("保存菜谱");
        save.setOnClickListener(v -> {
            formCategory = categoryHolder[0];
            saveForm();
        });
        body.addView(save, spaced(18));
        formInitialSignature = formSignature(categoryHolder[0]);
        setPage("FORM", existing == null ? "新建菜谱" : "编辑菜谱", () -> leaveForm(categoryHolder[0]), scroll(body), false);
    }

    private void saveForm() {
        String name = formName.getText().toString().trim();
        if (name.isEmpty()) { formName.setError("请填写菜名"); return; }
        int minutes = positiveInt(formMinutes, "请填写有效时间");
        int servings = positiveInt(formServings, "请填写有效份量");
        if (minutes <= 0 || servings <= 0) return;
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.addAll(parseIngredients(formMainIngredients.getText().toString(), false));
        ingredients.addAll(parseIngredients(formStaples.getText().toString(), true));
        if (ingredients.isEmpty()) { formMainIngredients.setError("至少填写一种食材"); return; }
        List<String> steps = parseLines(formSteps.getText().toString());
        if (steps.isEmpty()) { formSteps.setError("至少填写一个步骤"); return; }

        Recipe recipe = new Recipe(
                formExisting == null ? "" : formExisting.id,
                name,
                formCategory,
                valueOr(formFlavor, "家常"),
                valueOr(formDifficulty, "简单"),
                minutes,
                servings,
                ingredients,
                steps,
                formTips.getText().toString().trim(),
                true
        );
        repository.saveCustomRecipe(recipe);
        formSaved = true;
        toast("菜谱已保存");
        openRecipeDetail(repository.findById(recipe.id), "RECIPES");
    }

    private void leaveForm(String category) {
        if (formSaved || formInitialSignature.equals(formSignature(category))) {
            if (formExisting != null) showRecipeDetail(repository.findById(formExisting.id));
            else showRecipes(SCOPE_ALL);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("放弃未保存内容？")
                .setMessage("返回后，本次修改不会保留。")
                .setNegativeButton("继续编辑", null)
                .setPositiveButton("放弃", (d, w) -> {
                    if (formExisting != null) showRecipeDetail(repository.findById(formExisting.id));
                    else showRecipes(SCOPE_ALL);
                }).show();
    }

    private String formSignature(String category) {
        if (formName == null) return "";
        return category + "|" + formName.getText() + "|" + formFlavor.getText() + "|" + formDifficulty.getText()
                + "|" + formMinutes.getText() + "|" + formServings.getText() + "|" + formMainIngredients.getText()
                + "|" + formStaples.getText() + "|" + formSteps.getText() + "|" + formTips.getText();
    }

    private String ingredientsForEdit(Recipe recipe, boolean staple) {
        List<String> lines = new ArrayList<>();
        for (Ingredient ingredient : recipe.ingredients) {
            if (ingredient.staple == staple) lines.add(ingredient.name + " | " + ingredient.amount);
        }
        return String.join("\n", lines);
    }

    private List<Ingredient> parseIngredients(String raw, boolean staple) {
        List<Ingredient> result = new ArrayList<>();
        for (String line : parseLines(raw)) {
            String[] parts = line.split("[|｜]", 2);
            String name = parts[0].trim();
            String amount = parts.length > 1 ? parts[1].trim() : "适量";
            if (!name.isEmpty()) result.add(new Ingredient(name, amount.isEmpty() ? "适量" : amount, staple));
        }
        return result;
    }

    private List<String> parseLines(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null) return result;
        for (String line : raw.split("\\r?\\n")) {
            String value = line.trim();
            if (!value.isEmpty()) result.add(value);
        }
        return result;
    }

    private int positiveInt(EditText field, String error) {
        try {
            int value = Integer.parseInt(field.getText().toString().trim());
            if (value > 0) return value;
        } catch (Exception ignored) { }
        field.setError(error);
        return -1;
    }

    private String valueOr(EditText field, String fallback) {
        String value = field.getText().toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private boolean isThisWeek(long time) {
        if (time <= 0) return false;
        Calendar start = Calendar.getInstance();
        int day = start.get(Calendar.DAY_OF_WEEK);
        int offset = day == Calendar.SUNDAY ? 6 : day - Calendar.MONDAY;
        start.add(Calendar.DAY_OF_MONTH, -offset);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return time >= start.getTimeInMillis();
    }

    private List<String> pantryCategories() {
        return Arrays.asList("肉禽", "水鲜", "蛋豆", "时蔬", "谷物", "水果", "调料", "其他");
    }

    private String pantrySymbol(String category) {
        if ("肉禽".equals(category)) return "肉";
        if ("水鲜".equals(category)) return "鲜";
        if ("蛋豆".equals(category)) return "豆";
        if ("时蔬".equals(category)) return "蔬";
        if ("谷物".equals(category)) return "谷";
        if ("水果".equals(category)) return "果";
        if ("调料".equals(category)) return "味";
        return "余";
    }

    private int categoryTone(String category) {
        if (RecipeCategories.SOUP.equals(category) || "水鲜".equals(category)) return Color.rgb(63, 111, 111);
        if (RecipeCategories.STIR_FRY.equals(category) || "肉禽".equals(category)) return CINNABAR;
        if (RecipeCategories.STEAM.equals(category) || "时蔬".equals(category)) return JADE;
        if (RecipeCategories.STEW.equals(category) || "蛋豆".equals(category)) return Color.rgb(140, 92, 62);
        if (RecipeCategories.COLD.equals(category) || "水果".equals(category)) return Color.rgb(99, 133, 93);
        if (RecipeCategories.AIR_FRYER.equals(category) || RecipeCategories.GRILL.equals(category)) return GOLD;
        if (RecipeCategories.STAPLE.equals(category) || "谷物".equals(category)) return Color.rgb(151, 115, 60);
        if (RecipeCategories.BAKING.equals(category)) return Color.rgb(154, 103, 119);
        if ("调料".equals(category)) return Color.rgb(120, 96, 68);
        return MUTED;
    }

    private int softTone(int tone) { return mixColor(tone, WHITE, 0.82f); }

    private int statusTone(String status) {
        if (PantryItem.STATUS_EMPTY.equals(status)) return MUTED;
        if (PantryItem.STATUS_LOW.equals(status)) return CINNABAR;
        return JADE;
    }

    private int statusSoft(String status) {
        if (PantryItem.STATUS_EMPTY.equals(status)) return Color.rgb(236, 234, 229);
        if (PantryItem.STATUS_LOW.equals(status)) return CINNABAR_LIGHT;
        return JADE_LIGHT;
    }

    private LinearLayout pageBody() {
        LinearLayout body = vertical();
        body.setPadding(dp(16), dp(18), dp(16), dp(28));
        return body;
    }

    private LinearLayout dialogBody() {
        LinearLayout body = vertical();
        body.setPadding(dp(22), dp(8), dp(22), dp(8));
        return body;
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(0f, 1.16f);
        view.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        return view;
    }

    private TextView sectionTitle(String value) {
        TextView view = text(value, 17, INK, true);
        view.setPadding(0, dp(4), 0, dp(8));
        return view;
    }

    private EditText input(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(15);
        field.setTextColor(INK);
        field.setHintTextColor(Color.rgb(145, 145, 136));
        field.setSingleLine(false);
        field.setPadding(dp(13), dp(11), dp(13), dp(11));
        field.setBackground(roundRect(WHITE, 14, LINE));
        return field;
    }

    private EditText area(String hint, int lines) {
        EditText field = input(hint);
        field.setGravity(Gravity.TOP);
        field.setMinLines(lines);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        return field;
    }

    private View labeled(String label, View field) {
        LinearLayout box = vertical();
        TextView title = text(label, 12, MUTED, true);
        title.setPadding(0, 0, 0, dp(6));
        box.addView(title);
        box.addView(field, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return box;
    }

    private Button primaryButton(String label) {
        Button button = textButton(label, true);
        button.setTextColor(WHITE);
        button.setTextSize(14);
        button.setPadding(dp(14), dp(9), dp(14), dp(9));
        button.setBackground(ripple(JADE, 14, Color.TRANSPARENT));
        return button;
    }

    private Button outlineButton(String label) {
        Button button = textButton(label, true);
        button.setTextColor(JADE_DARK);
        button.setTextSize(13);
        button.setPadding(dp(12), dp(9), dp(12), dp(9));
        button.setBackground(ripple(WHITE, 14, LINE));
        return button;
    }

    private Button textButton(String label, boolean bold) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(INK);
        button.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(0);
        button.setMinWidth(0);
        button.setPadding(dp(10), dp(7), dp(10), dp(7));
        button.setBackgroundColor(Color.TRANSPARENT);
        return button;
    }

    private Button filterChip(String label, boolean selected, Runnable action) {
        Button button = textButton(label, selected);
        button.setTextSize(12);
        button.setOnClickListener(v -> action.run());
        styleFilterChip(button, selected);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(39));
        params.rightMargin = dp(7);
        button.setLayoutParams(params);
        return button;
    }

    private void styleFilterChip(Button button, boolean selected) {
        button.setTypeface(Typeface.create("sans", selected ? Typeface.BOLD : Typeface.NORMAL));
        button.setTextColor(selected ? WHITE : JADE_DARK);
        button.setBackground(ripple(selected ? JADE : WHITE, 14, selected ? Color.TRANSPARENT : LINE));
    }

    private LinearLayout horizontalChipRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private HorizontalScrollView horizontalScroll(View child) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(false);
        scroll.addView(child);
        return scroll;
    }

    private ScrollView scroll(View child) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.addView(child, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private View emptyState(String title, String note) {
        LinearLayout box = vertical();
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(20), dp(30), dp(20), dp(30));
        box.setBackground(roundRect(WHITE, 19, Color.TRANSPARENT));
        box.addView(text("·", 30, GOLD, true));
        TextView heading = text(title, 17, INK, true);
        heading.setGravity(Gravity.CENTER);
        box.addView(heading);
        if (note != null && !note.isEmpty()) {
            TextView desc = text(note, 13, MUTED, false);
            desc.setGravity(Gravity.CENTER);
            desc.setPadding(0, dp(7), 0, 0);
            box.addView(desc);
        }
        return box;
    }

    private Drawable roundRect(int fill, float radiusDp, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        if (stroke != Color.TRANSPARENT) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private Drawable ripple(int fill, float radiusDp, int stroke) {
        Drawable content = roundRect(fill, radiusDp, stroke);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return content;
        return new RippleDrawable(ColorStateList.valueOf(Color.argb(35, 23, 58, 53)), content, null);
    }

    private int mixColor(int first, int second, float secondAmount) {
        float firstAmount = 1f - secondAmount;
        return Color.rgb(
                Math.round(Color.red(first) * firstAmount + Color.red(second) * secondAmount),
                Math.round(Color.green(first) * firstAmount + Color.green(second) * secondAmount),
                Math.round(Color.blue(first) * firstAmount + Color.blue(second) * secondAmount)
        );
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private LinearLayout.LayoutParams spaced(int bottomDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(bottomDp);
        return params;
    }

    private void addGap(LinearLayout parent, int widthDp) {
        View gap = new View(this);
        parent.addView(gap, new LinearLayout.LayoutParams(dp(widthDp), 1));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private static String safe(String value) { return value == null ? "" : value; }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }

    private void restorePage(Bundle state) {
        recipeQuery = state.getString("recipeQuery", "");
        recipeScope = state.getString("recipeScope", SCOPE_ALL);
        recipeCategory = state.getString("recipeCategory", RecipeCategories.ALL);
        pantryFilter = state.getString("pantryFilter", "ALL");
        currentRecipeId = state.getString("recipeId", "");
        detailReturnPage = state.getString("detailReturnPage", "RECIPES");
        String page = state.getString("page", "HOME");
        if ("RECIPES".equals(page)) showRecipes(recipeScope);
        else if ("PANTRY".equals(page)) showPantry();
        else if ("PANTRY_MATCHES".equals(page)) showPantryMatches();
        else if ("SPECIALS".equals(page) || "SPECIAL_DETAIL".equals(page)) showSpecials();
        else if ("SHOPPING".equals(page)) showShoppingList();
        else if ("DETAIL".equals(page)) {
            Recipe recipe = repository.findById(currentRecipeId);
            if (recipe != null) showRecipeDetail(recipe); else showRecipes(SCOPE_ALL);
        } else if ("FORM".equals(page)) {
            Recipe recipe = repository.findById(currentRecipeId);
            showRecipeForm(recipe);
        } else showHome();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("page", currentPage);
        outState.putString("recipeId", currentRecipeId);
        outState.putString("recipeQuery", recipeQuery);
        outState.putString("recipeScope", recipeScope);
        outState.putString("recipeCategory", recipeCategory);
        outState.putString("pantryFilter", pantryFilter);
        outState.putString("detailReturnPage", detailReturnPage);
    }

    private void handleBack() {
        if (backAction != null) backAction.run();
        else if (!"HOME".equals(currentPage)) showHome();
        else finish();
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) handleBack();
        else super.onBackPressed();
    }
}
