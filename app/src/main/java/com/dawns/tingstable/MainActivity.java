package com.dawns.tingstable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dawns.tingstable.data.PantryRepository;
import com.dawns.tingstable.data.BackupPayload;
import com.dawns.tingstable.data.GitHubBackupClient;
import com.dawns.tingstable.data.LocalBackupManager;
import com.dawns.tingstable.data.RecipeRepository;
import com.dawns.tingstable.data.SpecialRecipeCatalog;
import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.PantryItem;
import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.RecipeBrowseState;
import com.dawns.tingstable.model.BackNavigationState;
import com.dawns.tingstable.model.SpecialCollection;
import com.dawns.tingstable.model.SpecialRecipe;
import com.dawns.tingstable.model.ThemeMode;
import com.dawns.tingstable.util.MotionSpec;
import com.dawns.tingstable.util.RecipeCategories;
import com.dawns.tingstable.util.RecipeCuisines;
import com.dawns.tingstable.util.RecipeFilters;
import com.dawns.tingstable.util.RecipeMatcher;
import com.dawns.tingstable.util.RemoteImageLoader;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@SuppressLint("GestureBackNavigation")
public class MainActivity extends Activity {
    private static final String UI_PREFS = "ui_preferences";
    private static final String THEME_KEY = "theme_mode";

    private int PAPER;
    private int SURFACE;
    private int INK;
    private int JADE;
    private int ACCENT_TEXT;
    private int JADE_LIGHT;
    private int CINNABAR;
    private int CINNABAR_LIGHT;
    private int GOLD;
    private int GOLD_LIGHT;
    private int MUTED;
    private int LINE;
    private int CONTROL_LINE;
    private int CONTROL_SOFT;
    private int CONTROL_INK;
    private int ON_ACCENT;
    private ThemeMode themeMode = ThemeMode.LIGHT;
    private boolean darkTheme;

    private RecipeRepository repository;
    private PantryRepository pantryRepository;
    private LocalBackupManager localBackupManager;
    private GitHubBackupClient cloudBackupClient;
    private RemoteImageLoader remoteImageLoader;
    private List<SpecialRecipe> yunfengRecipes;
    private FrameLayout root;
    private LinearLayout topBar;
    private LinearLayout bottomNav;
    private FrameLayout content;
    private TextView titleView;
    private ImageButton backButton;
    private ImageButton themeButton;
    private final Button[] navButtons = new Button[5];
    private Runnable backAction;
    private MotionSpec.MotionHandle pageMotion;

