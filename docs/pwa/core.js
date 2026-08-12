(function initCore(root, factory) {
  const api = factory();
  if (typeof module === "object" && module.exports) module.exports = api;
  root.LazyChefCore = api;
})(typeof globalThis !== "undefined" ? globalThis : this, function createCore() {
  "use strict";

  const VERSION = "v0.6.8-Bata";
  const SCHEMA_VERSION = 1;
  const SPECIAL_ID = "KKLLTL";
  const SPECIAL_KEY = "TL123";
  const SPECIAL_NAME = "露露的小厨房";
  const CUISINES = ["家常融合", "川菜", "湘菜", "粤菜", "江浙菜", "北方菜", "西式", "其他"];
  const CATEGORIES = ["主食", "汤羹", "炒菜", "蒸菜", "炖煮", "凉拌", "空气炸锅", "煎烤", "烘焙", "其他"];

  function now() { return Date.now(); }

  function uuid(prefix) {
    const value = typeof crypto !== "undefined" && crypto.randomUUID
      ? crypto.randomUUID()
      : "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, (letter) => {
          const random = Math.random() * 16 | 0;
          return (letter === "x" ? random : (random & 3 | 8)).toString(16);
        });
    return `${prefix || "custom"}-${value}`;
  }

  function text(value, fallback) {
    const result = value == null ? "" : String(value).trim();
    return result || fallback || "";
  }

  function uniqueStrings(values) {
    return [...new Set((Array.isArray(values) ? values : []).map((value) => text(value)).filter(Boolean))];
  }

  function normalizeIngredient(value) {
    const source = value && typeof value === "object" ? value : {};
    return {
      name: text(source.name),
      amount: text(source.amount),
      staple: Boolean(source.staple)
    };
  }

  function normalizeRecipe(value, forceCustom) {
    const source = value && typeof value === "object" ? value : {};
    const isCustom = forceCustom == null ? Boolean(source.custom) : Boolean(forceCustom);
    return {
      id: text(source.id, uuid(isCustom ? "custom" : "recipe")),
      name: text(source.name, "未命名菜谱"),
      category: CATEGORIES.includes(source.category) ? source.category : text(source.category, "其他"),
      cuisine: CUISINES.includes(source.cuisine) ? source.cuisine : "其他",
      flavor: text(source.flavor, "家常"),
      difficulty: text(source.difficulty, "简单"),
      minutes: Math.max(0, Number(source.minutes) || 0),
      servings: Math.max(1, Number(source.servings) || 2),
      ingredients: (Array.isArray(source.ingredients) ? source.ingredients : []).map(normalizeIngredient).filter((item) => item.name),
      steps: uniqueStrings(source.steps),
      tips: text(source.tips),
      custom: isCustom
    };
  }

  function normalizeCollection(value) {
    const source = value && typeof value === "object" ? value : {};
    const type = source.type === "special" ? "special" : "standard";
    const createdAt = Math.max(0, Number(source.createdAt) || now());
    return {
      schemaVersion: SCHEMA_VERSION,
      id: text(source.id),
      name: text(source.name, "未命名菜谱集"),
      type,
      revision: Math.max(0, Number(source.revision) || 0),
      createdAt,
      updatedAt: Math.max(createdAt, Number(source.updatedAt) || createdAt),
      editable: source.editable !== false,
      recipes: dedupeRecipes(source.recipes)
    };
  }

  function dedupeRecipes(values) {
    const map = new Map();
    (Array.isArray(values) ? values : []).forEach((value) => {
      const recipe = normalizeRecipe(value, true);
      if (recipe.id && recipe.name) map.set(recipe.id, recipe);
    });
    return [...map.values()];
  }

  function createEmptyCloudState() {
    return { schemaVersion: SCHEMA_VERSION, nextNormalNumber: 1, legacyMigratedProfiles: [], collections: {} };
  }

  function normalizeCloudState(value) {
    const source = value && typeof value === "object" ? value : createEmptyCloudState();
    if (Number(source.schemaVersion || SCHEMA_VERSION) !== SCHEMA_VERSION) throw new Error("暂不支持这个云端菜谱集版本");
    const items = {};
    const input = Array.isArray(source.collections)
      ? Object.fromEntries(source.collections.map((item) => [item.id, item]))
      : (source.collections && typeof source.collections === "object" ? source.collections : {});
    Object.values(input).forEach((value) => {
      const collection = normalizeCollection(value);
      if (collection.id) items[collection.id] = collection;
    });
    const highest = Math.max(0, ...Object.values(items)
      .filter((item) => item.type === "standard" && /^Dew-\d{4,}$/.test(item.id))
      .map((item) => Number(item.id.slice(4)) || 0));
    return {
      schemaVersion: SCHEMA_VERSION,
      nextNormalNumber: Math.max(1, Number(source.nextNormalNumber) || 1, highest + 1),
      legacyMigratedProfiles: uniqueStrings(source.legacyMigratedProfiles),
      collections: items
    };
  }

  function allocateStandardCollection(state, name, timestamp) {
    const output = normalizeCloudState(state);
    let number = output.nextNormalNumber;
    let id = `Dew-${String(number).padStart(4, "0")}`;
    while (output.collections[id]) {
      number += 1;
      id = `Dew-${String(number).padStart(4, "0")}`;
    }
    const createdAt = timestamp || now();
    const collection = normalizeCollection({ id, name, type: "standard", revision: 1, createdAt, updatedAt: createdAt, editable: true, recipes: [] });
    output.collections[id] = collection;
    output.nextNormalNumber = number + 1;
    return { state: output, collection };
  }

  function ensureSpecialCollection(state, name, timestamp) {
    const output = normalizeCloudState(state);
    if (!output.collections[SPECIAL_ID]) {
      const createdAt = timestamp || now();
      output.collections[SPECIAL_ID] = normalizeCollection({
        id: SPECIAL_ID,
        name: text(name, SPECIAL_NAME),
        type: "special",
        revision: 1,
        createdAt,
        updatedAt: createdAt,
        editable: true,
        recipes: []
      });
    }
    return { state: output, collection: output.collections[SPECIAL_ID] };
  }

  function isSpecialKeyValid(value) { return text(value) === SPECIAL_KEY; }

  function visibleCollections(state, unlockedIds) {
    const unlocked = new Set(uniqueStrings(unlockedIds));
    return Object.values(normalizeCloudState(state).collections)
      .filter((item) => item.type !== "special" || unlocked.has(item.id))
      .sort((left, right) => left.id.localeCompare(right.id));
  }

  function sortByHabit(recipes, usage) {
    const history = usage && typeof usage === "object" ? usage : {};
    return [...recipes].sort((left, right) => {
      const a = history[left.id] || {};
      const b = history[right.id] || {};
      const count = (Number(b.count) || 0) - (Number(a.count) || 0);
      if (count) return count;
      const recent = (Number(b.lastOpenedAt) || 0) - (Number(a.lastOpenedAt) || 0);
      if (recent) return recent;
      return left.name.localeCompare(right.name, "zh-CN");
    });
  }

  function groupByCuisine(recipes) {
    const groups = {};
    CUISINES.forEach((name) => { groups[name] = []; });
    recipes.forEach((recipe) => {
      const cuisine = CUISINES.includes(recipe.cuisine) ? recipe.cuisine : "其他";
      groups[cuisine].push(recipe);
    });
    return Object.entries(groups).filter((entry) => entry[1].length);
  }

  function filterRecipes(recipes, filters, favorites) {
    const query = text(filters && filters.query).toLocaleLowerCase("zh-CN");
    const scope = text(filters && filters.scope, "all");
    const cuisine = text(filters && filters.cuisine, "all");
    const category = text(filters && filters.category, "all");
    const favoriteSet = new Set(uniqueStrings(favorites));
    return recipes.filter((recipe) => {
      if (scope === "favorites" && !favoriteSet.has(recipe.id)) return false;
      if (scope === "custom" && !recipe.custom) return false;
      if (cuisine !== "all" && recipe.cuisine !== cuisine) return false;
      if (category !== "all" && recipe.category !== category) return false;
      if (!query) return true;
      const haystack = [recipe.name, recipe.flavor, recipe.cuisine, recipe.category]
        .concat(recipe.ingredients.map((item) => item.name)).join(" ").toLocaleLowerCase("zh-CN");
      return haystack.includes(query);
    });
  }

  function recordOpen(usage, recipeId, timestamp) {
    const output = { ...(usage || {}) };
    const previous = output[recipeId] || {};
    output[recipeId] = { count: (Number(previous.count) || 0) + 1, lastOpenedAt: timestamp || now() };
    return output;
  }

  function createDefaultData(profile) {
    return {
      schemaVersion: SCHEMA_VERSION,
      version: VERSION,
      profile: text(profile, "tings"),
      customRecipes: [],
      favorites: [],
      pantry: [],
      shopping: [],
      usage: {},
      cloudState: createEmptyCloudState(),
      unlockedSpecials: [],
      dirtyCollections: [],
      cloudSyncedAt: 0
    };
  }

  function normalizeData(value, profile) {
    const source = value && typeof value === "object" ? value : {};
    const targetProfile = text(profile, text(source.profile, "tings"));
    const fallback = createDefaultData(targetProfile);
    return {
      ...fallback,
      profile: targetProfile,
      customRecipes: dedupeRecipes(source.customRecipes),
      favorites: uniqueStrings(source.favorites),
      pantry: (Array.isArray(source.pantry) ? source.pantry : []).map((item) => ({
        id: text(item.id, uuid("pantry")),
        name: text(item.name),
        category: text(item.category, "其他"),
        quantity: text(item.quantity),
        unit: text(item.unit),
        status: ["充足", "不多", "用完"].includes(item.status) ? item.status : "充足",
        purchasedAt: Number(item.purchasedAt) || 0,
        note: text(item.note)
      })).filter((item) => item.name),
      shopping: uniqueStrings(source.shopping),
      usage: source.usage && typeof source.usage === "object" ? source.usage : {},
      cloudState: normalizeCloudState(source.cloudState),
      unlockedSpecials: uniqueStrings(source.unlockedSpecials),
      dirtyCollections: uniqueStrings(source.dirtyCollections),
      cloudSyncedAt: Math.max(0, Number(source.cloudSyncedAt) || 0)
    };
  }

  function exportPayload(data, profile) {
    return {
      app: "懒羊羊当大厨~",
      version: VERSION,
      exportedAt: new Date().toISOString(),
      data: normalizeData(data, profile)
    };
  }

  function importPayload(payload, profile) {
    if (!payload || typeof payload !== "object" || !payload.data) throw new Error("备份文件格式不正确");
    return normalizeData(payload.data, profile);
  }

  return {
    VERSION, SCHEMA_VERSION, SPECIAL_ID, SPECIAL_KEY, SPECIAL_NAME, CUISINES, CATEGORIES,
    uuid, normalizeRecipe, normalizeCollection, normalizeCloudState, allocateStandardCollection,
    ensureSpecialCollection, isSpecialKeyValid, visibleCollections, sortByHabit, groupByCuisine,
    filterRecipes, recordOpen, createDefaultData, normalizeData, exportPayload, importPayload
  };
});
