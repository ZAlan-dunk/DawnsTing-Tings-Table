"use strict";

const assert = require("node:assert/strict");
const test = require("node:test");
const core = require("../core.js");

test("ordinary collection ids are sequential and never reuse existing numbers", () => {
  let state = core.normalizeCloudState({
    schemaVersion: 1,
    nextNormalNumber: 1,
    collections: {
      "Dew-0003": { id: "Dew-0003", name: "旧集", type: "standard", revision: 1, recipes: [] }
    }
  });
  const first = core.allocateStandardCollection(state, "第一集", 100);
  assert.equal(first.collection.id, "Dew-0004");
  const second = core.allocateStandardCollection(first.state, "第二集", 200);
  assert.equal(second.collection.id, "Dew-0005");
});

test("same-name custom recipes remain separate by uuid", () => {
  const first = core.normalizeRecipe({ id: "custom-a", name: "蛋炒饭" }, true);
  const second = core.normalizeRecipe({ id: "custom-b", name: "蛋炒饭" }, true);
  const collection = core.normalizeCollection({ id: "Dew-0001", name: "家常", recipes: [first, second] });
  assert.equal(collection.recipes.length, 2);
  assert.deepEqual(collection.recipes.map((item) => item.id), ["custom-a", "custom-b"]);
});

test("custom special stays hidden until its id is locally unlocked", () => {
  const state = core.ensureSpecialCollection(core.normalizeCloudState({
    schemaVersion: 1,
    collections: { "Dew-0001": { id: "Dew-0001", name: "普通", type: "standard", recipes: [] } }
  })).state;
  assert.deepEqual(core.visibleCollections(state, []).map((item) => item.id), ["Dew-0001"]);
  assert.deepEqual(core.visibleCollections(state, [core.SPECIAL_ID]).map((item) => item.id), ["Dew-0001", core.SPECIAL_ID]);
  assert.equal(core.isSpecialKeyValid("TL123"), true);
  assert.equal(core.isSpecialKeyValid("tl123"), false);
});

test("habit sort uses open count, recent time, then name", () => {
  const recipes = [
    core.normalizeRecipe({ id: "a", name: "A" }, false),
    core.normalizeRecipe({ id: "b", name: "B" }, false),
    core.normalizeRecipe({ id: "c", name: "C" }, false)
  ];
  const sorted = core.sortByHabit(recipes, {
    a: { count: 2, lastOpenedAt: 10 },
    b: { count: 3, lastOpenedAt: 5 },
    c: { count: 2, lastOpenedAt: 20 }
  });
  assert.deepEqual(sorted.map((item) => item.id), ["b", "c", "a"]);
});

test("export and import preserve recipe data while staying in the target profile", () => {
  const source = core.createDefaultData("tings");
  source.customRecipes.push(core.normalizeRecipe({ id: "custom-1", name: "测试菜" }, true));
  source.favorites.push("custom-1");
  source.dirtyCollections.push("Dew-0001");
  const payload = core.exportPayload(source, "tings");
  const restored = core.importPayload(payload, "lulu");
  assert.equal(restored.profile, "lulu");
  assert.equal(restored.customRecipes[0].id, "custom-1");
  assert.deepEqual(restored.favorites, ["custom-1"]);
  assert.deepEqual(restored.dirtyCollections, ["Dew-0001"]);
});

test("category grouping omits empty cuisines", () => {
  const groups = core.groupByCuisine([
    core.normalizeRecipe({ id: "a", name: "A", cuisine: "川菜" }, false),
    core.normalizeRecipe({ id: "b", name: "B", cuisine: "粤菜" }, false)
  ]);
  assert.deepEqual(groups.map(([name]) => name), ["川菜", "粤菜"]);
});
