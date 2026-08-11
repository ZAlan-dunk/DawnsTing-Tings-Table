package com.dawns.tingstable.data;

import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.RecipeCollection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pure state transitions for the shared collections/state.json document. */
public final class RecipeCollectionCloudState {
    private static final int SCHEMA_VERSION = 1;

    private int nextNormalNumber;
    private final Set<String> legacyMigratedProfiles;
    private final Map<String, RecipeCollection> collections;

    private RecipeCollectionCloudState(int nextNormalNumber, Set<String> migrated,
                                       Map<String, RecipeCollection> collections) {
        this.nextNormalNumber = Math.max(1, nextNormalNumber);
        this.legacyMigratedProfiles = migrated;
        this.collections = collections;
    }

    public static RecipeCollectionCloudState empty() {
        return new RecipeCollectionCloudState(1, new LinkedHashSet<>(), new LinkedHashMap<>());
    }

    public static RecipeCollectionCloudState fromJson(JSONObject json) {
        if (json == null) return empty();
        if (json.optInt("schemaVersion", SCHEMA_VERSION) != SCHEMA_VERSION) {
            throw new IllegalArgumentException("暂不支持这个云端菜谱集版本");
        }
        Set<String> migrated = new LinkedHashSet<>();
        JSONArray profiles = json.optJSONArray("legacyMigratedProfiles");
        if (profiles != null) {
            for (int i = 0; i < profiles.length(); i++) {
                String value = profiles.optString(i).trim();
                if (!value.isEmpty()) migrated.add(value);
            }
        }
        Map<String, RecipeCollection> collections = new LinkedHashMap<>();
        JSONObject values = json.optJSONObject("collections");
        if (values != null) {
            Iterator<String> keys = values.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject item = values.optJSONObject(key);
                if (item == null) continue;
                RecipeCollection collection = parseCollection(item);
                collections.put(collection.id, collection);
            }
        } else {
            JSONArray array = json.optJSONArray("collections");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.optJSONObject(i);
                    if (item == null) continue;
                    RecipeCollection collection = parseCollection(item);
                    collections.put(collection.id, collection);
                }
            }
        }
        int next = Math.max(json.optInt("nextNormalNumber", 1), nextAfterExisting(collections));
        return new RecipeCollectionCloudState(next, migrated, collections);
    }

    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("schemaVersion", SCHEMA_VERSION);
            json.put("nextNormalNumber", nextNormalNumber);
            json.put("legacyMigratedProfiles", new JSONArray(legacyMigratedProfiles));
            JSONObject values = new JSONObject();
            for (RecipeCollection collection : collections.values()) {
                values.put(collection.id, collection.toJson());
            }
            json.put("collections", values);
            return json;
        } catch (Exception error) {
            throw new IllegalStateException("无法生成云端菜谱集数据", error);
        }
    }

    public List<RecipeCollection> collections() {
        List<RecipeCollection> result = new ArrayList<>(collections.values());
        result.sort(Comparator.comparing(value -> value.id));
        return result;
    }

    public RecipeCollection find(String collectionId) {
        return collections.get(collectionId == null ? "" : collectionId.trim());
    }

    public boolean isLegacyMigrated(String profileId) {
        return legacyMigratedProfiles.contains(profileId);
    }

    public void markLegacyMigrated(String profileId) {
        if (profileId != null && !profileId.trim().isEmpty()) {
            legacyMigratedProfiles.add(profileId.trim());
        }
    }

    public RecipeCollection createStandard(String name, List<Recipe> recipes, long now) {
        String id = allocateNormalId();
        RecipeCollection collection = new RecipeCollection(
                id, name, RecipeCollection.TYPE_STANDARD, 1L, now, now, true, recipes);
        collections.put(id, collection);
        return collection;
    }

    public RecipeCollection ensureSpecial(String id, String name, long now) {
        RecipeCollection existing = find(id);
        if (existing != null) return existing;
        RecipeCollection collection = new RecipeCollection(
                id, name, RecipeCollection.TYPE_SPECIAL, 1L, now, now, true,
                java.util.Collections.emptyList());
        collections.put(id, collection);
        return collection;
    }

    public RecipeCollection applyUpdate(RecipeCollection incoming, boolean force, long now) {
        if (incoming == null) throw new IllegalArgumentException("菜谱集数据为空");
        RecipeCollection current = find(incoming.id);
        if (current == null) throw new IllegalStateException("请求的菜谱集不存在");
        if (!force && incoming.revision != current.revision) {
            throw new IllegalStateException("VERSION_CONFLICT");
        }
        RecipeCollection updated = new RecipeCollection(
                current.id, incoming.name, current.type, current.revision + 1,
                current.createdAt, Math.max(current.updatedAt, now), true, incoming.recipes);
        collections.put(updated.id, updated);
        return updated;
    }

    public static List<Recipe> mergeRecipes(List<Recipe> values) {
        Map<String, Recipe> merged = new LinkedHashMap<>();
        if (values != null) {
            for (Recipe recipe : values) {
                if (recipe == null || recipe.id == null || recipe.id.trim().isEmpty()
                        || recipe.name == null || recipe.name.trim().isEmpty()) continue;
                merged.put(recipe.id, recipe);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private String allocateNormalId() {
        String id;
        do {
            id = String.format(java.util.Locale.US, "Dew-%04d", nextNormalNumber++);
        } while (collections.containsKey(id));
        return id;
    }

    private static int nextAfterExisting(Map<String, RecipeCollection> collections) {
        int next = 1;
        for (RecipeCollection collection : collections.values()) {
            if (!RecipeCollection.TYPE_STANDARD.equals(collection.type)) continue;
            try {
                next = Math.max(next, Integer.parseInt(collection.id.substring(4)) + 1);
            } catch (Exception ignored) { }
        }
        return next;
    }

    private static RecipeCollection parseCollection(JSONObject source) {
        try {
            JSONObject normalized = new JSONObject(source.toString());
            normalized.put("editable", true);
            return RecipeCollection.fromJson(normalized);
        } catch (Exception error) {
            throw new IllegalArgumentException("云端菜谱集数据格式错误", error);
        }
    }
}
