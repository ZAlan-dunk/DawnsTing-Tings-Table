package com.dawns.tingstable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.dawns.tingstable.data.RecipeRepository;
import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int PAPER = Color.rgb(244, 235, 221);
    private static final int INK = Color.rgb(36, 51, 47);
    private static final int JADE = Color.rgb(40, 83, 74);
    private static final int JADE_DARK = Color.rgb(23, 58, 53);
    private static final int JADE_LIGHT = Color.rgb(228, 238, 231);
    private static final int CINNABAR = Color.rgb(182, 93, 76);
    private static final int GOLD = Color.rgb(168, 132, 66);
    private static final int MUTED = Color.rgb(111, 117, 110);
    private static final int LINE = Color.rgb(216, 203, 183);
    private static final int WHITE = Color.rgb(255, 253, 248);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private RecipeRepository repository;
    private FrameLayout root;
    private LinearLayout shell;
    private AmbientBackgroundView ambientBackground;
    private LinearLayout topBar;
    private LinearLayout bottomNav;
    private FrameLayout content;
    private TextView titleView;
    private Button backButton;
    private Button[] navButtons = new Button[5];
    private Runnable backAction;
    private Runnable pendingCommit;
    private View undoBar;
    private Runnable undoTimeout;

    private String currentPage = "HOME";
    private String currentRecipeId = "";
    private String recipeQuery = "";
    private String favoriteQuery = "";
    private String ingredientQuery = "";
    private String detailReturnPage = "RECIPES";
    private int systemTopInset;
    private int systemBottomInset;
    private int systemLeftInset;
    private int systemRightInset;
    private int imeBottomInset;

    private EditText formName;
    private EditText formCategory;
    private EditText formFlavor;
    private EditText formDifficulty;
    private EditText formMinutes;
    private EditText formServings;
    private EditText formMainIngredients;
    private EditText formStaples;
    private EditText formSteps;
    private EditText formTips;
    private Recipe formExisting;
    private String formInitialSignature = "";
    private boolean formSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new RecipeRepository(this);
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

        ambientBackground = new AmbientBackgroundView(this);
        root.addView(ambientBackground, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(Color.TRANSPARENT);

        topBar = new LinearLayout(this);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setBackground(gradientRect(JADE_DARK, JADE, 0, 0, 0, 0));
        topBar.setMinimumHeight(dp(64));

        backButton = textButton("‹", true);
        backButton.setTextSize(24);
        backButton.setContentDescription("返回");
        backButton.setVisibility(View.GONE);
        topBar.addView(backButton, wrapParams(dp(48), ViewGroup.LayoutParams.WRAP_CONTENT));

        titleView = text("婷馔清欢", 21, WHITE, true);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setMaxLines(2);
        topBar.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        shell.addView(topBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content = new FrameLayout(this);
        content.setBackgroundColor(Color.TRANSPARENT);
        shell.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        bottomNav = new LinearLayout(this);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setBackground(roundRect(Color.argb(238, 255, 253, 248), 22, LINE));
        bottomNav.setMinimumHeight(dp(64));
        addNav(0, "首页", com.dawns.tingstable.R.drawable.ic_nav_home, this::showHome);
        addNav(1, "配方", com.dawns.tingstable.R.drawable.ic_nav_recipes, () -> showRecipes(false));
        addNav(2, "选菜", com.dawns.tingstable.R.drawable.ic_nav_ingredients, this::showIngredientPicker);
        addNav(3, "收藏", com.dawns.tingstable.R.drawable.ic_nav_favorite, () -> showRecipes(true));
        addNav(4, "清单", com.dawns.tingstable.R.drawable.ic_nav_shopping, this::showShoppingList);
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
        button.setTextSize(12);
        button.setMinHeight(dp(56));
        button.setCompoundDrawablesWithIntrinsicBounds(0, iconRes, 0, 0);
        button.setCompoundDrawablePadding(dp(1));
        button.setContentDescription(label);
        button.setOnClickListener(view -> action.run());
        navButtons[index] = button;
        bottomNav.addView(button, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    }

    private void applySafeInsets() {
        if (topBar == null) return;
        topBar.setPadding(dp(12) + systemLeftInset, dp(8) + systemTopInset, dp(16) + systemRightInset, dp(8));
        bottomNav.setPadding(dp(4) + systemLeftInset, dp(5), dp(4) + systemRightInset, dp(5) + systemBottomInset);
        int bottom = bottomNav.getVisibility() == View.VISIBLE ? 0 : Math.max(systemBottomInset, imeBottomInset);
        content.setPadding(systemLeftInset, 0, systemRightInset, bottom);
        ViewCompat.requestApplyInsets(content);
    }

    private void setNavSelection(int selected) {
        for (int i = 0; i < navButtons.length; i++) {
            Button button = navButtons[i];
            if (button == null) continue;
            boolean active = i == selected;
            button.setSelected(active);
            button.setTextColor(active ? JADE_DARK : MUTED);
            button.setBackground(ripple(active ? JADE_LIGHT : Color.TRANSPARENT, 14, Color.TRANSPARENT));
            button.setCompoundDrawableTintList(ColorStateList.valueOf(active ? JADE_DARK : MUTED));
            button.setContentDescription((active ? "当前页面：" : "打开") + button.getText());
        }
    }

    private void setPage(String page, String title, Runnable onBack, View view, boolean showNavigation) {
        currentPage = page;
        titleView.setText(title);
        backAction = onBack;
        backButton.setVisibility(onBack == null ? View.GONE : View.VISIBLE);
        backButton.setOnClickListener(v -> { if (backAction != null) backAction.run(); });
        bottomNav.setVisibility(showNavigation ? View.VISIBLE : View.GONE);
        if (showNavigation) setNavSelection(navIndex(page)); else setNavSelection(-1);
        content.removeAllViews();
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        int width = screenWidthDp >= 600 ? dp(Math.min(760, Math.max(320, screenWidthDp - 32))) : ViewGroup.LayoutParams.MATCH_PARENT;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL);
        content.addView(view, params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ValueAnimator.areAnimatorsEnabled()) {
            view.setAlpha(0f);
            view.setTranslationY(dp(8));
            view.animate().alpha(1f).translationY(0f).setDuration(220L).start();
        } else {
            view.setAlpha(1f);
            view.setTranslationY(0f);
        }
        applySafeInsets();
    }

    private int navIndex(String page) {
        if ("HOME".equals(page)) return 0;
        if ("RECIPES".equals(page)) return 1;
        if ("PICKER".equals(page)) return 2;
        if ("FAVORITES".equals(page)) return 3;
        if ("SHOPPING".equals(page)) return 4;
        return -1;
    }

    private void showHome() {
        LinearLayout body = pageBody(16);
        LinearLayout hero = card();
        hero.setPadding(dp(22), dp(24), dp(22), dp(22));
        hero.setElevation(dp(4));
        hero.setBackground(gradientRect(JADE_DARK, JADE, 22, 22, 22, 22));
        TextView eyebrow = text("食  笺", 12, Color.rgb(232, 210, 160), true);
        eyebrow.setLetterSpacing(0.18f);
        hero.addView(eyebrow);
        TextView brand = text("婷馔清欢", 32, WHITE, true);
        brand.setPadding(0, dp(10), 0, dp(3));
        hero.addView(brand);
        hero.addView(text("人间有味，四时清欢。", 17, Color.rgb(245, 237, 214), false));
        View rule = new View(this);
        rule.setBackgroundColor(Color.argb(100, 232, 210, 160));
        LinearLayout.LayoutParams ruleParams = new LinearLayout.LayoutParams(dp(54), dp(1));
        ruleParams.topMargin = dp(16);
        ruleParams.bottomMargin = dp(14);
        hero.addView(rule, ruleParams);
        LinearLayout marks = new LinearLayout(this);
        marks.setGravity(Gravity.CENTER_VERTICAL);
        marks.addView(text("家常", 12, Color.rgb(245, 237, 214), false));
        addSpacer(marks, 16, 1);
        marks.addView(text("四时", 12, Color.rgb(245, 237, 214), false));
        addSpacer(marks, 16, 1);
        marks.addView(text("清欢", 12, Color.rgb(245, 237, 214), false));
        hero.addView(marks);
        body.addView(hero, matchWrap(18));

        body.addView(section("今日食意"), matchWrap(8));
        body.addView(actionCard("配方表", "翻开一份家常做法。", "查看全部配方", () -> showRecipes(false)), matchWrap(10));
        body.addView(actionCard("通过食材选择菜单", "从手边已有的食材开始。", "开始选择食材", this::showIngredientPicker), matchWrap(14));

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        Button favorites = outlineButton("收藏  ·  " + repository.getFavorites().size());
        favorites.setOnClickListener(v -> showRecipes(true));
        quick.addView(favorites, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        addSpacer(quick, 10, 1);
        Button shopping = outlineButton("清单  ·  " + repository.getShoppingItems().size());
        shopping.setOnClickListener(v -> showShoppingList());
        quick.addView(shopping, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        body.addView(quick, matchWrap(8));
        setPage("HOME", "婷馔清欢", null, scroll(body), true);
    }

    private View actionCard(String title, String description, String buttonText, Runnable action) {
        LinearLayout box = card();
        box.setClickable(true);
        box.setOnClickListener(v -> { press(v); action.run(); });
        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(this);
        icon.setImageResource(title.startsWith("配方") ? R.drawable.ic_home_recipe : R.drawable.ic_home_picker);
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        icon.setBackground(roundRect(JADE_LIGHT, 16, Color.TRANSPARENT));
        heading.addView(icon, wrapParams(dp(52), dp(52)));
        TextView headingText = text(title, 20, INK, true);
        headingText.setPadding(dp(12), 0, 0, 0);
        heading.addView(headingText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.addView(heading);
        TextView desc = text(description, 14, MUTED, false);
        desc.setPadding(0, dp(12), 0, dp(12));
        box.addView(desc);
        Button button = primaryButton(buttonText);
        button.setOnClickListener(v -> { press(v); action.run(); });
        box.addView(button, wrapParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        return box;
    }

    private void showRecipes(boolean favoritesOnly) {
        String page = favoritesOnly ? "FAVORITES" : "RECIPES";
        String initialQuery = favoritesOnly ? favoriteQuery : recipeQuery;
        LinearLayout body = pageBody(12);
        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        EditText search = input("搜索菜名或食材");
        search.setText(initialQuery);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchRow.addView(search, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        addSpacer(searchRow, 6, 1);
        Button clear = textButton("×", true);
        clear.setTextSize(20);
        clear.setContentDescription("清除搜索");
        clear.setVisibility(initialQuery.isEmpty() ? View.GONE : View.VISIBLE);
        clear.setOnClickListener(v -> search.setText(""));
        searchRow.addView(clear, wrapParams(dp(48), dp(52)));
        body.addView(searchRow, matchWrap(8));

        Button add = primaryButton("＋ 添加自定义菜谱");
        add.setOnClickListener(v -> showRecipeForm(null));
        body.addView(add, matchWrap(10));

        TextView counter = text("", 13, MUTED, false);
        counter.setPadding(dp(2), 0, 0, dp(6));
        body.addView(counter);
        LinearLayout list = vertical(10);
        body.addView(list);
        Runnable render = () -> {
            String query = search.getText().toString();
            if (favoritesOnly) favoriteQuery = query; else recipeQuery = query;
            clear.setVisibility(query.trim().isEmpty() ? View.GONE : View.VISIBLE);
            renderRecipeRows(list, counter, query, favoritesOnly);
        };
        render.run();
        search.addTextChangedListener(simpleWatcher(render));
        search.setOnEditorActionListener((v, actionId, event) -> {
            render.run();
            return actionId == EditorInfo.IME_ACTION_SEARCH;
        });
        setPage(page, favoritesOnly ? "我的收藏" : "配方表", null, scroll(body), true);
    }

    private void renderRecipeRows(LinearLayout list, TextView counter, String query, boolean favoritesOnly) {
        list.removeAllViews();
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        Set<String> favorites = repository.getFavorites();
        int count = 0;
        for (Recipe recipe : repository.getAllRecipes()) {
            if (favoritesOnly && !favorites.contains(recipe.id)) continue;
            if (!normalized.isEmpty() && !recipeMatchesQuery(recipe, normalized)) continue;
            list.addView(recipeCard(recipe, favoritesOnly ? "FAVORITES" : "RECIPES", () -> renderRecipeRows(list, counter, query, favoritesOnly)), matchWrap(10));
            count++;
        }
        int countText = favoritesOnly
                ? (normalized.isEmpty() ? R.string.favorite_count : R.string.favorite_search_count)
                : (normalized.isEmpty() ? R.string.recipe_count : R.string.recipe_search_count);
        counter.setText(getString(countText, count));
        if (count == 0) {
            list.addView(emptyState(favoritesOnly ? "还没有收藏配方\n在配方表中点击 ☆ 收藏即可保存常用菜。" : "没有找到相关配方\n可以清除搜索，或添加自己的家常菜谱。"), matchWrap(8));
        }
    }

    private boolean recipeMatchesQuery(Recipe recipe, String normalized) {
        String direct = (recipe.name + " " + recipe.category + " " + recipe.flavor + " " + recipe.difficulty).toLowerCase(Locale.ROOT);
        if (direct.contains(normalized)) return true;
        for (Ingredient ingredient : recipe.ingredients) {
            if (RecipeMatcher.queryMatchesIngredient(normalized, ingredient.name)) return true;
        }
        return false;
    }

    private View recipeCard(Recipe recipe, String returnPage, Runnable refresh) {
        String visualGroup = recipeVisualGroup(recipe);
        int tone = toneForGroup(visualGroup);
        int soft = softForGroup(visualGroup);

        FrameLayout frame = new FrameLayout(this);
        frame.setClickable(true);
        frame.setFocusable(true);
        frame.setElevation(dp(2));
        frame.setBackground(gradientRoundRect(mixColor(WHITE, soft, 0.34f), WHITE, 18, mixColor(tone, LINE, 0.70f)));
        frame.setOnClickListener(v -> { press(v); openRecipeDetail(recipe, returnPage); });

        BotanicalMotifView motif = new BotanicalMotifView(this, tone);
        motif.setAlpha(0.72f);
        frame.addView(motif, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View accent = new View(this);
        accent.setBackground(roundRect(tone, 4, Color.TRANSPARENT));
        FrameLayout.LayoutParams accentParams = new FrameLayout.LayoutParams(dp(5), dp(62), Gravity.START | Gravity.TOP);
        accentParams.topMargin = dp(16);
        frame.addView(accent, accentParams);

        LinearLayout contentBox = vertical(8);
        contentBox.setPadding(dp(17), dp(15), dp(14), dp(14));

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView seal = text(groupSymbol(visualGroup), 14, WHITE, true);
        seal.setGravity(Gravity.CENTER);
        seal.setBackground(gradientRoundRect(tone, mixColor(tone, GOLD, 0.22f), 14, Color.TRANSPARENT));
        heading.addView(seal, wrapParams(dp(44), dp(44)));

        LinearLayout titleCopy = vertical(1);
        titleCopy.setPadding(dp(11), 0, dp(6), 0);
        titleCopy.addView(text(recipe.name, 19, INK, true));
        TextView flavor = text(recipe.flavor, 12, mixColor(tone, MUTED, 0.34f), false);
        flavor.setMaxLines(1);
        titleCopy.addView(flavor);
        heading.addView(titleCopy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        boolean favoriteState = repository.isFavorite(recipe.id);
        Button favorite = textButton(favoriteState ? "★" : "☆", false);
        favorite.setTextSize(21);
        favorite.setTextColor(favoriteState ? GOLD : mixColor(tone, MUTED, 0.25f));
        favorite.setMinWidth(0);
        favorite.setMinHeight(0);
        favorite.setPadding(0, 0, 0, dp(2));
        favorite.setBackground(ripple(favoriteState ? mixColor(soft, GOLD, 0.12f) : Color.argb(172, 255, 253, 248), 20, favoriteState ? mixColor(GOLD, LINE, 0.55f) : Color.TRANSPARENT));
        favorite.setContentDescription(favoriteState ? "取消收藏" + recipe.name : "收藏" + recipe.name);
        favorite.setOnClickListener(v -> { pulse(v); repository.toggleFavorite(recipe.id); refresh.run(); });
        heading.addView(favorite, wrapParams(dp(42), dp(42)));
        contentBox.addView(heading);

        LinearLayout meta = new LinearLayout(this);
        meta.setGravity(Gravity.CENTER_VERTICAL);
        meta.addView(pill(recipe.category, tone, soft), wrapParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(29)));
        addSpacer(meta, 6, 1);
        meta.addView(pill(recipe.minutes + " 分钟", JADE, JADE_LIGHT), wrapParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(29)));
        addSpacer(meta, 6, 1);
        meta.addView(pill(recipe.difficulty, GOLD, mixColor(WHITE, GOLD, 0.12f)), wrapParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(29)));
        if (recipe.custom) {
            addSpacer(meta, 6, 1);
            meta.addView(pill("私房", CINNABAR, mixColor(WHITE, CINNABAR, 0.10f)), wrapParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(29)));
        }
        contentBox.addView(meta);

        LinearLayout ingredients = new LinearLayout(this);
        ingredients.setGravity(Gravity.CENTER_VERTICAL);
        ingredients.setPadding(dp(11), dp(8), dp(11), dp(8));
        ingredients.setBackground(roundRect(Color.argb(155, Color.red(soft), Color.green(soft), Color.blue(soft)), 12, Color.TRANSPARENT));
        TextView leaf = text("❧", 17, tone, false);
        leaf.setGravity(Gravity.CENTER);
        ingredients.addView(leaf, wrapParams(dp(26), ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView ingredientLine = text(ingredientSummary(recipe), 13, mixColor(INK, tone, 0.18f), false);
        ingredientLine.setMaxLines(2);
        ingredients.addView(ingredientLine, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        contentBox.addView(ingredients);

        frame.addView(contentBox, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return frame;
    }

    private String ingredientSummary(Recipe recipe) {
        List<String> names = new ArrayList<>();
        for (Ingredient ingredient : recipe.ingredients) if (!ingredient.staple) names.add(ingredient.name);
        return names.isEmpty() ? "基础调味料" : String.join("、", names);
    }

    private void showRecipeDetail(Recipe recipe) {
        Recipe fresh = repository.findById(recipe.id);
        if (fresh == null) { returnFromRecipeDetail(); return; }
        currentRecipeId = fresh.id;
        LinearLayout body = pageBody(12);
        LinearLayout heading = card();
        heading.addView(text(fresh.name, 28, INK, true));
        TextView lead = text("一份可以随时回看的家常做法。", 14, MUTED, false);
        lead.setPadding(0, dp(7), 0, dp(12));
        heading.addView(lead);
        heading.addView(metaRows(fresh));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(isWide() ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        Button favorite = primaryButton(repository.isFavorite(fresh.id) ? "★ 已收藏" : "☆ 收藏配方");
        favorite.setContentDescription(repository.isFavorite(fresh.id) ? "取消收藏" + fresh.name : "收藏" + fresh.name);
        favorite.setOnClickListener(v -> { repository.toggleFavorite(fresh.id); showRecipeDetail(fresh); });
        actions.addView(favorite, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (fresh.custom) {
            addSpacer(actions, isWide() ? 10 : 1, isWide() ? 1 : 8);
            Button edit = outlineButton("编辑这份配方");
            edit.setOnClickListener(v -> showRecipeForm(fresh));
            actions.addView(edit, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        heading.addView(actions, matchWrap(4));
        body.addView(heading, matchWrap(8));

        body.addView(section("食材"), matchWrap(4));
        LinearLayout ingredientBox = card();
        for (Ingredient ingredient : fresh.ingredients) ingredientBox.addView(ingredientRow(ingredient), matchWrap(6));
        body.addView(ingredientBox, matchWrap(10));

        body.addView(section("制作步骤"), matchWrap(4));
        LinearLayout stepsBox = card();
        for (int i = 0; i < fresh.steps.size(); i++) stepsBox.addView(stepRow(i + 1, fresh.steps.get(i)), matchWrap(12));
        body.addView(stepsBox, matchWrap(10));

        if (fresh.tips != null && !fresh.tips.trim().isEmpty()) {
            LinearLayout tip = card();
            tip.setBackground(roundRect(JADE_LIGHT, 16, Color.TRANSPARENT));
            tip.addView(text("小贴士", 16, JADE_DARK, true));
            TextView tipText = text(fresh.tips, 14, INK, false);
            tipText.setPadding(0, dp(7), 0, 0);
            tip.addView(tipText);
            body.addView(tip, matchWrap(10));
        }
        if (fresh.custom) {
            Button delete = outlineButton("删除这份自定义配方");
            delete.setTextColor(CINNABAR);
            delete.setOnClickListener(v -> confirmDelete(fresh));
            body.addView(delete, matchWrap(20));
        }
        setPage("DETAIL", fresh.name, this::returnFromRecipeDetail, scroll(body), false);
    }

    private void openRecipeDetail(Recipe recipe, String returnPage) {
        detailReturnPage = returnPage;
        showRecipeDetail(recipe);
    }

    private void returnFromRecipeDetail() {
        if ("FAVORITES".equals(detailReturnPage)) showRecipes(true);
        else if ("PICKER".equals(detailReturnPage)) showIngredientPicker();
        else showRecipes(false);
    }
    private LinearLayout metaRows(Recipe recipe) {
        LinearLayout result = vertical(6);
        String[][] values = {{"用时", recipe.minutes + " 分钟"}, {"人数", recipe.servings + " 人份"}, {"难度", recipe.difficulty}, {"口味", recipe.flavor}};
        int columns = isWide() ? 4 : 2;
        for (int i = 0; i < values.length; i += columns) {
            LinearLayout row = new LinearLayout(this);
            for (int j = 0; j < columns; j++) {
                int index = i + j;
                if (index < values.length) row.addView(metaChip(values[index][0], values[index][1]), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                else row.addView(new Space(this), new LinearLayout.LayoutParams(0, 1, 1));
                if (j < columns - 1) addSpacer(row, 6, 1);
            }
            result.addView(row);
        }
        return result;
    }

    private View metaChip(String label, String value) {
        LinearLayout chip = vertical(1);
        chip.setPadding(dp(10), dp(8), dp(10), dp(8));
        chip.setBackground(roundRect(JADE_LIGHT, 12, Color.TRANSPARENT));
        chip.addView(text(label, 11, MUTED, false));
        chip.addView(text(value, 14, JADE_DARK, true));
        return chip;
    }

    private View ingredientRow(Ingredient ingredient) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = text(ingredient.name + (ingredient.staple ? " · 常备" : ""), 15, INK, false);
        row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView amount = text(ingredient.amount, 15, JADE_DARK, true);
        amount.setGravity(Gravity.END);
        row.addView(amount, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private View stepRow(int number, String step) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.TOP);
        TextView index = text(String.valueOf(number), 14, WHITE, true);
        index.setGravity(Gravity.CENTER);
        index.setBackground(roundRect(JADE, 16, Color.TRANSPARENT));
        row.addView(index, wrapParams(dp(32), dp(32)));
        TextView copy = text(step, 15, INK, false);
        copy.setPadding(dp(12), dp(3), 0, 0);
        row.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private void confirmDelete(Recipe recipe) {
        new AlertDialog.Builder(this)
                .setTitle("删除自定义配方？")
                .setMessage("删除后无法从应用内恢复这份配方。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    repository.deleteCustomRecipe(recipe.id);
                    toast("配方已删除");
                    showRecipes(false);
                }).show();
    }

    private void showIngredientPicker() {
        currentPage = "PICKER";
        Set<String> selected = new LinkedHashSet<>(repository.getSelectedIngredients());
        LinearLayout body = pageBody(10);
        LinearLayout intro = card();
        intro.addView(text("手边有什么，就从什么开始", 20, INK, true));
        TextView desc = text("常备调料默认视为已有。匹配结果会区分“可以制作”和“还差一点”。", 13, MUTED, false);
        desc.setPadding(0, dp(8), 0, 0);
        intro.addView(desc);
        body.addView(intro, matchWrap(10));

        LinearLayout searchRow = new LinearLayout(this);
        EditText search = input("搜索食材");
        search.setText(ingredientQuery);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchRow.addView(search, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        addSpacer(searchRow, 6, 1);
        Button clearSearch = textButton("×", true);
        clearSearch.setTextSize(20);
        clearSearch.setContentDescription("清除食材搜索");
        clearSearch.setVisibility(ingredientQuery.isEmpty() ? View.GONE : View.VISIBLE);
        clearSearch.setOnClickListener(v -> search.setText(""));
        searchRow.addView(clearSearch, wrapParams(dp(48), dp(52)));
        body.addView(searchRow, matchWrap(8));

        TextView selectedTitle = text("已选择 0 种", 14, JADE_DARK, true);
        body.addView(selectedTitle, matchWrap(4));
        HorizontalScrollView chipsScroll = new HorizontalScrollView(this);
        chipsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = new LinearLayout(this);
        chips.setPadding(0, dp(2), 0, dp(6));
        chipsScroll.addView(chips, new HorizontalScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        body.addView(chipsScroll, matchWrap(4));

        LinearLayout checks = vertical(6);
        TextView emptyIngredients = text("", 13, MUTED, false);
        body.addView(emptyIngredients);
        body.addView(checks, matchWrap(8));
        LinearLayout results = vertical(8);

        Runnable[] updateSummary = new Runnable[1];
        updateSummary[0] = () -> {
            selectedTitle.setText(getString(R.string.selected_ingredient_count, selected.size()));
            chips.removeAllViews();
            for (String item : new ArrayList<>(selected)) {
                Button chip = textButton(item + " ×", false);
                chip.setTextSize(12);
                String group = ingredientGroup(item);
                int chipTone = toneForGroup(group);
                int chipSoft = softForGroup(group);
                chip.setTextColor(chipTone);
                chip.setMinHeight(dp(40));
                chip.setContentDescription("移除已选食材" + item);
                chip.setBackground(ripple(chipSoft, 20, mixColor(chipTone, chipSoft, 0.70f)));
                chip.setOnClickListener(v -> {
                    selected.remove(item);
                    repository.saveSelectedIngredients(selected);
                    updateSummary[0].run();
                    rebuildIngredientChecks(checks, search.getText().toString(), selected, updateSummary[0], emptyIngredients);
                });
                chips.addView(chip, wrapParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)));
                addSpacer(chips, 6, 1);
            }
        };
        Runnable[] rebuildHolder = new Runnable[1];
        rebuildHolder[0] = () -> rebuildIngredientChecks(checks, search.getText().toString(), selected, updateSummary[0], emptyIngredients);
        rebuildHolder[0].run();
        updateSummary[0].run();
        search.addTextChangedListener(simpleWatcher(() -> {
            ingredientQuery = search.getText().toString();
            clearSearch.setVisibility(ingredientQuery.trim().isEmpty() ? View.GONE : View.VISIBLE);
            rebuildHolder[0].run();
        }));

        LinearLayout actions = new LinearLayout(this);
        Button match = primaryButton("看看能做什么");
        match.setOnClickListener(v -> renderMatches(results, selected));
        actions.addView(match, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
        addSpacer(actions, 10, 1);
        Button clear = outlineButton("清空已选");
        clear.setOnClickListener(v -> {
            if (selected.isEmpty()) return;
            new AlertDialog.Builder(this).setTitle("清空已选食材？").setMessage("当前选择不会影响菜谱和采购清单。")
                    .setNegativeButton("取消", null).setPositiveButton("清空", (dialog, which) -> {
                        selected.clear();
                        repository.saveSelectedIngredients(selected);
                        rebuildHolder[0].run();
                        updateSummary[0].run();
                        results.removeAllViews();
                    }).show();
        });
        actions.addView(clear, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        body.addView(actions, matchWrap(12));
        body.addView(results);
        setPage("PICKER", "按食材选菜", null, bodyPage(body), true);
    }

    private void rebuildIngredientChecks(LinearLayout checks, String query, Set<String> selected, Runnable updateSummary, TextView emptyView) {
        checks.removeAllViews();
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (String group : ingredientGroupOrder()) groups.put(group, new ArrayList<>());
        int count = 0;
        for (String name : repository.getAllIngredientNames()) {
            if (!normalized.isEmpty() && !RecipeMatcher.queryMatchesIngredient(normalized, name)) continue;
            groups.get(ingredientGroup(name)).add(name);
            count++;
        }

        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            String group = entry.getKey();
            int tone = toneForGroup(group);
            int soft = softForGroup(group);
            LinearLayout panel = vertical(7);
            panel.setPadding(dp(11), dp(11), dp(11), dp(6));
            panel.setBackground(gradientRoundRect(mixColor(WHITE, soft, 0.55f), Color.argb(246, 255, 253, 248), 18, mixColor(tone, LINE, 0.72f)));

            LinearLayout header = new LinearLayout(this);
            header.setGravity(Gravity.CENTER_VERTICAL);
            TextView symbol = text(groupSymbol(group), 14, WHITE, true);
            symbol.setGravity(Gravity.CENTER);
            symbol.setBackground(roundRect(tone, 14, Color.TRANSPARENT));
            header.addView(symbol, wrapParams(dp(38), dp(38)));
            LinearLayout headingCopy = vertical(0);
            headingCopy.setPadding(dp(10), 0, 0, 0);
            headingCopy.addView(text(group, 16, mixColor(INK, tone, 0.24f), true));
            int selectedInGroup = 0;
            for (String item : entry.getValue()) if (selected.contains(item)) selectedInGroup++;
            TextView groupMeta = text(entry.getValue().size() + " 种食材 · 已选 " + selectedInGroup, 11, MUTED, false);
            headingCopy.addView(groupMeta);
            header.addView(headingCopy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            TextView flourish = text("— ❧", 14, mixColor(tone, soft, 0.50f), false);
            header.addView(flourish);
            panel.addView(header, matchWrap(3));

            GridLayout grid = new GridLayout(this);
            int columns = isWide() ? 3 : 2;
            grid.setColumnCount(columns);
            grid.setUseDefaultMargins(false);
            for (int index = 0; index < entry.getValue().size(); index++) {
                String name = entry.getValue().get(index);
                CheckBox checkBox = ingredientCheck(name, selected.contains(name), tone, soft);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) selected.add(name); else selected.remove(name);
                    repository.saveSelectedIngredients(selected);
                    styleIngredientCheck(checkBox, isChecked, tone, soft);
                    int nowSelected = 0;
                    for (String item : entry.getValue()) if (selected.contains(item)) nowSelected++;
                    groupMeta.setText(String.format(Locale.getDefault(), "%d 种食材 · 已选 %d", entry.getValue().size(), nowSelected));
                    pulse(checkBox);
                    updateSummary.run();
                });
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = dp(50);
                params.columnSpec = GridLayout.spec(index % columns, 1f);
                params.rowSpec = GridLayout.spec(index / columns);
                params.setMargins(dp(3), dp(3), dp(3), dp(3));
                grid.addView(checkBox, params);
            }
            panel.addView(grid, matchWrap(0));
            checks.addView(panel, matchWrap(10));
        }
        emptyView.setText(count == 0 ? "没有找到这个食材，可以尝试更短的名称。" : "");
    }

    private void renderMatches(LinearLayout results, Set<String> selected) {
        results.removeAllViews();
        if (selected.isEmpty()) {
            results.addView(emptyState("请至少选择一种主要食材。"));
            return;
        }
        List<RecipeMatcher.Match> matches = RecipeMatcher.match(repository.getAllRecipes(), selected);
        if (matches.isEmpty()) {
            results.addView(emptyState("暂时没有接近的配方。\n可以继续选择食材，或先浏览配方表。"));
            return;
        }
        int readyCount = 0;
        int almostCount = 0;
        for (RecipeMatcher.Match match : matches) if (match.canCook()) readyCount++; else almostCount++;
        results.addView(section("匹配结果 · " + matches.size() + " 道"), matchWrap(4));
        boolean shownReadyTitle = false;
        boolean shownAlmostTitle = false;
        for (RecipeMatcher.Match match : matches) {
            if (match.canCook() && !shownReadyTitle) {
                results.addView(text("可以制作 · " + readyCount + " 道", 15, JADE_DARK, true), matchWrap(6));
                shownReadyTitle = true;
            }
            if (!match.canCook() && !shownAlmostTitle) {
                TextView almost = text("还差一点 · " + almostCount + " 道", 15, CINNABAR, true);
                almost.setPadding(0, dp(10), 0, 0);
                results.addView(almost, matchWrap(6));
                shownAlmostTitle = true;
            }
            boolean ready = match.canCook();
            int statusTone = ready ? JADE : CINNABAR;
            int statusSoft = ready ? JADE_LIGHT : Color.rgb(247, 229, 218);
            LinearLayout box = vertical(8);
            box.setPadding(dp(15), dp(14), dp(15), dp(14));
            box.setElevation(dp(2));
            box.setBackground(gradientRoundRect(mixColor(WHITE, statusSoft, 0.55f), WHITE, 18, mixColor(statusTone, LINE, 0.66f)));
            box.setClickable(true);
            box.setOnClickListener(v -> { press(v); openRecipeDetail(match.recipe, "PICKER"); });

            LinearLayout matchHeading = new LinearLayout(this);
            matchHeading.setGravity(Gravity.CENTER_VERTICAL);
            TextView stateIcon = text(ready ? "✓" : "差", 14, WHITE, true);
            stateIcon.setGravity(Gravity.CENTER);
            stateIcon.setBackground(roundRect(statusTone, 16, Color.TRANSPARENT));
            matchHeading.addView(stateIcon, wrapParams(dp(40), dp(40)));
            LinearLayout matchCopy = vertical(1);
            matchCopy.setPadding(dp(10), 0, 0, 0);
            matchCopy.addView(text(match.recipe.name, 19, INK, true));
            matchCopy.addView(text(ready ? "食材齐备，可以下厨" : "缺 " + match.missing.size() + " 种食材", 12, statusTone, true));
            matchHeading.addView(matchCopy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            matchHeading.addView(pill(match.percent + "%", statusTone, statusSoft), wrapParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(30)));
            box.addView(matchHeading);

            LinearLayout progressTrack = new LinearLayout(this);
            progressTrack.setBackground(roundRect(mixColor(statusSoft, LINE, 0.26f), 3, Color.TRANSPARENT));
            View progress = new View(this);
            progress.setBackground(roundRect(statusTone, 3, Color.TRANSPARENT));
            progressTrack.addView(progress, new LinearLayout.LayoutParams(0, dp(6), Math.max(1, match.percent)));
            if (match.percent < 100) progressTrack.addView(new Space(this), new LinearLayout.LayoutParams(0, dp(6), 100 - match.percent));
            box.addView(progressTrack, wrapParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(6)));

            String status = "已有 " + match.matchedCount + "/" + match.requiredCount + " 种主要食材";
            box.addView(text(status, 12, MUTED, false));
            if (!match.missing.isEmpty()) {
                LinearLayout missingWrap = new LinearLayout(this);
                missingWrap.setGravity(Gravity.CENTER_VERTICAL);
                missingWrap.setPadding(dp(10), dp(8), dp(10), dp(8));
                missingWrap.setBackground(roundRect(Color.argb(175, Color.red(statusSoft), Color.green(statusSoft), Color.blue(statusSoft)), 11, Color.TRANSPARENT));
                missingWrap.addView(text("待备  ", 12, statusTone, true));
                missingWrap.addView(text(String.join("、", match.missing), 13, INK, false), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                box.addView(missingWrap);
                Button buy = outlineButton("加入采购清单");
                buy.setTextColor(statusTone);
                buy.setBackground(ripple(Color.argb(160, 255, 253, 248), 12, mixColor(statusTone, LINE, 0.55f)));
                buy.setOnClickListener(v -> { repository.addShoppingItems(match.missing); toast("已加入采购清单"); });
                box.addView(buy, wrapParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
            }
            results.addView(box, matchWrap(9));
        }
    }

    private void showShoppingList() {
        List<String> shopping = repository.getShoppingItems();
        LinearLayout body = pageBody(10);
        LinearLayout top = card();
        top.addView(text("采购清单", 23, INK, true));
        top.addView(text("买到后勾选即可移除，误操作可以在短时间内撤销。", 13, MUTED, false));
        body.addView(top, matchWrap(10));
        if (shopping.isEmpty()) {
            body.addView(emptyState("采购清单还是空的。\n可以从食材匹配结果中加入缺少的食材。"));
        } else {
            for (String item : shopping) {
                String group = ingredientGroup(item);
                int tone = toneForGroup(group);
                int soft = softForGroup(group);
                LinearLayout row = new LinearLayout(this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(7), dp(5), dp(11), dp(5));
                row.setElevation(dp(1));
                row.setBackground(gradientRoundRect(mixColor(WHITE, soft, 0.48f), WHITE, 15, mixColor(tone, LINE, 0.72f)));

                TextView symbol = text(groupSymbol(group), 13, WHITE, true);
                symbol.setGravity(Gravity.CENTER);
                symbol.setBackground(roundRect(tone, 14, Color.TRANSPARENT));
                row.addView(symbol, wrapParams(dp(38), dp(38)));

                CheckBox box = new CheckBox(this);
                box.setText(item);
                box.setTextSize(16);
                box.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                box.setTextColor(INK);
                box.setGravity(Gravity.CENTER_VERTICAL);
                box.setMinHeight(dp(52));
                box.setPadding(dp(10), 0, dp(4), 0);
                box.setButtonTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}}, new int[]{tone, mixColor(tone, LINE, 0.62f)}));
                box.setBackgroundColor(Color.TRANSPARENT);
                box.setContentDescription("标记已买到" + item);
                box.setOnCheckedChangeListener((button, checked) -> {
                    if (!checked) return;
                    pulse(row);
                    repository.removeShoppingItem(item);
                    showShoppingList();
                    showUndoMessage("已移除：" + item, () -> { repository.addShoppingItems(Collections.singletonList(item)); showShoppingList(); }, null);
                });
                row.addView(box, new LinearLayout.LayoutParams(0, dp(52), 1));
                row.addView(pill(group, tone, soft), wrapParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(28)));
                body.addView(row, matchWrap(8));
            }
            Button clear = outlineButton("清空采购清单");
            clear.setTextColor(CINNABAR);
            clear.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("清空采购清单？")
                    .setMessage("清单中的所有食材都会被移除。")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("清空", (dialog, which) -> { repository.clearShoppingList(); showShoppingList(); })
                    .show());
            body.addView(clear, matchWrap(18));
        }
        setPage("SHOPPING", "采购清单", null, scroll(body), true);
    }

    private void showUndoMessage(String message, Runnable undo, Runnable commit) {
        if (undoTimeout != null) {
            handler.removeCallbacks(undoTimeout);
            undoTimeout = null;
        }
        if (pendingCommit != null) pendingCommit.run();
        if (undoBar != null && undoBar.getParent() == content) content.removeView(undoBar);
        pendingCommit = () -> { pendingCommit = null; if (commit != null) commit.run(); };

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(14), dp(6), dp(6), dp(6));
        bar.setBackground(roundRect(JADE_DARK, 14, Color.TRANSPARENT));
        TextView label = text(message, 13, WHITE, false);
        bar.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Runnable timeout = () -> {
            if (undoBar != bar) return;
            if (pendingCommit != null) pendingCommit.run();
            if (bar.getParent() == content) content.removeView(bar);
            undoBar = null;
            undoTimeout = null;
        };
        undoTimeout = timeout;

        Button undoButton = textButton("撤销", true);
        undoButton.setTextColor(Color.rgb(255, 235, 180));
        undoButton.setOnClickListener(v -> {
            handler.removeCallbacks(timeout);
            undoTimeout = null;
            if (pendingCommit != null) {
                pendingCommit = null;
                undo.run();
            }
            if (bar.getParent() == content) content.removeView(bar);
            if (undoBar == bar) undoBar = null;
        });
        bar.addView(undoButton, wrapParams(dp(64), dp(44)));
        undoBar = bar;

        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        int barWidthDp = Math.min(420, Math.max(240, screenWidthDp - 24));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp(barWidthDp), ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        params.leftMargin = dp(12);
        params.rightMargin = dp(12);
        params.bottomMargin = dp(12) + (bottomNav.getVisibility() == View.VISIBLE ? dp(4) : 0);
        content.addView(bar, params);
        handler.postDelayed(timeout, 4000);
    }

    private void showRecipeForm(Recipe existing) {
        formExisting = existing;
        formSaved = false;
        currentPage = "FORM";
        LinearLayout body = pageBody(12);
        TextView notice = text("这是本机私有菜谱，只保存在当前设备中。", 13, MUTED, false);
        notice.setPadding(dp(2), 0, 0, dp(8));
        body.addView(notice);
        formName = labeledInput(body, "菜名", existing == null ? "" : existing.name, "例如：番茄豆腐汤");
        formCategory = labeledInput(body, "分类", existing == null ? "家常菜" : existing.category, "例如：快手菜");
        formFlavor = labeledInput(body, "口味", existing == null ? "家常" : existing.flavor, "例如：咸鲜");
        formDifficulty = labeledInput(body, "难度", existing == null ? "简单" : existing.difficulty, "简单 / 中等 / 需要耐心");
        formMinutes = labeledInput(body, "预计用时（分钟）", existing == null ? "20" : String.valueOf(existing.minutes), "请输入数字");
        formMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        formServings = labeledInput(body, "适合人数", existing == null ? "2" : String.valueOf(existing.servings), "请输入数字");
        formServings.setInputType(InputType.TYPE_CLASS_NUMBER);
        formMainIngredients = labeledArea(body, "主要食材", existing == null ? "" : ingredientsForEdit(existing), "每行一种，格式：名称 | 用量\n例如：西红柿 | 2个");
        formStaples = labeledArea(body, "常备调料（可选）", existing == null ? "" : staplesForEdit(existing), "每行一种，格式：名称 | 用量\n例如：盐 | 适量");
        formSteps = labeledArea(body, "制作步骤", existing == null ? "" : String.join("\n", existing.steps), "每行一步，从准备到出锅");
        formTips = labeledArea(body, "小贴士（可选）", existing == null ? "" : existing.tips, "例如：出锅前再加盐");
        formInitialSignature = formSignature();

        Button save = primaryButton(existing == null ? "保存到配方表" : "保存修改");
        save.setOnClickListener(v -> saveForm());
        body.addView(save, matchWrap(8));
        TextView hint = text("保存后可以在配方表中搜索、收藏，也可以用于按食材选菜。", 12, MUTED, false);
        hint.setPadding(dp(2), dp(6), dp(2), dp(20));
        body.addView(hint);
        Runnable leave = () -> leaveForm(existing);
        setPage("FORM", existing == null ? "添加配方" : "编辑配方", leave, scroll(body), false);
    }

    private void saveForm() {
        String recipeName = formName.getText().toString().trim();
        List<Ingredient> parsedIngredients = parseIngredients(formMainIngredients.getText().toString(), formStaples.getText().toString());
        List<String> parsedSteps = parseLines(formSteps.getText().toString());
        boolean valid = true;
        if (recipeName.isEmpty()) { formName.setError("请输入菜名"); formName.requestFocus(); valid = false; }
        boolean hasMainIngredient = false;
        for (Ingredient ingredient : parsedIngredients) if (!ingredient.staple) { hasMainIngredient = true; break; }
        if (!hasMainIngredient) { formMainIngredients.setError("请至少填写一种主要食材"); if (valid) formMainIngredients.requestFocus(); valid = false; }
        if (parsedSteps.isEmpty()) { formSteps.setError("请至少填写一步制作步骤"); if (valid) formSteps.requestFocus(); valid = false; }
        int minutes = positiveIntOrError(formMinutes, 20);
        int servings = positiveIntOrError(formServings, 2);
        if (minutes < 1 || servings < 1) valid = false;
        if (!valid) { toast("请根据字段提示补充菜谱"); return; }
        Recipe recipe = new Recipe(
                formExisting == null ? "" : formExisting.id,
                recipeName,
                valueOr(formCategory, "家常菜"), valueOr(formFlavor, "家常"), valueOr(formDifficulty, "简单"),
                minutes, servings, parsedIngredients, parsedSteps,
                formTips.getText().toString().trim(), true
        );
        repository.saveCustomRecipe(recipe);
        formSaved = true;
        toast(formExisting == null ? "配方已添加" : "配方已更新");
        showRecipes(false);
    }

    private void leaveForm(Recipe existing) {
        if (formSaved || !formDirty()) {
            if (existing == null) showRecipes(false); else showRecipeDetail(existing);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("尚未保存菜谱")
                .setMessage("离开后，本次填写的内容将会丢失。")
                .setNegativeButton("继续编辑", null)
                .setPositiveButton("放弃修改", (dialog, which) -> { if (existing == null) showRecipes(false); else showRecipeDetail(existing); })
                .show();
    }

    private boolean formDirty() { return !formInitialSignature.equals(formSignature()); }

    private String formSignature() {
        if (formName == null) return "";
        return String.join("|", formName.getText().toString(), formCategory.getText().toString(), formFlavor.getText().toString(), formDifficulty.getText().toString(), formMinutes.getText().toString(), formServings.getText().toString(), formMainIngredients.getText().toString(), formStaples.getText().toString(), formSteps.getText().toString(), formTips.getText().toString());
    }

    private String ingredientsForEdit(Recipe recipe) {
        List<String> lines = new ArrayList<>();
        for (Ingredient ingredient : recipe.ingredients) if (!ingredient.staple) lines.add(ingredient.name + " | " + ingredient.amount);
        return String.join("\n", lines);
    }

    private String staplesForEdit(Recipe recipe) {
        List<String> lines = new ArrayList<>();
        for (Ingredient ingredient : recipe.ingredients) if (ingredient.staple) lines.add(ingredient.name + " | " + ingredient.amount);
        return String.join("\n", lines);
    }

    private List<Ingredient> parseIngredients(String mainValue, String stapleValue) {
        List<Ingredient> result = new ArrayList<>();
        parseIngredientLines(result, mainValue, false);
        parseIngredientLines(result, stapleValue, true);
        return result;
    }

    private void parseIngredientLines(List<Ingredient> result, String value, boolean defaultStaple) {
        for (String raw : value.split("\r?\n")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            boolean staple = defaultStaple || line.startsWith("*");
            if (line.startsWith("*")) line = line.substring(1).trim();
            String[] parts = line.split("\\|", 2);
            String name = parts[0].trim();
            if (!name.isEmpty()) result.add(new Ingredient(name, parts.length > 1 && !parts[1].trim().isEmpty() ? parts[1].trim() : "适量", staple));
        }
    }

    private List<String> parseLines(String value) {
        List<String> result = new ArrayList<>();
        for (String line : value.split("\r?\n")) if (!line.trim().isEmpty()) result.add(line.trim());
        return result;
    }

    private EditText labeledInput(LinearLayout parent, String label, String value, String hint) {
        parent.addView(text(label, 13, MUTED, true), matchWrap(4));
        EditText field = input(hint);
        field.setText(value);
        parent.addView(field, matchWrap(10));
        return field;
    }

    private EditText labeledArea(LinearLayout parent, String label, String value, String hint) {
        parent.addView(text(label, 13, MUTED, true), matchWrap(4));
        EditText field = input(hint);
        field.setSingleLine(false);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.setImeOptions(EditorInfo.IME_ACTION_NONE);
        field.setGravity(Gravity.TOP | Gravity.START);
        field.setMinLines(4);
        field.setPadding(dp(14), dp(12), dp(14), dp(12));
        field.setText(value);
        parent.addView(field, matchWrap(10));
        return field;
    }

    private String valueOr(EditText field, String fallback) {
        String value = field.getText().toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private int positiveIntOrError(EditText field, int fallback) {
        String value = field.getText().toString().trim();
        if (value.isEmpty()) { field.setError("请输入数字"); return -1; }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) { field.setError("请输入大于 0 的数字"); return -1; }
            return parsed;
        } catch (Exception ignored) {
            field.setError("请输入有效数字");
            return -1;
        }
    }

    private TextWatcher simpleWatcher(Runnable action) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { action.run(); }
            @Override public void afterTextChanged(Editable s) { }
        };
    }

    private LinearLayout pageBody(int spacingDp) {
        LinearLayout body = vertical(spacingDp);
        body.setPadding(dp(16), dp(18), dp(16), dp(28));
        return body;
    }

    private View bodyPage(LinearLayout body) { return scroll(body); }

    private LinearLayout vertical(int spacingDp) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout card() {
        LinearLayout card = vertical(0);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(roundRect(WHITE, 18, LINE));
        return card;
    }

    private String recipeVisualGroup(Recipe recipe) {
        if (recipe.custom) return "私房";
        String haystack = recipe.name + " " + recipe.category + " " + ingredientSummary(recipe);
        if (containsAny(haystack, "鸡翅", "鸡胸", "鸡肉", "猪", "肉", "牛", "排骨", "羊")) return "肉禽";
        if (containsAny(haystack, "鱼", "虾", "蟹", "贝", "紫菜", "海带")) return "水鲜";
        if (containsAny(haystack, "鸡蛋", "豆腐", "豆制品", "蛋")) return "蛋豆";
        if (containsAny(haystack, "米饭", "小米", "面", "粥", "馒头", "主食", "早餐")) return "谷物";
        if (containsAny(haystack, "汤", "羹", "炖菜")) return "汤羹";
        return "时蔬";
    }

    private List<String> ingredientGroupOrder() {
        return Arrays.asList("肉禽", "水鲜", "蛋豆", "时蔬", "谷物", "其他");
    }

    private String ingredientGroup(String name) {
        String value = name == null ? "" : name;
        if (containsAny(value, "猪", "牛", "羊", "鸡翅", "鸡胸", "鸡肉", "鸭", "鹅", "排骨", "肉")) return "肉禽";
        if (containsAny(value, "鱼", "虾", "蟹", "贝", "紫菜", "海带", "鱿鱼")) return "水鲜";
        if (containsAny(value, "蛋", "豆腐", "豆干", "豆皮", "牛奶", "奶酪")) return "蛋豆";
        if (containsAny(value, "米", "面", "馒头", "饼", "粉", "小米", "燕麦")) return "谷物";
        if (containsAny(value, "西红柿", "番茄", "土豆", "椒", "黄瓜", "萝卜", "茄子", "青菜", "生菜", "西兰花", "菜花", "香菇", "菌", "南瓜", "葱", "姜", "蒜")) return "时蔬";
        return "其他";
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private int toneForGroup(String group) {
        switch (group) {
            case "肉禽": return Color.rgb(157, 80, 69);
            case "水鲜": return Color.rgb(59, 108, 119);
            case "蛋豆": return Color.rgb(171, 126, 53);
            case "时蔬": return Color.rgb(74, 117, 77);
            case "谷物": return Color.rgb(145, 111, 66);
            case "汤羹": return Color.rgb(74, 112, 104);
            case "私房": return Color.rgb(111, 83, 119);
            default: return Color.rgb(103, 105, 97);
        }
    }

    private int softForGroup(String group) {
        switch (group) {
            case "肉禽": return Color.rgb(246, 224, 214);
            case "水鲜": return Color.rgb(219, 235, 236);
            case "蛋豆": return Color.rgb(246, 235, 201);
            case "时蔬": return Color.rgb(224, 237, 217);
            case "谷物": return Color.rgb(239, 225, 200);
            case "汤羹": return Color.rgb(222, 235, 227);
            case "私房": return Color.rgb(235, 224, 237);
            default: return Color.rgb(235, 232, 222);
        }
    }

    private String groupSymbol(String group) {
        switch (group) {
            case "肉禽": return "炙";
            case "水鲜": return "鲜";
            case "蛋豆": return "豆";
            case "时蔬": return "蔬";
            case "谷物": return "禾";
            case "汤羹": return "羹";
            case "私房": return "藏";
            default: return "味";
        }
    }

    private TextView pill(String label, int tone, int soft) {
        TextView badge = text(label, 11, tone, true);
        badge.setGravity(Gravity.CENTER);
        badge.setSingleLine(true);
        badge.setPadding(dp(10), 0, dp(10), 0);
        badge.setBackground(roundRect(Color.argb(210, Color.red(soft), Color.green(soft), Color.blue(soft)), 14, mixColor(tone, soft, 0.72f)));
        return badge;
    }

    private CheckBox ingredientCheck(String name, boolean checked, int tone, int soft) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(name);
        checkBox.setTextSize(14);
        checkBox.setGravity(Gravity.CENTER_VERTICAL);
        checkBox.setMinHeight(dp(48));
        checkBox.setMaxLines(1);
        checkBox.setEllipsize(android.text.TextUtils.TruncateAt.END);
        checkBox.setPadding(dp(10), 0, dp(7), 0);
        checkBox.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        checkBox.setTextDirection(View.TEXT_DIRECTION_LTR);
        checkBox.setButtonTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{tone, mixColor(tone, LINE, 0.62f)}));
        checkBox.setChecked(checked);
        checkBox.setContentDescription((checked ? "已选择" : "选择") + name);
        styleIngredientCheck(checkBox, checked, tone, soft);
        return checkBox;
    }

    private void styleIngredientCheck(CheckBox checkBox, boolean checked, int tone, int soft) {
        checkBox.setTextColor(checked ? mixColor(INK, tone, 0.28f) : INK);
        checkBox.setTypeface(Typeface.DEFAULT, checked ? Typeface.BOLD : Typeface.NORMAL);
        int fill = checked ? mixColor(soft, WHITE, 0.20f) : Color.argb(188, 255, 253, 248);
        int stroke = checked ? tone : mixColor(tone, LINE, 0.74f);
        checkBox.setBackground(ripple(fill, 13, stroke));
        checkBox.setContentDescription((checked ? "已选择" : "选择") + checkBox.getText());
    }

    private int mixColor(int first, int second, float secondAmount) {
        float amount = Math.max(0f, Math.min(1f, secondAmount));
        int red = Math.round(Color.red(first) * (1f - amount) + Color.red(second) * amount);
        int green = Math.round(Color.green(first) * (1f - amount) + Color.green(second) * amount);
        int blue = Math.round(Color.blue(first) * (1f - amount) + Color.blue(second) * amount);
        return Color.rgb(red, green, blue);
    }

    private Drawable gradientRoundRect(int start, int end, int radiusDp, int stroke) {
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        shape.setCornerRadius(dp(radiusDp));
        if (stroke != Color.TRANSPARENT) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private final class BotanicalMotifView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        BotanicalMotifView(Activity context, int tone) {
            super(context);
            paint.setColor(Color.argb(30, Color.red(tone), Color.green(tone), Color.blue(tone)));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float right = getWidth() - dp(10);
            float top = dp(12);
            path.reset();
            path.moveTo(right, top);
            path.cubicTo(right - dp(4), top + dp(18), right - dp(23), top + dp(31), right - dp(38), top + dp(50));
            path.cubicTo(right - dp(47), top + dp(62), right - dp(50), top + dp(76), right - dp(48), top + dp(91));
            canvas.drawPath(path, paint);
            canvas.drawOval(right - dp(24), top + dp(17), right - dp(8), top + dp(29), paint);
            canvas.drawOval(right - dp(43), top + dp(39), right - dp(25), top + dp(51), paint);
            canvas.drawOval(right - dp(57), top + dp(61), right - dp(40), top + dp(73), paint);
            canvas.drawCircle(dp(22), getHeight() - dp(15), dp(10), paint);
            canvas.drawCircle(dp(22), getHeight() - dp(15), dp(4), paint);
        }
    }
    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(0, 1.15f);
        view.setIncludeFontPadding(true);
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
        field.setHintTextColor(Color.rgb(130, 132, 126));
        field.setSingleLine(true);
        field.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        field.setMinHeight(dp(52));
        field.setPadding(dp(14), dp(4), dp(14), dp(4));
        field.setBackground(roundRect(Color.argb(248, 255, 253, 248), 12, LINE));
        return field;
    }

    private Button primaryButton(String label) {
        Button button = textButton(label, true);
        button.setTextColor(WHITE);
        button.setMinHeight(dp(48));
        button.setBackground(ripple(CINNABAR, 12, Color.TRANSPARENT));
        return button;
    }

    private Button outlineButton(String label) {
        Button button = textButton(label, false);
        button.setTextColor(JADE_DARK);
        button.setMinHeight(dp(48));
        button.setBackground(ripple(WHITE, 12, JADE));
        return button;
    }

    private Button textButton(String label, boolean bold) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(14);
        button.setTextColor(INK);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(48));
        button.setPadding(dp(8), dp(4), dp(8), dp(4));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setStateListAnimator(null);
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

    private GradientDrawable gradientRect(int start, int end, int topLeft, int topRight, int bottomRight, int bottomLeft) {
        GradientDrawable shape = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        shape.setCornerRadii(new float[]{dp(topLeft), dp(topLeft), dp(topRight), dp(topRight), dp(bottomRight), dp(bottomRight), dp(bottomLeft), dp(bottomLeft)});
        return shape;
    }

    private void press(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ValueAnimator.areAnimatorsEnabled()) {
            view.animate().scaleX(0.985f).scaleY(0.985f).setDuration(70L).withEndAction(() ->
                    view.animate().scaleX(1f).scaleY(1f).setDuration(150L).start()).start();
        }
    }

    private void pulse(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ValueAnimator.areAnimatorsEnabled()) {
            view.animate().scaleX(1.08f).scaleY(1.08f).setDuration(90L).withEndAction(() ->
                    view.animate().scaleX(1f).scaleY(1f).setDuration(160L).start()).start();
        }
    }

    private Drawable ripple(int fill, int radiusDp, int stroke) {
        return new RippleDrawable(ColorStateList.valueOf(Color.argb(36, 37, 53, 47)), roundRect(fill, radiusDp, stroke), null);
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
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        scroll.addView(child, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private LinearLayout.LayoutParams matchWrap(int bottomMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(bottomMarginDp);
        return params;
    }

    private LinearLayout.LayoutParams wrapParams(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private void addSpacer(LinearLayout layout, int widthDp, int heightDp) {
        Space spacer = new Space(this);
        layout.addView(spacer, new LinearLayout.LayoutParams(dp(widthDp), dp(heightDp)));
    }

    private boolean isWide() { return getResources().getConfiguration().screenWidthDp >= 600; }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_SHORT).show(); }

    private void restorePage(Bundle state) {
        currentPage = state.getString("page", "HOME");
        recipeQuery = state.getString("recipeQuery", "");
        favoriteQuery = state.getString("favoriteQuery", "");
        ingredientQuery = state.getString("ingredientQuery", "");
        detailReturnPage = state.getString("detailReturnPage", "RECIPES");
        currentRecipeId = state.getString("recipeId", "");
        if ("RECIPES".equals(currentPage)) showRecipes(false);
        else if ("FAVORITES".equals(currentPage)) showRecipes(true);
        else if ("PICKER".equals(currentPage)) showIngredientPicker();
        else if ("SHOPPING".equals(currentPage)) showShoppingList();
        else if ("DETAIL".equals(currentPage)) {
            Recipe recipe = repository.findById(currentRecipeId);
            if (recipe == null) showHome(); else showRecipeDetail(recipe);
        } else if ("FORM".equals(currentPage)) {
            Recipe recipe = repository.findById(state.getString("formRecipeId", ""));
            showRecipeForm(recipe);
            if (state.containsKey("formName")) {
                formName.setText(state.getString("formName", ""));
                formCategory.setText(state.getString("formCategory", ""));
                formFlavor.setText(state.getString("formFlavor", ""));
                formDifficulty.setText(state.getString("formDifficulty", ""));
                formMinutes.setText(state.getString("formMinutes", "20"));
                formServings.setText(state.getString("formServings", "2"));
                formMainIngredients.setText(state.getString("formMainIngredients", ""));
                formStaples.setText(state.getString("formStaples", ""));
                formSteps.setText(state.getString("formSteps", ""));
                formTips.setText(state.getString("formTips", ""));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("page", currentPage);
        outState.putString("recipeQuery", recipeQuery);
        outState.putString("favoriteQuery", favoriteQuery);
        outState.putString("ingredientQuery", ingredientQuery);
        outState.putString("detailReturnPage", detailReturnPage);
        outState.putString("recipeId", currentRecipeId);
        if (formExisting != null) outState.putString("formRecipeId", formExisting.id);
        if ("FORM".equals(currentPage) && formName != null) {
            outState.putString("formName", formName.getText().toString());
            outState.putString("formCategory", formCategory.getText().toString());
            outState.putString("formFlavor", formFlavor.getText().toString());
            outState.putString("formDifficulty", formDifficulty.getText().toString());
            outState.putString("formMinutes", formMinutes.getText().toString());
            outState.putString("formServings", formServings.getText().toString());
            outState.putString("formMainIngredients", formMainIngredients.getText().toString());
            outState.putString("formStaples", formStaples.getText().toString());
            outState.putString("formSteps", formSteps.getText().toString());
            outState.putString("formTips", formTips.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void handleBack() {
        if (backAction != null) backAction.run();
        else if (!"HOME".equals(currentPage)) showHome();
        else moveTaskToBack(true);
    }

    @SuppressLint("GestureBackNavigation")
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() { handleBack(); }
}