    private String currentPage = "HOME";
    private String currentRecipeId = "";
    private final RecipeBrowseState recipeState = new RecipeBrowseState();
    private TextView recipeSummary;
    private ImageButton recipeSearchAction;
    private ImageButton recipeFilterAction;
    private RecyclerView recipeList;
    private FrameLayout recipeResults;
    private View recipeEmpty;
    private RecipeListAdapter recipeListAdapter;
    private int recipeScrollPosition;
    private int recipeScrollOffset;
    private String pantryFilter = "ALL";
    private String detailReturnPage = "RECIPES";
    private String currentSpecialId = "";
    private final BackNavigationState backNavigationState = new BackNavigationState();
    private long lastBackDispatchAt;
    private AlertDialog cloudProgressDialog;

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
    private String formCuisine = RecipeCuisines.HOME_FUSION;
    private Recipe formExisting;
    private String formInitialSignature = "";
    private boolean formSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTheme();
        repository = new RecipeRepository(this);
        pantryRepository = new PantryRepository(this);
        localBackupManager = new LocalBackupManager(this, repository, pantryRepository);
        cloudBackupClient = new GitHubBackupClient(
                BuildConfig.RECIPE_CLOUD_OWNER,
                BuildConfig.RECIPE_CLOUD_REPOSITORY,
                BuildConfig.RECIPE_CLOUD_PROFILE_ID,
                BuildConfig.RECIPE_CLOUD_TOKEN
        );
        remoteImageLoader = new RemoteImageLoader(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        configureSystemBars();
        buildShell();
        showHome();
        if (savedInstanceState != null) restorePage(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0, this::dispatchBack);
        }
    }

    private void configureSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!darkTheme);
            controller.setAppearanceLightNavigationBars(!darkTheme);
        }
    }

    private void loadTheme() {
        SharedPreferences preferences = getSharedPreferences(UI_PREFS, MODE_PRIVATE);
        themeMode = ThemeMode.fromId(preferences.getString(THEME_KEY, ThemeMode.LIGHT.id()));
        darkTheme = themeMode.isDark();
        if (darkTheme) {
            PAPER = Color.rgb(18, 22, 20);
            SURFACE = Color.rgb(28, 34, 30);
            INK = Color.rgb(237, 242, 237);
            JADE = Color.rgb(158, 184, 166);
            ACCENT_TEXT = Color.rgb(211, 225, 214);
            JADE_LIGHT = Color.rgb(45, 58, 49);
            CINNABAR = Color.rgb(210, 155, 146);
            CINNABAR_LIGHT = Color.rgb(70, 48, 46);
            GOLD = Color.rgb(208, 187, 129);
            GOLD_LIGHT = Color.rgb(64, 57, 40);
            MUTED = Color.rgb(175, 187, 178);
            LINE = Color.rgb(53, 65, 57);
            CONTROL_LINE = Color.rgb(77, 95, 83);
            CONTROL_SOFT = Color.rgb(45, 58, 49);
            CONTROL_INK = Color.rgb(211, 225, 214);
            ON_ACCENT = Color.rgb(23, 32, 25);
        } else {
            PAPER = Color.rgb(244, 245, 242);
            SURFACE = Color.rgb(255, 255, 255);
            INK = Color.rgb(32, 42, 36);
            JADE = Color.rgb(96, 123, 108);
            ACCENT_TEXT = Color.rgb(80, 100, 90);
            JADE_LIGHT = Color.rgb(228, 236, 230);
            CINNABAR = Color.rgb(189, 139, 130);
            CINNABAR_LIGHT = Color.rgb(242, 230, 227);
            GOLD = Color.rgb(173, 155, 102);
            GOLD_LIGHT = Color.rgb(240, 235, 221);
            MUTED = Color.rgb(104, 117, 109);
            LINE = Color.rgb(209, 221, 212);
            CONTROL_LINE = Color.rgb(194, 208, 198);
            CONTROL_SOFT = Color.rgb(228, 236, 230);
            CONTROL_INK = Color.rgb(80, 100, 90);
            ON_ACCENT = Color.rgb(255, 255, 255);
        }
    }

    private void toggleTheme() {
        themeMode = themeMode.toggle();
        getSharedPreferences(UI_PREFS, MODE_PRIVATE)
                .edit()
                .putString(THEME_KEY, themeMode.id())
                .apply();
        recreate();
    }

    private void updateThemeButton() {
        if (themeButton == null) return;
        String next = darkTheme ? "切换到淡色皮肤" : "切换到黑夜皮肤";
        themeButton.setContentDescription(next);
        themeButton.setTooltipText(next);
        themeButton.setImageTintList(ColorStateList.valueOf(CONTROL_INK));
        themeButton.setBackground(ripple(CONTROL_SOFT, 14, CONTROL_LINE));
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
        topBar.setBackground(roundRect(PAPER, 0, CONTROL_LINE));
        topBar.setMinimumHeight(dp(62));

        backButton = iconButton(R.drawable.ic_action_chevron_right, "返回", false);
        backButton.setRotation(180f);
        backButton.setImageTintList(ColorStateList.valueOf(CONTROL_INK));
        backButton.setVisibility(View.GONE);
        topBar.addView(backButton, new LinearLayout.LayoutParams(dp(48), dp(48)));

        titleView = text(getString(R.string.app_name), 20, INK, true);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        titleView.setMaxLines(2);
        ViewCompat.setAccessibilityHeading(titleView, true);
        topBar.addView(titleView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        themeButton = iconButton(R.drawable.ic_action_theme, "切换到黑夜皮肤", false);
        themeButton.setOnClickListener(v -> toggleTheme());
        topBar.addView(themeButton, new LinearLayout.LayoutParams(dp(48), dp(48)));
        updateThemeButton();
        shell.addView(topBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content = new FrameLayout(this);
        content.setBackgroundColor(PAPER);
        shell.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        bottomNav = new LinearLayout(this);
        bottomNav.setGravity(Gravity.CENTER);
        bottomNav.setBackground(roundRect(SURFACE, 0, CONTROL_LINE));
        bottomNav.setElevation(dp(8));
        addNav(0, "首页", R.drawable.ic_nav_home, this::showHome);
        addNav(1, "菜谱", R.drawable.ic_nav_recipes, () -> showRecipes(RecipeBrowseState.SCOPE_ALL));
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
        if (pageMotion != null && pageMotion.isRunning()) pageMotion.cancel();
        View outgoing = content.getChildCount() == 0 ? null : content.getChildAt(content.getChildCount() - 1);
        String previousPage = currentPage;
        currentPage = page;
        backNavigationState.reset();
        titleView.setText(title);
        backAction = onBack;
        backButton.setVisibility(onBack == null ? View.GONE : View.VISIBLE);
        backButton.setOnClickListener(v -> { if (backAction != null) backAction.run(); });
        bottomNav.setVisibility(showNavigation ? View.VISIBLE : View.GONE);
        setNavSelection(showNavigation ? navIndex(page) : -1);
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        int width = screenWidthDp >= 600 ? dp(Math.min(820, Math.max(360, screenWidthDp - 40))) : ViewGroup.LayoutParams.MATCH_PARENT;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER_HORIZONTAL);
        content.addView(view, params);
        if (outgoing == null || outgoing == view) {
            MotionSpec.enter(view, dp(8));
        } else {
            Runnable removeOutgoing = () -> {
                if (outgoing.getParent() == content) content.removeView(outgoing);
            };
            int previousIndex = navIndex(previousPage);
            int nextIndex = navIndex(page);
            if (previousIndex >= 0 && nextIndex >= 0) {
                pageMotion = MotionSpec.siblingTransition(outgoing, view,
                        nextIndex >= previousIndex, dp(14), removeOutgoing);
            } else if (isBackTransition(previousPage, page)) {
                pageMotion = MotionSpec.backTransition(outgoing, view, dp(20), removeOutgoing);
            } else {
                pageMotion = MotionSpec.forwardTransition(outgoing, view, dp(20), removeOutgoing);
            }
        }
        applySafeInsets();
    }

    private boolean isBackTransition(String from, String to) {
        if ("DETAIL".equals(from)) return !"FORM".equals(to);
        if ("FORM".equals(from)) return true;
        if ("PANTRY_MATCHES".equals(from)) return "PANTRY".equals(to);
        return "SPECIAL_DETAIL".equals(from) && "SPECIALS".equals(to);
    }

    private void setNavSelection(int selected) {
        for (int i = 0; i < navButtons.length; i++) {
            Button button = navButtons[i];
            if (button == null) continue;
            boolean active = i == selected;
            boolean changedToActive = active && !button.isSelected();
            button.setTextColor(active ? CONTROL_INK : MUTED);
            button.setCompoundDrawableTintList(ColorStateList.valueOf(active ? CONTROL_INK : MUTED));
            button.setBackground(ripple(active ? CONTROL_SOFT : Color.TRANSPARENT, 13, Color.TRANSPARENT));
            button.setSelected(active);
            ViewCompat.setStateDescription(button, active ? "当前页面" : "未选择");
            if (changedToActive) MotionSpec.selectionFeedback(button);
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

        LinearLayout hero = vertical();
        hero.setBackground(ripple(SURFACE, 22, CONTROL_LINE));

        LinearLayout heroCopy = vertical();
        heroCopy.setPadding(dp(20), dp(20), dp(20), dp(18));
        TextView eyebrow = text("今日厨房", 12, CONTROL_INK, true);
        eyebrow.setLetterSpacing(0.08f);
        heroCopy.addView(eyebrow);
        TextView signature = text("漂亮嘞女明星～", 11, CINNABAR, true);
        signature.setPadding(0, dp(4), 0, 0);
        heroCopy.addView(signature);
        TextView brand = text(getString(R.string.app_name), 27, INK, true);
        brand.setPadding(0, dp(8), 0, dp(6));
        heroCopy.addView(brand);
        String kitchenState = available == 0
                ? "菜篮还是空的，先添几样喜欢的食材"
                : "菜篮有 " + available + " 种 · 今晚能做 " + canCook + " 道";
        heroCopy.addView(text(kitchenState, 13, MUTED, false));
        TextView prompt = text(matches.isEmpty() ? "去整理菜篮" : "看看今晚推荐", 12, CONTROL_INK, true);
        prompt.setPadding(0, dp(10), 0, 0);
        heroCopy.addView(prompt);
        hero.addView(heroCopy, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        Recipe heroRecipe = matches.isEmpty() ? null : matches.get(0).recipe;
        hero.setContentDescription(heroRecipe == null
                ? "今日厨房，" + kitchenState + "，打开菜篮"
                : "今日厨房，" + kitchenState + "，查看推荐菜谱" + heroRecipe.name);
        hero.setOnClickListener(v -> {
            if (heroRecipe == null) showPantry(); else openRecipeDetail(heroRecipe, "HOME");
        });
        MotionSpec.attachPress(hero);
        body.addView(hero, spaced(16));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);
        stats.addView(statCard("菜篮", available + " 种", available > 0 ? "尚有食材" : "等你添菜"), weighted());
        addGap(stats, 10);
        stats.addView(statCard("可做", canCook + " 道", "按现有食材"), weighted());
        addGap(stats, 10);
        stats.addView(statCard("提醒", low + " 种", "余量不多"), weighted());
        body.addView(stats, spaced(22));

        body.addView(sectionTitle("从这里开始"));
        LinearLayout actionRowOne = new LinearLayout(this);
        actionRowOne.setOrientation(LinearLayout.HORIZONTAL);
        actionRowOne.addView(homeAction("翻菜谱", "按菜系和做法找一餐", R.drawable.ic_home_recipe,
                () -> showRecipes(RecipeBrowseState.SCOPE_ALL)), weighted());
        addGap(actionRowOne, 10);
        actionRowOne.addView(homeAction("打开菜篮", "看看余量与新购食材", R.drawable.ic_home_pantry,
                this::showPantry), weighted());
        body.addView(actionRowOne, spaced(10));
        LinearLayout actionRowTwo = new LinearLayout(this);
        actionRowTwo.setOrientation(LinearLayout.HORIZONTAL);
        actionRowTwo.addView(homeAction("就用现有食材", "匹配现在可以做的菜", R.drawable.ic_home_cook_now,
                this::showPantryMatches), weighted());
        addGap(actionRowTwo, 10);
        actionRowTwo.addView(homeAction("特典菜谱", "云峰特典 · 150 道收藏", R.drawable.ic_home_special,
                this::showSpecials), weighted());
        body.addView(actionRowTwo, spaced(16));

        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        Button favorites = outlineButton("收藏 · " + repository.getFavorites().size());
        favorites.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_favorite, 0, 0, 0);
        favorites.setCompoundDrawablePadding(dp(6));
        favorites.setOnClickListener(v -> showRecipes(RecipeBrowseState.SCOPE_FAVORITES));
        quick.addView(favorites, weighted());
        addGap(quick, 10);
        Button shopping = outlineButton("清单 · " + repository.getShoppingItems().size());
        shopping.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_shopping, 0, 0, 0);
        shopping.setCompoundDrawablePadding(dp(6));
        shopping.setOnClickListener(v -> showShoppingList());
        quick.addView(shopping, weighted());
        body.addView(quick, spaced(12));
        body.addView(cloudBackupRow(), spaced(4));
        body.addView(appearanceRow(), spaced(8));

        setPage("HOME", "今日厨房", null, scroll(body), true);
    }

    private View cloudBackupRow() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(2), dp(2), dp(2));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_action_cloud);
        icon.setImageTintList(ColorStateList.valueOf(JADE));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(icon, new LinearLayout.LayoutParams(dp(28), dp(28)));

        LinearLayout copy = vertical();
        copy.setPadding(dp(10), 0, dp(6), 0);
        copy.addView(text("云端备份", 14, INK, true));
        copy.addView(text(cloudBackupStatus(), 12, MUTED, false));
        row.addView(copy, weighted());

        Button restore = outlineButton("恢复");
        restore.setContentDescription("查看并恢复云端备份");
        restore.setOnClickListener(v -> restoreCloudBackup());
        row.addView(restore, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48)));
        addGap(row, 6);

        Button upload = primaryButton("上传");
        upload.setContentDescription("立即上传个人数据备份");
        upload.setOnClickListener(v -> uploadCloudBackup());
        row.addView(upload, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48)));

        boolean configured = cloudBackupClient != null && cloudBackupClient.isConfigured();
        restore.setEnabled(configured);
        upload.setEnabled(configured);
        if (!configured) {
            restore.setAlpha(0.5f);
            upload.setAlpha(0.5f);
        }
        return row;
    }

    private String cloudBackupStatus() {
        if (cloudBackupClient == null || !cloudBackupClient.isConfigured()) return "云备份暂不可用";
        long uploaded = localBackupManager.lastUploadAt();
        long restored = localBackupManager.lastRestoreAt();
        if (uploaded <= 0L && restored <= 0L) return "尚未上传备份";
        if (uploaded >= restored) return "最近上传 " + formatBackupTime(uploaded);
        return "最近恢复 " + formatBackupTime(restored);
    }

    private void uploadCloudBackup() {
        if (!ensureCloudBackupConfigured()) return;
        try {
            BackupPayload payload = localBackupManager.capture(
                    BuildConfig.RECIPE_CLOUD_PROFILE_ID,
                    BuildConfig.VERSION_NAME,
                    System.currentTimeMillis()
            );
            String raw = payload.toJson().toString();
            showCloudProgress("正在上传", "正在保存个人菜谱数据…");
            cloudBackupClient.upload(raw, new GitHubBackupClient.Callback<Long>() {
                @Override
                public void onSuccess(Long completedAt) {
                    dismissCloudProgress();
                    localBackupManager.markUploaded(completedAt);
                    toast("云端备份已更新");
                    if ("HOME".equals(currentPage)) showHome();
                }

                @Override
                public void onError(String message) {
                    dismissCloudProgress();
                    showCloudError(message);
                }
            });
        } catch (Exception error) {
            dismissCloudProgress();
            showCloudError(error.getMessage());
        }
    }

    private void restoreCloudBackup() {
        if (!ensureCloudBackupConfigured()) return;
        showCloudProgress("正在读取", "正在获取最近的云端备份…");
        cloudBackupClient.download(new GitHubBackupClient.Callback<String>() {
            @Override
            public void onSuccess(String raw) {
                dismissCloudProgress();
                try {
                    BackupPayload payload = localBackupManager.parse(raw, BuildConfig.RECIPE_CLOUD_PROFILE_ID);
                    showRestoreConfirmation(payload);
                } catch (Exception error) {
                    showCloudError(error.getMessage());
                }
            }

            @Override
            public void onError(String message) {
                dismissCloudProgress();
                showCloudError(message);
            }
        });
    }

    private void showRestoreConfirmation(BackupPayload payload) {
        String time = payload.createdAt > 0L ? formatBackupTime(payload.createdAt) : "时间未知";
        dialogBuilder()
                .setTitle("恢复这份备份？")
                .setMessage("备份时间：" + time + "\n" + payload.summary()
                        + "\n\n本机现有的个人菜谱、收藏、菜篮、清单与皮肤设置将被替换；内置菜谱不受影响。")
                .setNegativeButton("取消", null)
                .setPositiveButton("确认恢复", (dialog, which) -> {
                    if (!localBackupManager.restore(payload)) {
                        showCloudError("恢复失败，本机原有数据已尝试保留");
                        return;
                    }
                    localBackupManager.markRestored(System.currentTimeMillis());
                    toast("个人数据已恢复");
                    recreate();
                })
                .show();
    }

    private boolean ensureCloudBackupConfigured() {
        if (cloudBackupClient != null && cloudBackupClient.isConfigured()) return true;
        showCloudError("当前安装包未配置云备份");
        return false;
    }

    private void showCloudProgress(String title, String message) {
        dismissCloudProgress();
        cloudProgressDialog = dialogBuilder()
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .create();
        cloudProgressDialog.show();
    }

    private void dismissCloudProgress() {
        if (cloudProgressDialog != null && cloudProgressDialog.isShowing()) cloudProgressDialog.dismiss();
        cloudProgressDialog = null;
    }

    private void showCloudError(String message) {
        String detail = message == null || message.trim().isEmpty() ? "操作失败，请稍后重试" : message;
        dialogBuilder()
                .setTitle("云端备份未完成")
                .setMessage(detail)
                .setPositiveButton("知道了", null)
                .show();
    }

    private String formatBackupTime(long time) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(time));
    }

    private View appearanceRow() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(2), dp(2), dp(2));
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_action_theme);
        icon.setImageTintList(ColorStateList.valueOf(JADE));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(icon, new LinearLayout.LayoutParams(dp(28), dp(28)));
        LinearLayout copy = vertical();
        copy.setPadding(dp(10), 0, dp(8), 0);
        copy.addView(text("皮肤", 14, INK, true));
        copy.addView(text(themeMode.isDark() ? "黑夜模式" : "淡色模式", 12, MUTED, false));
        row.addView(copy, weighted());
        Button action = outlineButton(themeMode.isDark() ? "换成淡色" : "进入黑夜");
        action.setOnClickListener(v -> toggleTheme());
        row.addView(action, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48)));
        return row;
    }

    private View statCard(String title, String value, String note) {
        LinearLayout box = vertical();
        box.setPadding(dp(13), dp(14), dp(12), dp(13));
        box.setBackground(roundRect(SURFACE, 18, CONTROL_LINE));
        box.addView(text(title, 12, MUTED, false));
        TextView number = text(value, 21, ACCENT_TEXT, true);
        number.setPadding(0, dp(4), 0, dp(3));
        box.addView(number);
        box.addView(text(note, 10, MUTED, false));
        return box;
    }

    private View homeAction(String title, String description, int iconRes, Runnable action) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.TOP | Gravity.START);
        box.setMinimumHeight(dp(132));
        box.setPadding(dp(16), dp(16), dp(14), dp(14));
        box.setBackground(ripple(SURFACE, 18, CONTROL_LINE));
        box.setOnClickListener(v -> action.run());
        MotionSpec.attachPress(box);
        FrameLayout iconFrame = new FrameLayout(this);
        iconFrame.setBackground(roundRect(CONTROL_SOFT, 13, Color.TRANSPARENT));
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(CONTROL_INK));
        icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        iconFrame.addView(icon, new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER));
        box.addView(iconFrame, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout words = vertical();
        words.setPadding(0, dp(13), 0, 0);
        words.addView(text(title, 16, INK, true));
        TextView desc = text(description, 13, MUTED, false);
        desc.setPadding(0, dp(3), 0, 0);
        desc.setMaxLines(2);
        words.addView(desc);
        box.addView(words, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        box.setContentDescription(title + "，" + description);
        return box;
    }

    private void showRecipes(String scope) {
        boolean scopeChanged = !Objects.equals(scope, recipeState.getScope());
        if (scopeChanged) {
            recipeScrollPosition = 0;
            recipeScrollOffset = 0;
        }
        recipeState.setScope(scope);
        LinearLayout page = vertical();
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(10), dp(10), dp(8));

        recipeSummary = text("", 13, MUTED, true);
        recipeSummary.setMaxLines(2);
        recipeSummary.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        header.addView(recipeSummary, weighted());

        recipeSearchAction = iconButton(R.drawable.ic_action_search, "搜索菜谱", false);
        recipeSearchAction.setOnClickListener(v -> {
            MotionSpec.iconNudge(v, dp(3));
            showRecipeSearchSheet();
        });
        header.addView(recipeSearchAction, new LinearLayout.LayoutParams(dp(48), dp(48)));

        recipeFilterAction = iconButton(R.drawable.ic_action_filter, "筛选菜谱", false);
        recipeFilterAction.setOnClickListener(v -> {
            MotionSpec.selectionFeedback(v);
            showRecipeFilterSheet();
        });
        header.addView(recipeFilterAction, new LinearLayout.LayoutParams(dp(48), dp(48)));

        ImageButton add = iconButton(R.drawable.ic_action_add, "新增自定义菜谱", false);
        add.setOnClickListener(v -> showRecipeForm(null));
        header.addView(add, new LinearLayout.LayoutParams(dp(48), dp(48)));
        page.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        recipeResults = new FrameLayout(this);
        recipeList = new RecyclerView(this);
        recipeList.setLayoutManager(new LinearLayoutManager(this));
        recipeList.setClipToPadding(false);
        recipeList.setPadding(dp(16), dp(6), dp(16), dp(20));
        recipeListAdapter = new RecipeListAdapter();
        recipeList.setAdapter(recipeListAdapter);
        if (MotionSpec.enabled()) {
            DefaultItemAnimator animator = new DefaultItemAnimator();
            animator.setAddDuration(MotionSpec.FILTER);
            animator.setRemoveDuration(MotionSpec.FILTER);
            animator.setMoveDuration(MotionSpec.FILTER);
            animator.setChangeDuration(MotionSpec.FILTER);
            recipeList.setItemAnimator(animator);
        } else {
            recipeList.setItemAnimator(null);
        }
        recipeResults.addView(recipeList, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        recipeEmpty = emptyState("没有找到合适的菜谱", "点击这里修改搜索或筛选条件。");
        recipeEmpty.setContentDescription("没有找到合适的菜谱，修改搜索或筛选条件");
        recipeEmpty.setOnClickListener(v -> {
            if (recipeState.hasActiveQuery()) showRecipeSearchSheet(); else showRecipeFilterSheet();
        });
        MotionSpec.attachPress(recipeEmpty);
        recipeEmpty.setVisibility(View.GONE);
        FrameLayout.LayoutParams emptyParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
        );
        emptyParams.setMargins(dp(16), dp(12), dp(16), 0);
        recipeResults.addView(recipeEmpty, emptyParams);
        page.addView(recipeResults, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        setPage("RECIPES", "菜谱", null, page, true);
        renderRecipeResults(false);
        restoreRecipeScrollPosition();
    }

    private void renderRecipeResults(boolean animate) {
        if (recipeResults == null || recipeListAdapter == null) return;
        Runnable update = () -> {
            Set<String> favorites = repository.getFavorites();
            List<Recipe> filtered = RecipeFilters.filter(repository.getAllRecipes(), favorites, recipeState);
            recipeSummary.setText(filtered.size() + " 道 · " + recipeState.compactSummary());
            boolean queryActive = recipeState.hasActiveQuery();
            boolean facetsActive = recipeState.activeFilterCount() > 0;
            styleIconButton(recipeSearchAction, queryActive);
            styleIconButton(recipeFilterAction, facetsActive);
            recipeSearchAction.setContentDescription(queryActive
                    ? "搜索菜谱，当前关键词：" + recipeState.getQuery()
                    : "搜索菜谱");
            recipeSearchAction.setTooltipText(recipeSearchAction.getContentDescription());
            ViewCompat.setStateDescription(recipeSearchAction,
                    queryActive ? "已输入搜索词" : "无搜索词");
            recipeFilterAction.setContentDescription(facetsActive
                    ? "筛选菜谱，已选择 " + recipeState.activeFilterCount() + " 项条件"
                    : "筛选菜谱");
            recipeFilterAction.setTooltipText(recipeFilterAction.getContentDescription());
            ViewCompat.setStateDescription(recipeFilterAction,
                    facetsActive ? "有筛选条件" : "无筛选条件");
            recipeListAdapter.submitRecipes(filtered, favorites);
            recipeList.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
            recipeEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        };
        if (animate) MotionSpec.crossfade(recipeResults, update); else update.run();
    }

    private void showRecipeSearchSheet() {
        LinearLayout body = vertical();
        EditText search = input("搜索菜名、食材、菜系或口味");
        search.setSingleLine(true);
        search.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        search.setText(recipeState.getQuery());
        search.setSelection(search.length());
        body.addView(search, spaced(10));
        body.addView(text("搜索会匹配菜名、口味、菜系、做法和食材。", 12, MUTED, false));

        Dialog dialog = showBottomSheet("搜索菜谱", body, false,
                "清除", () -> {
                    recipeState.setQuery("");
                    resetRecipeScrollToTop();
                    renderRecipeResults(true);
                },
                "查看结果", () -> {
                    recipeState.setQuery(search.getText().toString());
                    resetRecipeScrollToTop();
                    renderRecipeResults(true);
                });
        search.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) return false;
            recipeState.setQuery(search.getText().toString());
            resetRecipeScrollToTop();
            renderRecipeResults(true);
            dialog.dismiss();
            return true;
        });
        search.requestFocus();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void showRecipeFilterSheet() {
        String[] scope = { recipeState.scopeLabel() };
        String[] cuisine = { recipeState.getCuisine() };
        String[] method = { RecipeCategories.ALL.equals(recipeState.getCookingMethod())
                ? "全部做法" : recipeState.getCookingMethod() };

        LinearLayout body = vertical();
        body.addView(sheetLabel("菜谱范围"));
        body.addView(choiceRow(new String[] { "全部菜谱", "我的收藏", "自定义" }, scope), spaced(14));
        body.addView(sheetLabel("菜系"));
        body.addView(choiceRow(RecipeCuisines.all().toArray(new String[0]), cuisine), spaced(14));
        body.addView(sheetLabel("烹饪方式"));
        List<String> methodLabels = new ArrayList<>();
        for (String value : RecipeCategories.all()) {
            methodLabels.add(RecipeCategories.ALL.equals(value) ? "全部做法" : value);
        }
        body.addView(choiceRow(methodLabels.toArray(new String[0]), method));

        ScrollView scroll = scroll(body);
        showBottomSheet("筛选菜谱", scroll, true,
                "重置", () -> {
                    recipeState.setScope(RecipeBrowseState.SCOPE_ALL);
                    recipeState.setCuisine(RecipeCuisines.ALL);
                    recipeState.setCookingMethod(RecipeCategories.ALL);
                    resetRecipeScrollToTop();
                    renderRecipeResults(true);
                },
                "应用", () -> {
                    if ("我的收藏".equals(scope[0])) recipeState.setScope(RecipeBrowseState.SCOPE_FAVORITES);
                    else if ("自定义".equals(scope[0])) recipeState.setScope(RecipeBrowseState.SCOPE_CUSTOM);
                    else recipeState.setScope(RecipeBrowseState.SCOPE_ALL);
                    recipeState.setCuisine(cuisine[0]);
                    recipeState.setCookingMethod("全部做法".equals(method[0])
                            ? RecipeCategories.ALL : method[0]);
                    resetRecipeScrollToTop();
                    renderRecipeResults(true);
                });
    }

    private TextView sheetLabel(String value) {
        TextView label = text(value, 13, INK, true);
        label.setPadding(0, dp(4), 0, dp(7));
        return label;
    }

    private Dialog showBottomSheet(String title, View sheetContent, boolean tall,
                                   String secondaryLabel, Runnable secondaryAction,
                                   String primaryLabel, Runnable primaryAction) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout sheet = vertical();
        sheet.setPadding(dp(18), dp(10), dp(18), dp(16));
        sheet.setBackground(roundRect(SURFACE, 22, Color.TRANSPARENT));
        sheet.setElevation(dp(18));

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleView = text(title, 20, INK, true);
        ViewCompat.setAccessibilityHeading(titleView, true);
        heading.addView(titleView, weighted());
        ImageButton close = iconButton(R.drawable.ic_action_close, "关闭" + title, false);
        close.setOnClickListener(v -> dialog.dismiss());
        heading.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        sheet.addView(heading, spaced(8));

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                tall ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT,
                tall ? 1f : 0f);
        sheet.addView(sheetContent, contentParams);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(12), 0, 0);
        Button secondary = outlineButton(secondaryLabel);
        secondary.setOnClickListener(v -> {
            secondaryAction.run();
            dialog.dismiss();
        });
        actions.addView(secondary, weighted());
        addGap(actions, 10);
        Button primary = primaryButton(primaryLabel);
        primary.setOnClickListener(v -> {
            primaryAction.run();
            dialog.dismiss();
        });
        actions.addView(primary, weighted());
        sheet.addView(actions);

        dialog.setContentView(sheet);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.32f;
            window.setAttributes(attributes);
            int screenWidthDp = getResources().getConfiguration().screenWidthDp;
            int screenHeightDp = getResources().getConfiguration().screenHeightDp;
            int width = screenWidthDp >= 600 ? dp(Math.min(620, screenWidthDp - 32))
                    : ViewGroup.LayoutParams.MATCH_PARENT;
            int height = tall ? dp(Math.min(620, Math.max(280, Math.round(screenHeightDp * 0.82f))))
                    : ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setLayout(width, height);
            int side = screenWidthDp >= 600 ? 0 : dp(8);
            window.getDecorView().setPadding(side + systemLeftInset, 0,
                    side + systemRightInset, Math.max(dp(8), systemBottomInset));
        }
        MotionSpec.bottomSheetEnter(sheet, dp(24));
        return dialog;
    }

    private View recipeCard(Recipe recipe, boolean isFavorite) {
        int tone = categoryTone(RecipeCategories.categoryFor(recipe));
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.HORIZONTAL);
        outer.setBackground(ripple(SURFACE, 18, Color.TRANSPARENT));
        outer.setClipToOutline(true);
        outer.setMinimumHeight(dp(86));
        outer.setContentDescription("查看菜谱：" + recipe.name);
        outer.setOnClickListener(v -> openRecipeDetail(recipe, "RECIPES"));
        MotionSpec.attachPress(outer);

        View accent = new View(this);
        accent.setBackgroundColor(tone);
        outer.addView(accent, new LinearLayout.LayoutParams(dp(5), ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout box = vertical();
        box.setPadding(dp(15), dp(14), dp(13), dp(14));
        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.addView(text(recipe.name, 18, INK, true), weighted());
        Button favorite = textButton(isFavorite ? "★" : "☆", false);
        favorite.setTextSize(22);
        favorite.setTextColor(isFavorite ? GOLD : MUTED);
        favorite.setContentDescription(isFavorite ? "取消收藏" : "收藏");
        favorite.setOnClickListener(v -> {
            v.setEnabled(false);
            repository.toggleFavorite(recipe.id);
            boolean nowFavorite = repository.isFavorite(recipe.id);
            favorite.setText(nowFavorite ? "★" : "☆");
            favorite.setTextColor(nowFavorite ? GOLD : MUTED);
            favorite.setContentDescription(nowFavorite ? "取消收藏" : "收藏");
            MotionSpec.favorite(v);
            long delay = MotionSpec.enabled() ? MotionSpec.FAVORITE : 0L;
            v.postDelayed(() -> {
                if ("RECIPES".equals(currentPage)) {
                    boolean removeCard = RecipeBrowseState.SCOPE_FAVORITES.equals(recipeState.getScope());
                    renderRecipeResults(removeCard);
                }
            }, delay);
        });
        top.addView(favorite, new LinearLayout.LayoutParams(dp(48), dp(48)));
        box.addView(top);

        String meta = RecipeCuisines.normalize(recipe.cuisine) + "  ·  "
                + RecipeCategories.categoryFor(recipe) + "  ·  "
                + recipe.minutes + " 分钟  ·  " + recipe.difficulty;
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
        if (recipe.ingredients == null) return "食材待补充";
        for (Ingredient ingredient : recipe.ingredients) {
            if (!ingredient.staple) names.add(ingredient.name);
            if (names.size() == 4) break;
        }
        return names.isEmpty() ? "食材待补充" : String.join("、", names);
    }

    private void openRecipeDetail(Recipe recipe, String returnPage) {
        if (recipe == null) return;
        if ("RECIPES".equals(returnPage)) saveRecipeScrollPosition();
        currentRecipeId = recipe.id;
        detailReturnPage = returnPage;
        showRecipeDetail(recipe);
    }

    private void saveRecipeScrollPosition() {
        if (recipeList == null || !(recipeList.getLayoutManager() instanceof LinearLayoutManager)) return;
        LinearLayoutManager layout = (LinearLayoutManager) recipeList.getLayoutManager();
        int position = layout.findFirstVisibleItemPosition();
        if (position < 0) return;
        View first = layout.findViewByPosition(position);
        recipeScrollPosition = position;
        recipeScrollOffset = first == null ? 0 : first.getTop() - recipeList.getPaddingTop();
    }

    private void restoreRecipeScrollPosition() {
        if (recipeList == null || recipeListAdapter == null || recipeListAdapter.getItemCount() == 0) return;
        int position = Math.min(recipeScrollPosition, recipeListAdapter.getItemCount() - 1);
        recipeList.post(() -> {
            if (recipeList == null || !(recipeList.getLayoutManager() instanceof LinearLayoutManager)) return;
            ((LinearLayoutManager) recipeList.getLayoutManager())
                    .scrollToPositionWithOffset(position, recipeScrollOffset);
        });
    }

    private void resetRecipeScrollToTop() {
        recipeScrollPosition = 0;
        recipeScrollOffset = 0;
        if (recipeList != null) recipeList.scrollToPosition(0);
    }

    private void showRecipeDetail(Recipe recipe) {
        LinearLayout body = pageBody();
        int tone = categoryTone(RecipeCategories.categoryFor(recipe));

        LinearLayout hero = vertical();
        hero.setPadding(dp(20), dp(20), dp(20), dp(18));
        hero.setBackground(roundRect(tone, 22, Color.TRANSPARENT));
        hero.addView(text(RecipeCategories.categoryFor(recipe), 12, Color.argb(220, 255, 255, 255), true));
        TextView name = text(recipe.name, 28, ON_ACCENT, true);
        name.setPadding(0, dp(8), 0, dp(8));
        hero.addView(name);
        hero.addView(text(recipe.flavor + "  ·  " + recipe.minutes + " 分钟  ·  " + recipe.servings + " 人份", 14, ON_ACCENT, false));
        body.addView(hero, spaced(15));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button favorite = outlineButton(repository.isFavorite(recipe.id) ? "★ 已收藏" : "☆ 收藏");
        favorite.setContentDescription(repository.isFavorite(recipe.id) ? "取消收藏" : "收藏");
        favorite.setOnClickListener(v -> {
            repository.toggleFavorite(recipe.id);
            boolean nowFavorite = repository.isFavorite(recipe.id);
            favorite.setText(nowFavorite ? "★ 已收藏" : "☆ 收藏");
            favorite.setContentDescription(nowFavorite ? "取消收藏" : "收藏");
            MotionSpec.favorite(favorite);
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
        row.setBackground(ripple(SURFACE, 15, Color.TRANSPARENT));
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
        TextView number = text(String.valueOf(index), 13, ON_ACCENT, true);
        number.setGravity(Gravity.CENTER);
        number.setBackground(roundRect(JADE, 14, Color.TRANSPARENT));
        row.addView(number, new LinearLayout.LayoutParams(dp(28), dp(28)));
        TextView words = text(step, 15, INK, false);
        words.setPadding(dp(12), dp(3), 0, dp(4));
        row.addView(words, weighted());
        return row;
    }

    private void returnFromRecipeDetail() {
        if ("HOME".equals(detailReturnPage)) showHome();
        else if ("PANTRY_MATCHES".equals(detailReturnPage)) showPantryMatches();
        else if ("SPECIALS".equals(detailReturnPage)) showSpecials();
        else showRecipes(recipeState.getScope());
    }

    private void confirmDelete(Recipe recipe) {
        dialogBuilder()
                .setTitle("删除菜谱")
                .setMessage("确定删除“" + recipe.name + "”吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    repository.deleteCustomRecipe(recipe.id);
                    toast("已删除");
                    showRecipes(RecipeBrowseState.SCOPE_CUSTOM);
                }).show();
    }

    private void showRecipesForIngredient(String ingredient) {
        recipeState.setQuery(ingredient);
        recipeState.setScope(RecipeBrowseState.SCOPE_ALL);
        recipeState.setCuisine(RecipeCuisines.ALL);
        recipeState.setCookingMethod(RecipeCategories.ALL);
        showRecipes(RecipeBrowseState.SCOPE_ALL);
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
        shelf.setBackground(roundRect(SURFACE, 19, Color.TRANSPARENT));
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

        AlertDialog dialog = dialogBuilder()
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
                dialogBuilder()
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
        box.setBackground(ripple(SURFACE, 18, Color.TRANSPARENT));
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
        currentSpecialId = "";
        LinearLayout body = pageBody();
        body.addView(text("特典菜谱", 25, INK, true));
        body.addView(text("云峰已收录 150 道，另一席静候成篇。", 13, MUTED, false), spaced(14));
        for (SpecialCollection collection : specialCollections()) {
            LinearLayout card = vertical();
            card.setPadding(dp(20), dp(20), dp(20), dp(18));
            int tone = "ting".equals(collection.id) ? JADE : CINNABAR;
            card.setBackground(ripple(SURFACE, 21, CONTROL_LINE));
            TextView mark = text(collection.subtitle, 11, tone, true);
            mark.setLetterSpacing(0.1f);
            card.addView(mark);
            TextView title = text(collection.title, 23, INK, true);
            title.setPadding(0, dp(10), 0, dp(7));
            card.addView(title);
            card.addView(text(collection.quote, 14, MUTED, false));
            if (!collection.recipes.isEmpty()) {
                TextView count = text(collection.recipes.size() + " 道收藏", 12, MUTED, true);
                count.setPadding(0, dp(10), 0, 0);
                card.addView(count);
            }
            TextView enter = text("入席  ›", 13, tone, true);
            enter.setPadding(0, dp(14), 0, 0);
            card.addView(enter);
            card.setOnClickListener(v -> showSpecialDetail(collection));
            body.addView(card, spaced(12));
        }
        setPage("SPECIALS", "特典", null, scroll(body), true);
    }

    private void showSpecialDetail(SpecialCollection collection) {
        currentSpecialId = collection.id;
        LinearLayout page = vertical();
        page.setPadding(dp(16), dp(16), dp(16), 0);
        LinearLayout hero = vertical();
        hero.setPadding(dp(20), dp(20), dp(20), dp(18));
        hero.setBackground(roundRect(SURFACE, 20, CONTROL_LINE));
        int tone = "ting".equals(collection.id) ? JADE : CINNABAR;
        hero.addView(text(collection.subtitle, 12, tone, true));
        TextView title = text(collection.title, 25, INK, true);
        title.setPadding(0, dp(10), 0, dp(7));
        hero.addView(title);
        hero.addView(text(collection.quote, 14, MUTED, false));
        if (!collection.recipes.isEmpty()) {
            TextView count = text(collection.recipes.size() + " 道收藏 · 按收藏顺序排列", 12, CONTROL_INK, true);
            count.setPadding(0, dp(10), 0, 0);
            hero.addView(count);
        }
        page.addView(hero, spaced(16));

        if (collection.recipes.isEmpty()) {
            page.addView(emptyState("尚待入席", ""), spaced(22));
            setPage("SPECIAL_DETAIL", collection.title, this::showSpecials, scroll(page), false);
            return;
        }

        RecyclerView list = new RecyclerView(this);
        int columns = getResources().getConfiguration().screenWidthDp >= 600 ? 2 : 1;
        list.setLayoutManager(new GridLayoutManager(this, columns));
        list.setClipToPadding(false);
        list.setPadding(0, 0, 0, dp(20));
        list.setAdapter(new SpecialRecipeAdapter(collection.recipes));
        list.setItemAnimator(null);
        page.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        ));
        setPage("SPECIAL_DETAIL", collection.title, this::showSpecials, page, false);
    }

    private List<SpecialCollection> specialCollections() {
        return Arrays.asList(
                new SpecialCollection("ting", "婷馔清欢", "四时特典", "人间有味，四时清欢。", Collections.emptyList()),
                new SpecialCollection("feng", "楚天云岫 · 云峰特典", "云峰特典", "楚水有味，云峰藏香。", yunfengRecipes())
        );
    }

    private List<SpecialRecipe> yunfengRecipes() {
        if (yunfengRecipes == null) {
            yunfengRecipes = SpecialRecipeCatalog.load(this, R.raw.yunfeng_special);
        }
        return yunfengRecipes;
    }

    private SpecialCollection findSpecialCollection(String id) {
        for (SpecialCollection collection : specialCollections()) {
            if (collection.id.equals(id)) return collection;
        }
        return null;
    }

    private void openSpecialRecipe(SpecialRecipe recipe) {
        if (!SpecialRecipe.isAllowedRecipeUrl(recipe.sourceUrl)) {
            toast("原菜谱链接不可用");
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(recipe.sourceUrl)));
        } catch (ActivityNotFoundException ignored) {
            toast("没有可打开网页的应用");
        }
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
                row.setBackground(roundRect(SURFACE, 15, Color.TRANSPARENT));
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
            clear.setOnClickListener(v -> dialogBuilder()
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
        AlertDialog dialog = dialogBuilder()
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
        dialogBuilder()
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
        AlertDialog dialog = dialogBuilder()
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
        formCuisine = existing == null ? RecipeCuisines.HOME_FUSION : RecipeCuisines.normalize(existing.cuisine);
        LinearLayout body = pageBody();
        body.addView(text(existing == null ? "新建菜谱" : "编辑菜谱", 25, INK, true));
        body.addView(text("每行一项，名称与用量用“|”分开。", 13, MUTED, false), spaced(12));

        formName = input("例如：番茄炒鸡蛋");
        formName.setText(existing == null ? "" : existing.name);
        body.addView(labeled("菜名", formName), spaced(8));

        body.addView(text("菜系", 12, MUTED, true));
        String[] cuisineHolder = {formCuisine};
        View cuisineChoices = choiceRow(RecipeCuisines.editable().toArray(new String[0]), cuisineHolder);
        body.addView(cuisineChoices, spaced(10));

        body.addView(text("烹饪方式", 12, MUTED, true));
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
            formCuisine = cuisineHolder[0];
            formCategory = categoryHolder[0];
            saveForm();
        });
        body.addView(save, spaced(18));
        formInitialSignature = formSignature(cuisineHolder[0], categoryHolder[0]);
        setPage("FORM", existing == null ? "新建菜谱" : "编辑菜谱",
                () -> leaveForm(cuisineHolder[0], categoryHolder[0]), scroll(body), false);
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
                formCuisine,
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

    private void leaveForm(String cuisine, String category) {
        if (formSaved || formInitialSignature.equals(formSignature(cuisine, category))) {
            if (formExisting != null) showRecipeDetail(repository.findById(formExisting.id));
            else showRecipes(RecipeBrowseState.SCOPE_ALL);
            return;
        }
        dialogBuilder()
                .setTitle("放弃未保存内容？")
                .setMessage("返回后，本次修改不会保留。")
                .setNegativeButton("继续编辑", null)
                .setPositiveButton("放弃", (d, w) -> {
                    if (formExisting != null) showRecipeDetail(repository.findById(formExisting.id));
                    else showRecipes(RecipeBrowseState.SCOPE_ALL);
                }).show();
    }

    private String formSignature(String cuisine, String category) {
        if (formName == null) return "";
        return cuisine + "|" + category + "|" + formName.getText() + "|" + formFlavor.getText() + "|" + formDifficulty.getText()
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
        if (RecipeCategories.SOUP.equals(category) || RecipeCategories.STEAM.equals(category)
                || "水鲜".equals(category) || "时蔬".equals(category)) return JADE;
        if (RecipeCategories.STIR_FRY.equals(category) || RecipeCategories.STEW.equals(category)
                || RecipeCategories.GRILL.equals(category) || "肉禽".equals(category)
                || "蛋豆".equals(category)) return CINNABAR;
        if (RecipeCategories.COLD.equals(category) || RecipeCategories.AIR_FRYER.equals(category)
                || RecipeCategories.STAPLE.equals(category) || RecipeCategories.BAKING.equals(category)
                || "水果".equals(category) || "谷物".equals(category) || "调料".equals(category)) return GOLD;
        return MUTED;
    }

    private int softTone(int tone) { return mixColor(tone, SURFACE, 0.82f); }

    private int statusTone(String status) {
        if (PantryItem.STATUS_EMPTY.equals(status)) return MUTED;
        if (PantryItem.STATUS_LOW.equals(status)) return CINNABAR;
        return JADE;
    }

    private int statusSoft(String status) {
        if (PantryItem.STATUS_EMPTY.equals(status)) return mixColor(SURFACE, MUTED, 0.12f);
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

    private AlertDialog.Builder dialogBuilder() {
        int style = darkTheme
                ? android.R.style.Theme_Material_Dialog_Alert
                : android.R.style.Theme_Material_Light_Dialog_Alert;
        return new AlertDialog.Builder(this, style);
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
        field.setHintTextColor(MUTED);
        field.setSingleLine(false);
        field.setMinHeight(dp(48));
        field.setPadding(dp(13), dp(11), dp(13), dp(11));
        field.setBackground(roundRect(SURFACE, 14, LINE));
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
        button.setTextColor(ON_ACCENT);
        button.setTextSize(14);
        button.setPadding(dp(14), dp(9), dp(14), dp(9));
        button.setBackground(ripple(JADE, 14, Color.TRANSPARENT));
        return button;
    }

    private Button outlineButton(String label) {
        Button button = textButton(label, true);
        button.setTextColor(CONTROL_INK);
        button.setTextSize(13);
        button.setPadding(dp(12), dp(9), dp(12), dp(9));
        button.setBackground(ripple(SURFACE, 14, CONTROL_LINE));
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
        button.setMinHeight(dp(48));
        button.setMinWidth(0);
        button.setPadding(dp(10), dp(7), dp(10), dp(7));
        button.setBackgroundColor(Color.TRANSPARENT);
        MotionSpec.attachPress(button);
        return button;
    }

    private ImageButton iconButton(int iconRes, String description, boolean selected) {
        ImageButton button = new ImageButton(this);
        button.setImageResource(iconRes);
        button.setScaleType(ImageView.ScaleType.CENTER);
        button.setPadding(dp(12), dp(12), dp(12), dp(12));
        button.setMinimumWidth(dp(48));
        button.setMinimumHeight(dp(48));
        button.setContentDescription(description);
        button.setTooltipText(description);
        styleIconButton(button, selected);
        MotionSpec.attachPress(button);
        return button;
    }

    private void styleIconButton(ImageButton button, boolean selected) {
        if (button == null) return;
        button.setImageTintList(ColorStateList.valueOf(selected ? CONTROL_INK : MUTED));
        button.setBackground(ripple(selected ? CONTROL_SOFT : SURFACE, 14, CONTROL_LINE));
        button.setSelected(selected);
    }

    private Button filterChip(String label, boolean selected, Runnable action) {
        Button button = textButton(label, selected);
        button.setTextSize(12);
        button.setOnClickListener(v -> action.run());
        styleFilterChip(button, selected);
        button.setContentDescription((selected ? "已选择：" : "选择：") + label);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(48));
        params.rightMargin = dp(7);
        button.setLayoutParams(params);
        return button;
    }

    private void styleFilterChip(Button button, boolean selected) {
        button.setTypeface(Typeface.create("sans", selected ? Typeface.BOLD : Typeface.NORMAL));
        button.setTextColor(selected ? CONTROL_INK : MUTED);
        button.setBackground(ripple(selected ? CONTROL_SOFT : SURFACE, 14, selected ? JADE : CONTROL_LINE));
        button.setContentDescription((selected ? "已选择：" : "选择：") + button.getText());
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
        box.setBackground(roundRect(SURFACE, 19, Color.TRANSPARENT));
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

    private final class SpecialRecipeAdapter extends RecyclerView.Adapter<SpecialRecipeViewHolder> {
        private final List<SpecialRecipe> items;

        SpecialRecipeAdapter(List<SpecialRecipe> items) {
            this.items = new ArrayList<>(items);
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return stableRecipeId("special-" + items.get(position).id);
        }

        @Override
        public SpecialRecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout container = new FrameLayout(MainActivity.this);
            int side = getResources().getConfiguration().screenWidthDp >= 600 ? dp(6) : 0;
            container.setPadding(side, 0, side, dp(12));
            container.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            LinearLayout card = vertical();
            card.setBackground(ripple(SURFACE, 18, CONTROL_LINE));
            card.setClipToOutline(true);
            MotionSpec.attachPress(card);

            ImageView cover = new ImageView(MainActivity.this);
            cover.setBackgroundColor(CONTROL_SOFT);
            cover.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            int coverHeight = getResources().getConfiguration().screenWidthDp >= 600 ? dp(180) : dp(168);
            card.addView(cover, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    coverHeight
            ));

            LinearLayout copy = vertical();
            copy.setPadding(dp(16), dp(14), dp(16), dp(15));
            TextView order = text("", 11, CINNABAR, true);
            TextView title = text("", 18, INK, true);
            title.setMaxLines(4);
            title.setPadding(0, dp(6), 0, dp(8));
            TextView source = text("下厨房 · 查看原菜谱  ›", 12, CONTROL_INK, true);
            copy.addView(order);
            copy.addView(title);
            copy.addView(source);
            card.addView(copy);
            container.addView(card, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return new SpecialRecipeViewHolder(container, card, cover, order, title);
        }

        @Override
        public void onBindViewHolder(SpecialRecipeViewHolder holder, int position) {
            SpecialRecipe recipe = items.get(position);
            remoteImageLoader.clear(holder.cover);
            holder.cover.setImageResource(R.drawable.ic_nav_special);
            holder.cover.setImageTintList(ColorStateList.valueOf(CONTROL_INK));
            holder.cover.setScaleType(ImageView.ScaleType.CENTER);
            int padding = dp(56);
            holder.cover.setPadding(padding, padding, padding, padding);
            holder.order.setText(String.format(java.util.Locale.ROOT, "第 %03d 道", position + 1));
            holder.title.setText(recipe.title);
            holder.card.setContentDescription("第" + (position + 1) + "道，" + recipe.title + "，打开下厨房原菜谱");
            holder.card.setOnClickListener(v -> openSpecialRecipe(recipe));
            remoteImageLoader.load(holder.cover, recipe.coverUrl);
        }

        @Override
        public void onViewRecycled(SpecialRecipeViewHolder holder) {
            remoteImageLoader.clear(holder.cover);
            holder.cover.setImageDrawable(null);
            holder.card.setOnClickListener(null);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static final class SpecialRecipeViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout card;
        final ImageView cover;
        final TextView order;
        final TextView title;

        SpecialRecipeViewHolder(FrameLayout container, LinearLayout card, ImageView cover,
                                TextView order, TextView title) {
            super(container);
            this.card = card;
            this.cover = cover;
            this.order = order;
            this.title = title;
        }
    }

    private final class RecipeListAdapter extends RecyclerView.Adapter<RecipeViewHolder> {
        private List<RecipeListItem> items = Collections.emptyList();

        RecipeListAdapter() {
            setHasStableIds(true);
        }

        void submitRecipes(List<Recipe> recipes, Set<String> favorites) {
            List<RecipeListItem> next = new ArrayList<>();
            for (Recipe recipe : recipes) {
                next.add(new RecipeListItem(recipe, favorites.contains(recipe.id)));
            }
            List<RecipeListItem> previous = items;
            DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override public int getOldListSize() { return previous.size(); }
                @Override public int getNewListSize() { return next.size(); }

                @Override
                public boolean areItemsTheSame(int oldPosition, int newPosition) {
                    return Objects.equals(
                            previous.get(oldPosition).recipe.id,
                            next.get(newPosition).recipe.id
                    );
                }

                @Override
                public boolean areContentsTheSame(int oldPosition, int newPosition) {
                    return sameRecipeContent(previous.get(oldPosition), next.get(newPosition));
                }
            });
            items = next;
            diff.dispatchUpdatesTo(this);
        }

        @Override
        public long getItemId(int position) {
            return stableRecipeId(items.get(position).recipe.id);
        }

        @Override
        public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout container = new FrameLayout(MainActivity.this);
            container.setPadding(0, 0, 0, dp(10));
            container.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return new RecipeViewHolder(container);
        }

        @Override
        public void onBindViewHolder(RecipeViewHolder holder, int position) {
            RecipeListItem item = items.get(position);
            holder.container.removeAllViews();
            holder.container.addView(recipeCard(item.recipe, item.favorite), new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }

        @Override
        public void onViewRecycled(RecipeViewHolder holder) {
            holder.container.removeAllViews();
        }

        @Override public int getItemCount() { return items.size(); }
    }

    private boolean sameRecipeContent(RecipeListItem left, RecipeListItem right) {
        Recipe first = left.recipe;
        Recipe second = right.recipe;
        return left.favorite == right.favorite
                && Objects.equals(first.name, second.name)
                && Objects.equals(RecipeCuisines.normalize(first.cuisine), RecipeCuisines.normalize(second.cuisine))
                && Objects.equals(RecipeCategories.categoryFor(first), RecipeCategories.categoryFor(second))
                && Objects.equals(first.difficulty, second.difficulty)
                && first.minutes == second.minutes
                && Objects.equals(ingredientSummary(first), ingredientSummary(second));
    }

    private long stableRecipeId(String id) {
        long hash = 1469598103934665603L;
        String value = safe(id);
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 1099511628211L;
        }
        return hash;
    }

    private static final class RecipeListItem {
        final Recipe recipe;
        final boolean favorite;

        RecipeListItem(Recipe recipe, boolean favorite) {
            this.recipe = recipe;
            this.favorite = favorite;
        }
    }

    private static final class RecipeViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout container;

        RecipeViewHolder(FrameLayout container) {
            super(container);
            this.container = container;
        }
    }

    private void restorePage(Bundle state) {
        recipeState.setQuery(state.getString("recipeQuery", ""));
        recipeState.setScope(state.getString("recipeScope", RecipeBrowseState.SCOPE_ALL));
        recipeState.setCookingMethod(state.getString("recipeCategory", RecipeCategories.ALL));
        recipeState.setCuisine(state.getString("recipeCuisine", RecipeCuisines.ALL));
        recipeState.setDimension(state.getString("recipeDimension", RecipeBrowseState.DIMENSION_CUISINE));
        pantryFilter = state.getString("pantryFilter", "ALL");
        currentRecipeId = state.getString("recipeId", "");
        detailReturnPage = state.getString("detailReturnPage", "RECIPES");
        currentSpecialId = state.getString("specialId", "");
        recipeScrollPosition = state.getInt("recipeScrollPosition", 0);
        recipeScrollOffset = state.getInt("recipeScrollOffset", 0);
        String page = state.getString("page", "HOME");
        if ("RECIPES".equals(page)) showRecipes(recipeState.getScope());
        else if ("PANTRY".equals(page)) showPantry();
        else if ("PANTRY_MATCHES".equals(page)) showPantryMatches();
        else if ("SPECIALS".equals(page)) showSpecials();
        else if ("SPECIAL_DETAIL".equals(page)) {
            SpecialCollection collection = findSpecialCollection(currentSpecialId);
            if (collection == null) showSpecials(); else showSpecialDetail(collection);
        }
        else if ("SHOPPING".equals(page)) showShoppingList();
        else if ("DETAIL".equals(page)) {
            Recipe recipe = repository.findById(currentRecipeId);
            if (recipe != null) showRecipeDetail(recipe); else showRecipes(RecipeBrowseState.SCOPE_ALL);
        } else if ("FORM".equals(page)) {
            Recipe recipe = repository.findById(currentRecipeId);
            showRecipeForm(recipe);
        } else showHome();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if ("RECIPES".equals(currentPage)) saveRecipeScrollPosition();
        outState.putString("page", currentPage);
        outState.putString("recipeId", currentRecipeId);
        outState.putString("recipeQuery", recipeState.getQuery());
        outState.putString("recipeScope", recipeState.getScope());
        outState.putString("recipeCategory", recipeState.getCookingMethod());
        outState.putString("recipeCuisine", recipeState.getCuisine());
        outState.putString("recipeDimension", recipeState.getDimension());
        outState.putString("pantryFilter", pantryFilter);
        outState.putString("detailReturnPage", detailReturnPage);
        outState.putString("specialId", currentSpecialId);
        outState.putInt("recipeScrollPosition", recipeScrollPosition);
        outState.putInt("recipeScrollOffset", recipeScrollOffset);
    }

    private void handleBack() {
        if (backAction != null) {
            backNavigationState.reset();
            backAction.run();
            return;
        }
        if (!"HOME".equals(currentPage)) {
            backNavigationState.reset();
            showHome();
            return;
        }
        if (backNavigationState.shouldConfirmExit(SystemClock.uptimeMillis())) {
            dialogBuilder()
                    .setTitle("退出应用？")
                    .setMessage("当前内容已保存在设备中。")
                    .setNegativeButton("继续使用", null)
                    .setPositiveButton("退出", (dialog, which) -> finish())
                    .show();
        } else {
            toast("再按一次返回键，确认退出");
        }
    }

    private void dispatchBack() {
        long now = SystemClock.uptimeMillis();
        if (now - lastBackDispatchAt < 150L) return;
        lastBackDispatchAt = now;
        handleBack();
    }

    @Override
    public void onBackPressed() {
        dispatchBack();
    }

    @Override
    protected void onDestroy() {
        dismissCloudProgress();
        if (cloudBackupClient != null) cloudBackupClient.shutdown();
        if (remoteImageLoader != null) remoteImageLoader.close();
        super.onDestroy();
    }
}
