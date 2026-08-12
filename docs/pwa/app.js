(function startApp() {
  "use strict";

  const core = window.LazyChefCore;
  const CONFIG = {
    profile: "tings",
    repo: "DawnsTing-Tings-Table",
    pageUrl: "https://zalan-dunk.github.io/DawnsTing-Tings-Table/",
    cloudOwner: "ZAlan-dunk",
    cloudRepository: "Dawnsdew-Recipe-Cloud",
    cloudStatePath: "collections/state.json",
    storageKey: "lazy-sheep-chef:pwa:tings:v1",
    themeKey: "lazy-sheep-chef:pwa:tings:theme",
    tokenKey: "lazy-sheep-chef:pwa:tings:github-token"
  };

  const icons = {
    back: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M15 18l-6-6 6-6"/></svg>',
    home: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 10.5 12 4l8 6.5V20H4z"/><path d="M9 20v-6h6v6"/></svg>',
    recipes: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 4h12v16H6z"/><path d="M9 8h6M9 12h6M9 16h4"/></svg>',
    pantry: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 7h14l-1 13H6z"/><path d="M8 7a4 4 0 0 1 8 0M9 11h6M9 15h4"/></svg>',
    special: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m12 3 2.5 5 5.5.8-4 3.9.9 5.5L12 15.6 7.1 18.2l.9-5.5-4-3.9L9.5 8z"/><path d="M6 21h12"/></svg>',
    shopping: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16v13H4z"/><path d="M8 7a4 4 0 0 1 8 0M8 11h8M8 15h5"/></svg>',
    collections: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 4h5v16H4zM10.5 4h5v16h-5zM17 5l3.5-1 3.5 14-3.5 1z"/><path d="M3 21h18"/></svg>',
    search: '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="6"/><path d="m16 16 4 4"/></svg>',
    filter: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 6h16M7 12h10M10 18h4"/></svg>',
    category: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 5h6v6H4zM14 5h6v6h-6zM4 15h6v5H4zM14 15h6v5h-6z"/></svg>',
    sort: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 6h11M8 12h8M8 18h5M4 4v16"/></svg>',
    add: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>',
    cloud: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18h10a4 4 0 0 0 .4-8A6 6 0 0 0 6 9a4.5 4.5 0 0 0 1 9z"/><path d="M12 10v6M9.5 13l2.5-3 2.5 3"/></svg>',
    moon: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 15.5A8 8 0 0 1 8.5 4 8.5 8.5 0 1 0 20 15.5z"/></svg>',
    sun: '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>',
    install: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3v12M8 11l4 4 4-4M5 20h14"/></svg>',
    close: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18"/></svg>',
    favorite: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m12 4 2.5 5 5.5.8-4 3.9.9 5.5L12 16.6 7.1 19.2l.9-5.5-4-3.9L9.5 9z"/></svg>',
    export: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 15V3M8 7l4-4 4 4M5 11v9h14v-9"/></svg>',
    import: '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3v12M8 11l4 4 4-4M5 20h14"/></svg>',
    key: '<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="8" cy="15" r="4"/><path d="m11 12 8-8M16 7l2 2M14 9l2 2"/></svg>'
  };

  const builtInFallback = [
    core.normalizeRecipe({ id: "builtin-tomato-egg", name: "西红柿炒鸡蛋", category: "炒菜", cuisine: "家常融合", flavor: "咸鲜微甜", difficulty: "简单", minutes: 15, servings: 2, ingredients: [{ name: "西红柿", amount: "2个" }, { name: "鸡蛋", amount: "3个" }], steps: ["西红柿切块，鸡蛋打散。", "分别炒熟后合炒调味。"], custom: false }, false),
    core.normalizeRecipe({ id: "builtin-potato", name: "酸辣土豆丝", category: "炒菜", cuisine: "川菜", flavor: "酸辣", difficulty: "简单", minutes: 20, servings: 2, ingredients: [{ name: "土豆", amount: "2个" }, { name: "青椒", amount: "1个" }], steps: ["土豆切丝洗去淀粉。", "大火快炒并调入醋。"], custom: false }, false)
  ];

  let data = core.normalizeData(loadJson(CONFIG.storageKey), CONFIG.profile);
  let builtIns = builtInFallback;
  let yunfeng = [];
  let route = { name: "home" };
  let deferredInstallPrompt = null;
  let toastTimer = null;
  let exitBackAt = 0;
  let allowRootExit = false;
  const standalone = matchMedia("(display-mode: standalone)").matches || Boolean(window.navigator.standalone);
  let filters = { query: "", scope: "all", cuisine: "all", category: "all", categoryMode: false, habit: true };

  const main = document.getElementById("app-main");
  const title = document.getElementById("page-title");
  const kicker = document.getElementById("page-kicker");
  const backButton = document.getElementById("back-button");
  const themeButton = document.getElementById("theme-button");
  const installButton = document.getElementById("install-button");
  const bottomNav = document.getElementById("bottom-nav");
  const modalRoot = document.getElementById("modal-root");
  const toastNode = document.getElementById("toast");
  const importFile = document.getElementById("import-file");

  init().catch((error) => {
    main.innerHTML = `<section class="empty-state"><h2>页面暂时无法启动</h2><p>${escapeHtml(error.message || "请刷新后重试")}</p></section>`;
  });

  async function init() {
    applyTheme(localStorage.getItem(CONFIG.themeKey) || (matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light"));
    backButton.innerHTML = icons.back;
    installButton.innerHTML = icons.install;
    themeButton.addEventListener("click", toggleTheme);
    installButton.addEventListener("click", installApp);
    installButton.hidden = standalone;
    backButton.addEventListener("click", () => history.back());
    importFile.addEventListener("change", importBackupFile);
    window.addEventListener("popstate", (event) => {
      if (allowRootExit) return;
      if (standalone && event.state && event.state.root && route.name === "home") {
        history.pushState({ route: { name: "home" }, guard: true }, "", location.href);
        const current = Date.now();
        if (current - exitBackAt < 1800) {
          confirmAction("退出懒羊羊当大厨~？", "确认后将返回 iOS 的上一浏览位置。", () => {
            allowRootExit = true;
            history.go(-2);
          });
        } else {
          exitBackAt = current;
          showToast("再返回一次可退出");
        }
        return;
      }
      route = event.state && event.state.route ? event.state.route : { name: "home" };
      render();
    });
    window.addEventListener("online", render);
    window.addEventListener("offline", render);
    window.addEventListener("beforeinstallprompt", (event) => {
      event.preventDefault();
      deferredInstallPrompt = event;
      installButton.hidden = false;
    });
    window.addEventListener("appinstalled", () => { installButton.hidden = true; showToast("已添加到桌面"); });

    const [recipeResult, specialResult] = await Promise.allSettled([
      fetch("./data/built-in-recipes.json").then(requireJson),
      fetch("./data/yunfeng-special.json").then(requireJson)
    ]);
    if (recipeResult.status === "fulfilled") {
      builtIns = (recipeResult.value.recipes || []).map((item) => core.normalizeRecipe(item, false));
    }
    if (specialResult.status === "fulfilled") yunfeng = specialResult.value.recipes || [];

    if (history.state && history.state.route) route = history.state.route;
    else history.replaceState({ route: { name: "home" }, root: standalone }, "", location.href);
    if (standalone && !(history.state && history.state.guard)) {
      history.replaceState({ route: { name: "home" }, root: true }, "", location.href);
      history.pushState({ route: { name: "home" }, guard: true }, "", location.href);
      route = { name: "home" };
    }
    render();
    registerServiceWorker();
  }

  function loadJson(key) {
    try { return JSON.parse(localStorage.getItem(key) || "null"); } catch (_) { return null; }
  }

  function saveData() {
    localStorage.setItem(CONFIG.storageKey, JSON.stringify(data));
  }

  function requireJson(response) {
    if (!response.ok) throw new Error(`资源加载失败：${response.status}`);
    return response.json();
  }

  function escapeHtml(value) {
    return String(value == null ? "" : value).replace(/[&<>'"]/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" }[char]));
  }

  function attr(value) { return escapeHtml(value); }

  function applyTheme(theme) {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem(CONFIG.themeKey, theme);
    themeButton.innerHTML = theme === "dark" ? icons.sun : icons.moon;
    themeButton.setAttribute("aria-label", theme === "dark" ? "切换浅色皮肤" : "切换夜间皮肤");
  }

  function toggleTheme() {
    applyTheme(document.documentElement.dataset.theme === "dark" ? "light" : "dark");
  }

  function navigate(name, params, replace) {
    if (name === "recipe" && params && params.id) {
      data.usage = core.recordOpen(data.usage, params.id);
      saveData();
    }
    route = { name, ...(params || {}) };
    const state = { route };
    if (replace) history.replaceState(state, "", location.href); else history.pushState(state, "", location.href);
    render();
    window.scrollTo({ top: 0, behavior: matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth" });
  }

  function render() {
    const topLevel = ["home", "recipes", "pantry", "specials", "shopping"].includes(route.name);
    backButton.hidden = topLevel;
    bottomNav.hidden = !topLevel;
    if (route.name === "home") renderHome();
    else if (route.name === "recipes") renderRecipes();
    else if (route.name === "recipe") renderRecipeDetail(route.id, route.collectionId);
    else if (route.name === "recipeForm") renderRecipeForm(route.id, route.collectionId);
    else if (route.name === "pantry") renderPantry();
    else if (route.name === "matches") renderMatches();
    else if (route.name === "specials") renderSpecials();
    else if (route.name === "specialDetail") renderSpecialDetail(route.id);
    else if (route.name === "shopping") renderShopping();
    else if (route.name === "collections") renderCollections();
    else if (route.name === "collection") renderCollection(route.id);
    else renderHome();
    renderNav(topLevel ? route.name : "");
    document.title = `${title.textContent} · 懒羊羊当大厨~`;
  }

  function setHeader(value, subtitle) {
    title.textContent = value;
    kicker.textContent = subtitle || "漂亮嘞女明星～";
  }

  function shell(content) {
    main.innerHTML = `${navigator.onLine ? "" : '<div class="offline-banner">当前离线，已缓存内容和本地数据仍可使用。</div>'}<div class="screen">${content}</div>`;
  }

  function renderNav(active) {
    const items = [
      ["home", "首页", icons.home], ["recipes", "菜谱", icons.recipes], ["pantry", "菜篮", icons.pantry],
      ["specials", "特典", icons.special], ["shopping", "清单", icons.shopping]
    ];
    bottomNav.innerHTML = items.map(([name, label, icon]) => `<button class="nav-item" data-nav="${name}" type="button" ${active === name ? 'aria-current="page"' : ""}>${icon}<span>${label}</span></button>`).join("");
    bottomNav.querySelectorAll("[data-nav]").forEach((button) => button.addEventListener("click", () => navigate(button.dataset.nav)));
  }

  function visibleCollectionList() { return core.visibleCollections(data.cloudState, data.unlockedSpecials); }
  function collectionRecipes() { return visibleCollectionList().flatMap((item) => item.recipes); }
  function allRecipes(includeCollections) { return [...builtIns, ...data.customRecipes, ...(includeCollections === false ? [] : collectionRecipes())]; }
  function findRecipe(id, collectionId) {
    if (collectionId) {
      const collection = visibleCollectionList().find((item) => item.id === collectionId);
      return collection && collection.recipes.find((item) => item.id === id);
    }
    return allRecipes().find((item) => item.id === id);
  }

  function renderHome() {
    setHeader("懒羊羊当大厨~", core.VERSION);
    const visibleCollections = visibleCollectionList();
    const pantryReady = data.pantry.filter((item) => item.status !== "用完").length;
    shell(`
      <section class="hero">
        <div>
          <div class="hero-eyebrow">漂亮嘞女明星～</div>
          <h1>今天也让厨房轻松一点。</h1>
          <p>菜谱、菜篮和采购清单都保存在这台设备，常用内容可以离线打开。</p>
        </div>
        <div class="status-row" aria-label="厨房摘要">
          <span><strong>${allRecipes().length}</strong> 道菜谱</span>
          <span><strong>${pantryReady}</strong> 种可用食材</span>
          <span><strong>${data.shopping.length}</strong> 项待采购</span>
        </div>
      </section>
      <section class="section">
        <div class="section-heading"><h2>常用入口</h2></div>
        <div class="action-grid">
          ${actionCard("recipes", icons.recipes, "翻看菜谱", "搜索、筛选、分类与习惯排序")}
          ${actionCard("pantry", icons.pantry, "我的菜篮", "记录余量并匹配可以做的菜")}
          ${actionCard("matches", icons.search, "现在能做", "按当前菜篮计算可做与接近可做")}
          ${actionCard("specials", icons.special, "特典菜谱", "婷馔清欢与 150 道云峰收藏")}
          ${actionCard("collections", icons.collections, "个人菜谱集", visibleCollections.length ? `已有 ${visibleCollections.length} 个可见菜谱集` : "创建或从云端获取编号菜谱集", true)}
        </div>
      </section>
      <section class="section">
        <div class="section-heading"><h2>数据</h2></div>
        <div class="summary-strip">
          <button class="summary-item text-button" id="export-data"><strong>${data.customRecipes.length + collectionRecipes().length}</strong><span>导出个人菜谱</span></button>
          <button class="summary-item text-button" id="import-data"><strong>${data.cloudSyncedAt ? formatDate(data.cloudSyncedAt) : "本机"}</strong><span>导入备份</span></button>
          <button class="summary-item text-button" id="cloud-settings"><strong>${localStorage.getItem(CONFIG.tokenKey) ? "已配置" : "未配置"}</strong><span>云端菜谱集</span></button>
        </div>
      </section>
      <section class="section install-only-browser">
        <div class="notice"><strong>添加到 iPhone 主屏幕</strong><br>在 Safari 点“分享”，再选“添加到主屏幕”，之后可像 App 一样打开。</div>
      </section>`);
    main.querySelectorAll("[data-action-route]").forEach((button) => button.addEventListener("click", () => navigate(button.dataset.actionRoute)));
    document.getElementById("export-data").addEventListener("click", exportBackup);
    document.getElementById("import-data").addEventListener("click", () => importFile.click());
    document.getElementById("cloud-settings").addEventListener("click", showCloudSettings);
  }

  function actionCard(routeName, icon, heading, description, wide) {
    return `<button class="action-card${wide ? " wide" : ""}" type="button" data-action-route="${routeName}"><span class="action-icon">${icon}</span><span><span class="action-title">${heading}</span><span class="action-description">${description}</span></span></button>`;
  }

  function renderRecipes(options) {
    setHeader("菜谱", "结果优先，工具按需展开");
    const source = options && options.recipes ? options.recipes : allRecipes(false);
    let recipes = core.filterRecipes(source, filters, data.favorites);
    recipes = filters.habit ? core.sortByHabit(recipes, data.usage) : recipes.sort((a, b) => a.name.localeCompare(b.name, "zh-CN"));
    const body = filters.categoryMode
      ? core.groupByCuisine(recipes).map(([name, items]) => `<div class="group-heading">${name} · ${items.length}</div>${items.map((item) => recipeCard(item)).join("")}`).join("")
      : recipes.map((item) => recipeCard(item)).join("");
    shell(`
      <div class="screen-heading">
        <div><h1>${options && options.heading ? escapeHtml(options.heading) : "所有菜谱"}</h1><p>${recipes.length} 道结果${filters.query ? ` · “${escapeHtml(filters.query)}”` : ""}</p></div>
        <div class="toolbar">
          <button class="icon-button" id="search-recipes" type="button" aria-label="搜索菜谱">${icons.search}</button>
          <button class="icon-button" id="filter-recipes" type="button" aria-label="筛选菜谱">${icons.filter}</button>
          <button class="icon-button" id="category-recipes" type="button" aria-label="切换分类模式" aria-pressed="${filters.categoryMode}">${icons.category}</button>
          <button class="icon-button" id="add-recipe" type="button" aria-label="新建菜谱">${icons.add}</button>
        </div>
      </div>
      ${recipes.length ? `<div class="recipe-list">${body}</div>` : '<section class="empty-state"><h2>没有符合条件的菜谱</h2><p>打开筛选工具调整条件。</p></section>'}`);
    bindRecipeCards();
    document.getElementById("search-recipes").addEventListener("click", showSearchSheet);
    document.getElementById("filter-recipes").addEventListener("click", showFilterSheet);
    document.getElementById("category-recipes").addEventListener("click", () => { filters.categoryMode = !filters.categoryMode; renderRecipes(options); });
    document.getElementById("add-recipe").addEventListener("click", () => navigate("recipeForm"));
  }

  function recipeCard(recipe, collectionId) {
    const favorite = data.favorites.includes(recipe.id);
    return `<article class="recipe-card" data-recipe-id="${attr(recipe.id)}" ${collectionId ? `data-collection-id="${attr(collectionId)}"` : ""}>
      <div class="recipe-card-main" role="button" tabindex="0" aria-label="打开菜谱 ${attr(recipe.name)}">
        <h2>${escapeHtml(recipe.name)}</h2>
        <div class="meta"><span>${escapeHtml(recipe.cuisine)}</span><span>${escapeHtml(recipe.category)}</span><span>${recipe.minutes || "-"} 分钟</span><span>${escapeHtml(recipe.difficulty)}</span></div>
      </div>
      <button class="favorite-button" type="button" aria-label="${favorite ? "取消收藏" : "收藏"} ${attr(recipe.name)}" aria-pressed="${favorite}">${icons.favorite}</button>
    </article>`;
  }

  function bindRecipeCards() {
    main.querySelectorAll(".recipe-card").forEach((card) => {
      const open = () => navigate("recipe", { id: card.dataset.recipeId, collectionId: card.dataset.collectionId || "" });
      card.querySelector(".recipe-card-main").addEventListener("click", open);
      card.querySelector(".recipe-card-main").addEventListener("keydown", (event) => { if (event.key === "Enter" || event.key === " ") { event.preventDefault(); open(); } });
      card.querySelector(".favorite-button").addEventListener("click", () => {
        const id = card.dataset.recipeId;
        data.favorites = data.favorites.includes(id) ? data.favorites.filter((item) => item !== id) : [...data.favorites, id];
        saveData(); render();
      });
    });
  }

  function renderRecipeDetail(id, collectionId) {
    const recipe = findRecipe(id, collectionId);
    if (!recipe) { setHeader("菜谱", "内容不存在"); shell('<section class="empty-state"><h2>没有找到这道菜</h2><p>它可能已从菜谱集中移除。</p></section>'); return; }
    setHeader(recipe.name, `${recipe.cuisine} · ${recipe.category}`);
    shell(`
      <section class="detail-hero"><h1>${escapeHtml(recipe.name)}</h1><div class="meta"><span>${recipe.minutes || "-"} 分钟</span><span>${recipe.servings} 人份</span><span>${escapeHtml(recipe.flavor)}</span><span>${escapeHtml(recipe.difficulty)}</span></div></section>
      <section class="detail-section"><h2>食材</h2><ul class="ingredient-list">${recipe.ingredients.map((item) => `<li><button type="button" data-ingredient="${attr(item.name)}">${escapeHtml(item.name)}</button>${item.amount ? ` · ${escapeHtml(item.amount)}` : ""}</li>`).join("") || "<li>尚未记录</li>"}</ul></section>
      <section class="detail-section"><h2>步骤</h2><ol class="step-list">${recipe.steps.map((step) => `<li>${escapeHtml(step)}</li>`).join("") || "<li>尚未记录</li>"}</ol></section>
      ${recipe.tips ? `<section class="detail-section"><h2>小贴士</h2><div class="notice">${escapeHtml(recipe.tips)}</div></section>` : ""}
      <section class="detail-section button-row">
        <button class="secondary-button" id="add-ingredients" type="button">缺少食材加入清单</button>
        ${recipe.custom ? '<button class="secondary-button" id="edit-recipe" type="button">编辑菜谱</button>' : ""}
      </section>`);
    main.querySelectorAll("[data-ingredient]").forEach((button) => button.addEventListener("click", () => { filters.query = button.dataset.ingredient; navigate("recipes"); }));
    document.getElementById("add-ingredients").addEventListener("click", () => {
      const pantry = new Set(data.pantry.filter((item) => item.status !== "用完").map((item) => canonical(item.name)));
      const missing = recipe.ingredients.filter((item) => !item.staple && !pantry.has(canonical(item.name))).map((item) => item.name);
      data.shopping = [...new Set([...data.shopping, ...missing])]; saveData(); showToast(missing.length ? "缺少食材已加入清单" : "菜篮食材已经齐全");
    });
    const edit = document.getElementById("edit-recipe");
    if (edit) edit.addEventListener("click", () => navigate("recipeForm", { id: recipe.id, collectionId: collectionId || "" }));
  }

  function renderRecipeForm(id, collectionId) {
    const existing = id ? findRecipe(id, collectionId) : null;
    setHeader(existing ? "编辑菜谱" : "新建菜谱", collectionId || "保存在本机");
    const targetCollections = visibleCollectionList().filter((item) => item.editable);
    shell(`
      <form class="form" id="recipe-form">
        <div class="field"><label for="recipe-name">菜名</label><input id="recipe-name" required maxlength="60" value="${attr(existing && existing.name)}"></div>
        <div class="two-column">
          <div class="field"><label for="recipe-cuisine">菜系</label><select id="recipe-cuisine">${core.CUISINES.map((value) => `<option ${existing && existing.cuisine === value ? "selected" : ""}>${value}</option>`).join("")}</select></div>
          <div class="field"><label for="recipe-category">做法</label><select id="recipe-category">${core.CATEGORIES.map((value) => `<option ${existing && existing.category === value ? "selected" : ""}>${value}</option>`).join("")}</select></div>
        </div>
        <div class="two-column"><div class="field"><label for="recipe-minutes">分钟</label><input id="recipe-minutes" type="number" min="0" max="1440" value="${existing ? existing.minutes : 20}"></div><div class="field"><label for="recipe-servings">人份</label><input id="recipe-servings" type="number" min="1" max="99" value="${existing ? existing.servings : 2}"></div></div>
        <div class="field"><label for="recipe-flavor">口味</label><input id="recipe-flavor" maxlength="30" value="${attr(existing && existing.flavor)}"></div>
        <div class="field"><label for="recipe-ingredients">食材</label><textarea id="recipe-ingredients" placeholder="每行一个，例如：西红柿 | 2个">${escapeHtml(existing ? existing.ingredients.map((item) => `${item.name}${item.amount ? ` | ${item.amount}` : ""}${item.staple ? " | 常备" : ""}`).join("\n") : "")}</textarea></div>
        <div class="field"><label for="recipe-steps">步骤</label><textarea id="recipe-steps" placeholder="每行一个步骤">${escapeHtml(existing ? existing.steps.join("\n") : "")}</textarea></div>
        <div class="field"><label for="recipe-tips">小贴士</label><textarea id="recipe-tips">${escapeHtml(existing && existing.tips)}</textarea></div>
        ${!collectionId && targetCollections.length ? `<div class="field"><label for="recipe-target">保存位置</label><select id="recipe-target"><option value="">仅保存在本机</option>${targetCollections.map((item) => `<option value="${attr(item.id)}">${escapeHtml(item.id)} · ${escapeHtml(item.name)}</option>`).join("")}</select></div>` : ""}
        <div class="button-row"><button class="primary-button" type="submit">保存菜谱</button>${existing ? '<button class="danger-button" id="delete-recipe" type="button">删除</button>' : ""}</div>
      </form>`);
    document.getElementById("recipe-form").addEventListener("submit", (event) => {
      event.preventDefault();
      const ingredients = document.getElementById("recipe-ingredients").value.split(/\r?\n/).map((line) => line.trim()).filter(Boolean).map((line) => { const [name, amount, marker] = line.split("|").map((item) => item.trim()); return { name, amount: amount || "", staple: marker === "常备" }; });
      const recipe = core.normalizeRecipe({
        ...(existing || {}), id: existing ? existing.id : core.uuid("custom"), name: document.getElementById("recipe-name").value,
        cuisine: document.getElementById("recipe-cuisine").value, category: document.getElementById("recipe-category").value,
        minutes: document.getElementById("recipe-minutes").value, servings: document.getElementById("recipe-servings").value,
        flavor: document.getElementById("recipe-flavor").value, difficulty: existing ? existing.difficulty : "简单",
        ingredients, steps: document.getElementById("recipe-steps").value.split(/\r?\n/), tips: document.getElementById("recipe-tips").value, custom: true
      }, true);
      const targetId = collectionId || (document.getElementById("recipe-target") && document.getElementById("recipe-target").value);
      saveRecipeToTarget(recipe, targetId); showToast("菜谱已保存"); history.back();
    });
    const remove = document.getElementById("delete-recipe");
    if (remove) remove.addEventListener("click", () => confirmAction("删除这道菜谱？", "删除后可通过此前导出的备份恢复。", () => { deleteRecipe(existing.id, collectionId); history.back(); }));
  }

  function saveRecipeToTarget(recipe, collectionId) {
    if (collectionId) {
      const collection = data.cloudState.collections[collectionId];
      if (!collection) return;
      collection.recipes = [...collection.recipes.filter((item) => item.id !== recipe.id), recipe];
      collection.updatedAt = Date.now();
      data.dirtyCollections = [...new Set([...data.dirtyCollections, collectionId])];
    } else data.customRecipes = [...data.customRecipes.filter((item) => item.id !== recipe.id), recipe];
    saveData();
  }

  function deleteRecipe(id, collectionId) {
    if (collectionId && data.cloudState.collections[collectionId]) {
      data.cloudState.collections[collectionId].recipes = data.cloudState.collections[collectionId].recipes.filter((item) => item.id !== id);
      data.cloudState.collections[collectionId].updatedAt = Date.now();
      data.dirtyCollections = [...new Set([...data.dirtyCollections, collectionId])];
    } else data.customRecipes = data.customRecipes.filter((item) => item.id !== id);
    data.favorites = data.favorites.filter((item) => item !== id); saveData();
  }

  function showSearchSheet() {
    showModal("搜索菜谱", `<form class="form" id="search-form"><div class="field"><label for="search-query">菜名或食材</label><input id="search-query" type="search" value="${attr(filters.query)}" autofocus></div><div class="button-row"><button class="primary-button" type="submit">查看结果</button><button class="secondary-button" id="clear-search" type="button">清除</button></div></form>`, (sheet) => {
      sheet.querySelector("#search-form").addEventListener("submit", (event) => { event.preventDefault(); filters.query = sheet.querySelector("#search-query").value.trim(); closeModal(); render(); });
      sheet.querySelector("#clear-search").addEventListener("click", () => { filters.query = ""; closeModal(); render(); });
    });
  }

  function showFilterSheet() {
    showModal("筛选菜谱", `<form class="form" id="filter-form">
      <div class="field"><label for="filter-scope">范围</label><select id="filter-scope"><option value="all">全部菜谱</option><option value="favorites">我的收藏</option><option value="custom">自定义</option></select></div>
      <div class="field"><label for="filter-cuisine">菜系</label><select id="filter-cuisine"><option value="all">全部菜系</option>${core.CUISINES.map((value) => `<option>${value}</option>`).join("")}</select></div>
      <div class="field"><label for="filter-category">做法</label><select id="filter-category"><option value="all">全部做法</option>${core.CATEGORIES.map((value) => `<option>${value}</option>`).join("")}</select></div>
      <div class="field"><label for="filter-sort">排序</label><select id="filter-sort"><option value="habit">按使用习惯</option><option value="name">按菜名</option></select></div>
      <div class="button-row"><button class="primary-button" type="submit">应用筛选</button><button class="secondary-button" id="reset-filter" type="button">重置</button></div></form>`, (sheet) => {
        sheet.querySelector("#filter-scope").value = filters.scope; sheet.querySelector("#filter-cuisine").value = filters.cuisine; sheet.querySelector("#filter-category").value = filters.category; sheet.querySelector("#filter-sort").value = filters.habit ? "habit" : "name";
        sheet.querySelector("#filter-form").addEventListener("submit", (event) => { event.preventDefault(); filters.scope = sheet.querySelector("#filter-scope").value; filters.cuisine = sheet.querySelector("#filter-cuisine").value; filters.category = sheet.querySelector("#filter-category").value; filters.habit = sheet.querySelector("#filter-sort").value === "habit"; closeModal(); render(); });
        sheet.querySelector("#reset-filter").addEventListener("click", () => { filters = { ...filters, scope: "all", cuisine: "all", category: "all", habit: true }; closeModal(); render(); });
      });
  }

  function renderPantry() {
    setHeader("我的菜篮", "本机保存，不上传个人设置");
    shell(`<div class="screen-heading"><div><h1>菜篮</h1><p>${data.pantry.length} 种食材</p></div><div class="toolbar"><button class="icon-button" id="add-pantry" type="button" aria-label="添加食材">${icons.add}</button></div></div>
      ${data.pantry.length ? `<div class="pantry-list">${data.pantry.map((item) => `<article class="pantry-card"><div><strong>${escapeHtml(item.name)}</strong><div class="meta"><span>${escapeHtml(item.category)}</span><span>${escapeHtml((item.quantity + item.unit).trim() || "未记录数量")}</span><span>${escapeHtml(item.status)}</span></div></div><div class="button-row"><button class="text-button" data-edit-pantry="${attr(item.id)}" type="button">编辑</button><button class="text-button" data-delete-pantry="${attr(item.id)}" type="button">删除</button></div></article>`).join("")}</div>` : '<section class="empty-state"><h2>菜篮还是空的</h2><p>添加家里现有的食材，就能计算现在能做什么。</p></section>'}
      <section class="section"><button class="primary-button" id="open-matches" type="button">看看现在能做什么</button></section>`);
    document.getElementById("add-pantry").addEventListener("click", () => showPantryForm());
    document.getElementById("open-matches").addEventListener("click", () => navigate("matches"));
    main.querySelectorAll("[data-edit-pantry]").forEach((button) => button.addEventListener("click", () => showPantryForm(data.pantry.find((item) => item.id === button.dataset.editPantry))));
    main.querySelectorAll("[data-delete-pantry]").forEach((button) => button.addEventListener("click", () => { data.pantry = data.pantry.filter((item) => item.id !== button.dataset.deletePantry); saveData(); render(); }));
  }

  function showPantryForm(existing) {
    showModal(existing ? "编辑食材" : "添加食材", `<form class="form" id="pantry-form"><div class="field"><label for="pantry-name">食材名称</label><input id="pantry-name" required value="${attr(existing && existing.name)}"></div><div class="two-column"><div class="field"><label for="pantry-quantity">数量</label><input id="pantry-quantity" value="${attr(existing && existing.quantity)}"></div><div class="field"><label for="pantry-unit">单位</label><input id="pantry-unit" value="${attr(existing && existing.unit)}"></div></div><div class="field"><label for="pantry-category">分类</label><select id="pantry-category">${["肉禽","水鲜","蛋豆","时蔬","谷物","水果","调料","其他"].map((value) => `<option ${existing && existing.category === value ? "selected" : ""}>${value}</option>`).join("")}</select></div><div class="field"><label for="pantry-status">余量</label><select id="pantry-status">${["充足","不多","用完"].map((value) => `<option ${existing && existing.status === value ? "selected" : ""}>${value}</option>`).join("")}</select></div><button class="primary-button" type="submit">保存食材</button></form>`, (sheet) => {
      sheet.querySelector("#pantry-form").addEventListener("submit", (event) => { event.preventDefault(); const item = { ...(existing || {}), id: existing ? existing.id : core.uuid("pantry"), name: sheet.querySelector("#pantry-name").value.trim(), quantity: sheet.querySelector("#pantry-quantity").value.trim(), unit: sheet.querySelector("#pantry-unit").value.trim(), category: sheet.querySelector("#pantry-category").value, status: sheet.querySelector("#pantry-status").value, purchasedAt: Date.now(), note: "" }; data.pantry = [...data.pantry.filter((value) => value.id !== item.id), item]; saveData(); closeModal(); render(); });
    });
  }

  function canonical(value) { return String(value || "").trim().toLowerCase().replace(/\s+/g, ""); }

  function calculateMatches() {
    const available = new Set(data.pantry.filter((item) => item.status !== "用完").map((item) => canonical(item.name)));
    return allRecipes(false).map((recipe) => {
      const required = recipe.ingredients.filter((item) => !item.staple);
      const missing = required.filter((item) => !available.has(canonical(item.name))).map((item) => item.name);
      const matched = required.length - missing.length;
      const percent = required.length ? Math.round(matched * 100 / required.length) : 0;
      return { recipe, missing, percent, canCook: required.length > 0 && missing.length === 0, almost: matched >= 1 && missing.length <= 3 && percent >= 50 };
    }).filter((item) => item.canCook || item.almost).sort((a, b) => Number(b.canCook) - Number(a.canCook) || a.missing.length - b.missing.length || b.percent - a.percent);
  }

  function renderMatches() {
    setHeader("现在能做", "根据菜篮食材匹配");
    const matches = calculateMatches();
    shell(`<div class="screen-heading"><div><h1>食材匹配</h1><p>${matches.length} 道可做或接近可做</p></div></div>${matches.length ? `<div class="recipe-list">${matches.map((item) => `<article class="recipe-card" data-recipe-id="${attr(item.recipe.id)}"><div class="recipe-card-main" role="button" tabindex="0"><h2>${escapeHtml(item.recipe.name)}</h2><div class="meta"><span>${item.canCook ? "可以开做" : `还差 ${item.missing.length} 种`}</span><span>${item.percent}% 已齐</span></div>${item.missing.length ? `<div class="meta">缺：${item.missing.map(escapeHtml).join("、")}</div>` : ""}</div><button class="favorite-button" type="button" aria-label="加入缺少食材到清单">${icons.shopping}</button></article>`).join("")}</div>` : '<section class="empty-state"><h2>暂时没有匹配结果</h2><p>先在菜篮添加现有食材。</p></section>'}`);
    main.querySelectorAll(".recipe-card").forEach((card, index) => { const match = matches[index]; card.querySelector(".recipe-card-main").addEventListener("click", () => navigate("recipe", { id: match.recipe.id })); card.querySelector(".favorite-button").addEventListener("click", () => { data.shopping = [...new Set([...data.shopping, ...match.missing])]; saveData(); showToast("已加入采购清单"); }); });
  }

  function renderShopping() {
    setHeader("采购清单", "按加入顺序保存");
    shell(`<div class="screen-heading"><div><h1>采购清单</h1><p>${data.shopping.length} 项</p></div><div class="toolbar"><button class="icon-button" id="add-shopping" type="button" aria-label="添加采购项">${icons.add}</button></div></div>${data.shopping.length ? `<div class="shopping-list">${data.shopping.map((item) => `<article class="shopping-item"><strong>${escapeHtml(item)}</strong><div class="button-row"><button class="text-button" data-stock="${attr(item)}" type="button">买到了</button><button class="text-button" data-remove-shopping="${attr(item)}" type="button">移除</button></div></article>`).join("")}</div><section class="section"><button class="danger-button" id="clear-shopping" type="button">清空清单</button></section>` : '<section class="empty-state"><h2>采购清单是空的</h2><p>可以手动添加，或从菜谱和匹配结果加入缺少食材。</p></section>'}`);
    document.getElementById("add-shopping").addEventListener("click", () => showTextPrompt("添加采购项", "食材名称", "", (value) => { data.shopping = [...new Set([...data.shopping, value])]; saveData(); render(); }));
    main.querySelectorAll("[data-remove-shopping]").forEach((button) => button.addEventListener("click", () => { data.shopping = data.shopping.filter((item) => item !== button.dataset.removeShopping); saveData(); render(); }));
    main.querySelectorAll("[data-stock]").forEach((button) => button.addEventListener("click", () => { const name = button.dataset.stock; data.shopping = data.shopping.filter((item) => item !== name); data.pantry.push({ id: core.uuid("pantry"), name, category: "其他", quantity: "", unit: "", status: "充足", purchasedAt: Date.now(), note: "" }); saveData(); render(); }));
    const clear = document.getElementById("clear-shopping"); if (clear) clear.addEventListener("click", () => confirmAction("清空采购清单？", "这会移除当前全部采购项。", () => { data.shopping = []; saveData(); render(); }));
  }

  function renderSpecials() {
    setHeader("特典菜谱", "两席特典，独立于个人菜谱集");
    shell(`<div class="screen-heading"><div><h1>特典菜谱</h1><p>云峰已收录 ${yunfeng.length || 150} 道，另一席静候成篇。</p></div></div><div class="special-list">
      <button class="special-card" data-special="ting" type="button"><span class="special-mark">四时特典</span><h2>婷馔清欢</h2><span>人间有味，四时清欢。</span></button>
      <button class="special-card" data-special="feng" type="button"><span class="special-mark">云峰特典</span><h2>楚天云岫 · 云峰特典</h2><span>楚水有味，云峰藏香。 · ${yunfeng.length || 150} 道收藏</span></button>
    </div>`);
    main.querySelectorAll("[data-special]").forEach((button) => button.addEventListener("click", () => navigate("specialDetail", { id: button.dataset.special })));
  }

  function renderSpecialDetail(id) {
    const isFeng = id === "feng";
    const heading = isFeng ? "楚天云岫 · 云峰特典" : "婷馔清欢";
    setHeader(heading, isFeng ? "按收藏顺序排列" : "四时特典");
    if (!isFeng) { shell('<section class="detail-hero"><h1>婷馔清欢</h1><p>人间有味，四时清欢。</p></section><section class="empty-state section"><h2>尚待入席</h2><p>这一席会在后续迭代继续补充。</p></section>'); return; }
    shell(`<section class="detail-hero"><h1>${heading}</h1><p>楚水有味，云峰藏香。</p><div class="meta"><span>${yunfeng.length} 道收藏</span><span>点击后打开原菜谱</span></div></section><section class="section special-list">${yunfeng.map((item) => `<a class="special-card cover-special" href="${attr(item.sourceUrl)}" target="_blank" rel="noopener noreferrer"><img src="${attr(item.coverUrl)}" alt="" loading="lazy" referrerpolicy="no-referrer"><span class="special-copy"><span class="special-mark">云峰收藏</span><h2>${escapeHtml(item.title)}</h2></span></a>`).join("")}</section>`);
  }

  function renderCollections() {
    setHeader("个人菜谱集", "编号菜谱集与定制特典");
    const collections = visibleCollectionList();
    shell(`<div class="screen-heading"><div><h1>个人菜谱集</h1><p>先显示本机缓存，再按需从云端同步。</p></div><div class="toolbar"><button class="icon-button" id="sync-collections" type="button" aria-label="同步云端菜谱集">${icons.cloud}</button><button class="icon-button" id="add-collection" type="button" aria-label="创建菜谱集">${icons.add}</button></div></div>
      ${collections.length ? `<div class="collection-list">${collections.map((item) => `<button class="collection-card" type="button" data-collection="${attr(item.id)}"><span class="special-mark">${item.type === "special" ? "定制特典" : "普通菜谱集"}</span><h2>${escapeHtml(item.name)}</h2><span>${escapeHtml(item.id)} · ${item.recipes.length} 道菜${data.dirtyCollections.includes(item.id) ? " · 待同步" : ""}</span></button>`).join("")}</div>` : '<section class="empty-state"><h2>还没有菜谱集</h2><p>创建普通菜谱集，或输入密钥解锁定制特典。</p></section>'}
      <section class="section button-row"><button class="secondary-button" id="unlock-special" type="button">输入特典密钥</button><button class="secondary-button" id="cloud-config" type="button">云端设置</button></section>
      <section class="section notice">云端只同步菜谱集和其中的自定义菜谱。皮肤、收藏、菜篮、采购清单和习惯排序只保存在当前设备。</section>`);
    main.querySelectorAll("[data-collection]").forEach((button) => button.addEventListener("click", () => navigate("collection", { id: button.dataset.collection })));
    document.getElementById("add-collection").addEventListener("click", createCollection);
    document.getElementById("unlock-special").addEventListener("click", unlockSpecial);
    document.getElementById("sync-collections").addEventListener("click", syncCollections);
    document.getElementById("cloud-config").addEventListener("click", showCloudSettings);
  }

  function createCollection() {
    showTextPrompt("创建普通菜谱集", "菜谱集名称", "我的菜谱集", async (name) => {
      const token = localStorage.getItem(CONFIG.tokenKey) || "";
      closeModal();
      if (token && navigator.onLine) {
        showToast("正在从云端分配连续编号…", true);
        try {
          const result = await createRemoteCollection(token, name);
          data.cloudState = result.state;
          data.cloudSyncedAt = Date.now();
          saveData();
          navigate("collection", { id: result.collection.id });
          showToast(`菜谱集 ${result.collection.id} 已创建`);
          return;
        } catch (error) {
          showToast(`${error.message || "云端创建失败"}，已改为本机待同步`);
        }
      }
      const result = core.allocateStandardCollection(data.cloudState, name);
      data.cloudState = result.state;
      data.dirtyCollections = [...new Set([...data.dirtyCollections, result.collection.id])];
      saveData();
      navigate("collection", { id: result.collection.id });
    });
  }

  function unlockSpecial() {
    showModal("解锁定制特典", `<form class="form" id="special-key-form"><div class="field"><label for="special-key">特典密钥</label><input id="special-key" autocomplete="off" required></div><div class="field"><label for="special-name">特典名称</label><input id="special-name" maxlength="40" value="${core.SPECIAL_NAME}"></div><button class="primary-button" type="submit">解锁特典</button></form>`, (sheet) => {
      sheet.querySelector("#special-key-form").addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!core.isSpecialKeyValid(sheet.querySelector("#special-key").value)) { showToast("特典密钥不正确"); return; }
        const specialName = sheet.querySelector("#special-name").value;
        data.unlockedSpecials = [...new Set([...data.unlockedSpecials, core.SPECIAL_ID])];
        closeModal();
        const token = localStorage.getItem(CONFIG.tokenKey) || "";
        if (token && navigator.onLine) {
          showToast("正在获取定制特典…", true);
          try {
            const result = await ensureRemoteSpecial(token, specialName);
            data.cloudState = result.state;
            data.cloudSyncedAt = Date.now();
            saveData(); render(); showToast("定制特典已解锁");
            return;
          } catch (error) {
            showToast(`${error.message || "云端获取失败"}，已保留本机解锁`);
          }
        }
        const wasMissing = !data.cloudState.collections[core.SPECIAL_ID];
        const result = core.ensureSpecialCollection(data.cloudState, specialName);
        data.cloudState = result.state;
        if (wasMissing) data.dirtyCollections = [...new Set([...data.dirtyCollections, core.SPECIAL_ID])];
        saveData(); render();
      });
    });
  }

  function renderCollection(id) {
    const collection = visibleCollectionList().find((item) => item.id === id);
    if (!collection) { setHeader("个人菜谱集", "内容不可见"); shell('<section class="empty-state"><h2>没有找到这个菜谱集</h2><p>定制特典需要先输入正确密钥。</p></section>'); return; }
    setHeader(collection.name, `${collection.id} · ${collection.type === "special" ? "定制特典" : "普通菜谱集"}`);
    const recipes = core.sortByHabit(collection.recipes, data.usage);
    shell(`<div class="screen-heading"><div><h1>${escapeHtml(collection.name)}</h1><p>${escapeHtml(collection.id)} · ${recipes.length} 道菜${data.dirtyCollections.includes(collection.id) ? " · 待同步" : ""}</p></div><div class="toolbar"><button class="icon-button" id="rename-collection" type="button" aria-label="修改菜谱集名称">${icons.collections}</button><button class="icon-button" id="add-collection-recipe" type="button" aria-label="添加菜谱">${icons.add}</button></div></div>${recipes.length ? `<div class="recipe-list">${recipes.map((item) => recipeCard(item, collection.id)).join("")}</div>` : '<section class="empty-state"><h2>菜谱集还是空的</h2><p>添加第一道个人菜谱。</p></section>'}`);
    bindRecipeCards();
    document.getElementById("add-collection-recipe").addEventListener("click", () => navigate("recipeForm", { collectionId: collection.id }));
    document.getElementById("rename-collection").addEventListener("click", () => showTextPrompt("修改菜谱集名称", "名称", collection.name, (value) => { collection.name = value; collection.updatedAt = Date.now(); data.dirtyCollections = [...new Set([...data.dirtyCollections, collection.id])]; saveData(); closeModal(); render(); }));
  }

  function showCloudSettings() {
    const hasToken = Boolean(localStorage.getItem(CONFIG.tokenKey));
    showModal("云端菜谱集设置", `<form class="form" id="cloud-form"><div class="notice">网页中不内置 GitHub 令牌。令牌只保存在当前浏览器，用于访问私有菜谱仓库。</div><div class="field"><label for="cloud-token">GitHub 令牌</label><input id="cloud-token" type="password" autocomplete="off" placeholder="${hasToken ? "已保存，留空保持不变" : "输入可读写菜谱仓库的令牌"}"></div><div class="button-row"><button class="primary-button" type="submit">保存并同步</button>${hasToken ? '<button class="danger-button" id="remove-token" type="button">移除令牌</button>' : ""}</div></form>`, (sheet) => {
      sheet.querySelector("#cloud-form").addEventListener("submit", async (event) => { event.preventDefault(); const token = sheet.querySelector("#cloud-token").value.trim(); if (token) localStorage.setItem(CONFIG.tokenKey, token); closeModal(); await syncCollections(); });
      const remove = sheet.querySelector("#remove-token"); if (remove) remove.addEventListener("click", () => { localStorage.removeItem(CONFIG.tokenKey); closeModal(); render(); showToast("已移除本机云端令牌"); });
    });
  }

  async function syncCollections() {
    const token = localStorage.getItem(CONFIG.tokenKey) || "";
    if (!token) { showCloudSettings(); return; }
    showToast("正在同步云端菜谱集…", true);
    try {
      let snapshot;
      for (let attempt = 0; attempt < 5; attempt += 1) {
        snapshot = await readCloudState(token);
        if (!data.dirtyCollections.length) break;
        const merged = mergeDirtyCollections(snapshot.state);
        try {
          snapshot = await writeCloudState(token, merged, snapshot.sha);
          data.dirtyCollections = [];
          break;
        } catch (error) {
          if ((error.status === 409 || error.status === 422) && attempt < 4) continue;
          throw error;
        }
      }
      if (!snapshot) throw new Error("云端同步未完成");
      data.cloudState = core.normalizeCloudState(snapshot.state);
      data.cloudSyncedAt = Date.now(); saveData(); render(); showToast("云端菜谱集已同步");
    } catch (error) { showToast(error.message || "云端同步失败"); }
  }

  function mergeDirtyCollections(remoteState) {
    let merged = core.normalizeCloudState(remoteState);
    data.dirtyCollections.forEach((id) => {
      const local = data.cloudState.collections[id];
      const remote = merged.collections[id];
      if (!local) return;
      if (remote && local.type === "standard" && remote.createdAt !== local.createdAt) {
        const allocation = core.allocateStandardCollection(merged, local.name, local.createdAt);
        merged = allocation.state;
        allocation.collection.recipes = local.recipes;
        allocation.collection.updatedAt = Date.now();
        return;
      }
      if (remote && local.type === "special" && remote.createdAt !== local.createdAt) {
        const recipes = new Map(remote.recipes.map((recipe) => [recipe.id, recipe]));
        local.recipes.forEach((recipe) => recipes.set(recipe.id, recipe));
        merged.collections[id] = core.normalizeCollection({ ...remote, name: local.name || remote.name, revision: remote.revision + 1, updatedAt: Date.now(), recipes: [...recipes.values()] });
        return;
      }
      if (remote && remote.revision > local.revision) throw new Error(`${id} 云端已有更新，请先导出本机备份后再处理`);
      const next = core.normalizeCollection(local);
      next.revision = remote ? remote.revision + 1 : Math.max(1, next.revision);
      next.createdAt = remote ? remote.createdAt : next.createdAt;
      next.updatedAt = Date.now();
      merged.collections[id] = next;
    });
    merged.nextNormalNumber = Math.max(merged.nextNormalNumber, data.cloudState.nextNormalNumber);
    return core.normalizeCloudState(merged);
  }

  async function createRemoteCollection(token, name) {
    for (let attempt = 0; attempt < 5; attempt += 1) {
      const snapshot = await readCloudState(token);
      const result = core.allocateStandardCollection(snapshot.state, name);
      try {
        const written = await writeCloudState(token, result.state, snapshot.sha);
        return { state: written.state, collection: written.state.collections[result.collection.id] };
      } catch (error) {
        if ((error.status === 409 || error.status === 422) && attempt < 4) continue;
        throw error;
      }
    }
    throw new Error("编号分配繁忙，请重试");
  }

  async function ensureRemoteSpecial(token, name) {
    for (let attempt = 0; attempt < 5; attempt += 1) {
      const snapshot = await readCloudState(token);
      if (snapshot.state.collections[core.SPECIAL_ID]) return { state: snapshot.state, collection: snapshot.state.collections[core.SPECIAL_ID] };
      const result = core.ensureSpecialCollection(snapshot.state, name);
      try {
        const written = await writeCloudState(token, result.state, snapshot.sha);
        return { state: written.state, collection: written.state.collections[core.SPECIAL_ID] };
      } catch (error) {
        if ((error.status === 409 || error.status === 422) && attempt < 4) continue;
        throw error;
      }
    }
    throw new Error("特典创建繁忙，请重试");
  }

  async function readCloudState(token) {
    const url = `https://api.github.com/repos/${CONFIG.cloudOwner}/${CONFIG.cloudRepository}/contents/${CONFIG.cloudStatePath}?ref=main`;
    const response = await githubRequest(url, token);
    if (response.status === 404) return { state: core.createDefaultData(CONFIG.profile).cloudState, sha: "" };
    if (!response.ok) throw await githubError(response);
    const item = await response.json();
    const raw = decodeBase64Utf8(String(item.content || "").replace(/\n/g, ""));
    return { state: core.normalizeCloudState(JSON.parse(raw)), sha: item.sha || "" };
  }

  async function writeCloudState(token, state, sha) {
    const url = `https://api.github.com/repos/${CONFIG.cloudOwner}/${CONFIG.cloudRepository}/contents/${CONFIG.cloudStatePath}`;
    const body = { message: `collections: update from PWA ${CONFIG.profile}`, branch: "main", content: encodeBase64Utf8(JSON.stringify(state, null, 2) + "\n") };
    if (sha) body.sha = sha;
    const response = await githubRequest(url, token, { method: "PUT", body: JSON.stringify(body) });
    if (!response.ok) throw await githubError(response);
    return { state, sha: (await response.json()).content.sha };
  }

  function githubRequest(url, token, options) {
    return fetch(url, { ...(options || {}), headers: { Accept: "application/vnd.github+json", Authorization: `Bearer ${token}`, "X-GitHub-Api-Version": "2022-11-28", "Content-Type": "application/json", ...((options && options.headers) || {}) } });
  }

  async function githubError(response) {
    let message = ""; try { message = (await response.json()).message || ""; } catch (_) { /* no body */ }
    let error;
    if (response.status === 401 || response.status === 403) error = new Error("云端授权无效，请检查令牌");
    else if (response.status === 409 || response.status === 422) error = new Error("云端刚刚被其他设备更新，请重新同步");
    else error = new Error(`云端返回 ${response.status}${message ? `：${message}` : ""}`);
    error.status = response.status;
    return error;
  }

  function encodeBase64Utf8(value) {
    const bytes = new TextEncoder().encode(value);
    let binary = "";
    for (let offset = 0; offset < bytes.length; offset += 0x8000) {
      binary += String.fromCharCode(...bytes.subarray(offset, offset + 0x8000));
    }
    return btoa(binary);
  }
  function decodeBase64Utf8(value) { return new TextDecoder().decode(Uint8Array.from(atob(value), (char) => char.charCodeAt(0))); }

  function exportBackup() {
    const payload = core.exportPayload(data, CONFIG.profile);
    const blob = new Blob([JSON.stringify(payload, null, 2) + "\n"], { type: "application/json" });
    const link = document.createElement("a"); link.href = URL.createObjectURL(blob); link.download = `lazy-sheep-chef-${CONFIG.profile}-${new Date().toISOString().slice(0, 10)}.json`; link.click(); setTimeout(() => URL.revokeObjectURL(link.href), 1000); showToast("备份文件已导出");
  }

  async function importBackupFile(event) {
    const file = event.target.files && event.target.files[0]; event.target.value = ""; if (!file) return;
    try { const payload = JSON.parse(await file.text()); const imported = core.importPayload(payload, CONFIG.profile); confirmAction("导入这份本机备份？", "将替换当前 PWA 的本地菜谱、菜篮、清单和本机设置。云端令牌不受影响。", () => { data = imported; saveData(); navigate("home", {}, true); showToast("本机备份已恢复"); }); } catch (error) { showToast(error.message || "备份文件无法读取"); }
  }

  function showModal(heading, content, onReady) {
    modalRoot.innerHTML = `<div class="modal-backdrop" role="presentation"><section class="modal-sheet" role="dialog" aria-modal="true" aria-labelledby="modal-title"><div class="modal-heading"><h2 id="modal-title">${escapeHtml(heading)}</h2><button class="icon-button" id="modal-close" type="button" aria-label="关闭">${icons.close}</button></div>${content}</section></div>`;
    const sheet = modalRoot.querySelector(".modal-sheet");
    modalRoot.querySelector("#modal-close").addEventListener("click", closeModal);
    modalRoot.querySelector(".modal-backdrop").addEventListener("click", (event) => { if (event.target === event.currentTarget) closeModal(); });
    document.addEventListener("keydown", modalEscape);
    if (onReady) onReady(sheet);
    const focusable = sheet.querySelector("input, select, textarea, button"); if (focusable) focusable.focus({ preventScroll: true });
  }

  function modalEscape(event) { if (event.key === "Escape") closeModal(); }
  function closeModal() { modalRoot.innerHTML = ""; document.removeEventListener("keydown", modalEscape); }

  function showTextPrompt(heading, label, value, onSubmit) {
    showModal(heading, `<form class="form" id="text-prompt"><div class="field"><label for="text-value">${escapeHtml(label)}</label><input id="text-value" maxlength="60" required value="${attr(value)}"></div><button class="primary-button" type="submit">确认</button></form>`, (sheet) => sheet.querySelector("#text-prompt").addEventListener("submit", (event) => { event.preventDefault(); const result = sheet.querySelector("#text-value").value.trim(); if (result) { closeModal(); onSubmit(result); } }));
  }

  function confirmAction(heading, description, onConfirm) {
    showModal(heading, `<p>${escapeHtml(description)}</p><div class="button-row"><button class="danger-button" id="confirm-action" type="button">确认</button><button class="secondary-button" id="cancel-action" type="button">取消</button></div>`, (sheet) => { sheet.querySelector("#confirm-action").addEventListener("click", () => { closeModal(); onConfirm(); }); sheet.querySelector("#cancel-action").addEventListener("click", closeModal); });
  }

  function showToast(message, persistent) {
    clearTimeout(toastTimer); toastNode.textContent = message; toastNode.hidden = false;
    if (!persistent) toastTimer = setTimeout(() => { toastNode.hidden = true; }, 2800);
  }

  async function installApp() {
    if (deferredInstallPrompt) { deferredInstallPrompt.prompt(); await deferredInstallPrompt.userChoice; deferredInstallPrompt = null; installButton.hidden = true; return; }
    showModal("添加到主屏幕", `<div class="install-guide"><div class="install-step"><strong>1</strong><span>使用 iPhone 的 Safari 打开本页面。</span></div><div class="install-step"><strong>2</strong><span>点击 Safari 底部的“分享”按钮。</span></div><div class="install-step"><strong>3</strong><span>选择“添加到主屏幕”，再确认“添加”。</span></div></div>`);
  }

  function formatDate(timestamp) { return new Intl.DateTimeFormat("zh-CN", { month: "numeric", day: "numeric" }).format(new Date(timestamp)); }

  async function registerServiceWorker() {
    if (!("serviceWorker" in navigator) || location.protocol === "file:") return;
    try { await navigator.serviceWorker.register("./service-worker.js", { scope: "./" }); } catch (_) { showToast("离线缓存暂未启用"); }
  }
})();
