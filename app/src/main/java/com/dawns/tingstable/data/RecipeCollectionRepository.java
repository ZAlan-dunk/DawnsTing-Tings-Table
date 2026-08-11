package com.dawns.tingstable.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.dawns.tingstable.model.RecipeCollection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class RecipeCollectionRepository {
    private static final String PREFS = "recipe_collections_v01";
    private static final String KEY_COLLECTIONS = "collections";
    private static final String KEY_LAST_SYNC = "last_sync_at";
    private static final String KEY_DIRTY = "dirty_collection_ids";

    private final SharedPreferences preferences;

    public RecipeCollectionRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<RecipeCollection> getCollections() {
        List<RecipeCollection> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(preferences.getString(KEY_COLLECTIONS, "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) result.add(RecipeCollection.fromJson(item));
            }
        } catch (Exception ignored) { }
        return result;
    }

    public RecipeCollection findById(String id) {
        for (RecipeCollection collection : getCollections()) {
            if (collection.id.equals(id)) return collection;
        }
        return null;
    }

    public void mergeCatalog(List<RecipeCollection> remote) {
        Map<String, RecipeCollection> merged = new LinkedHashMap<>();
        for (RecipeCollection local : getCollections()) merged.put(local.id, local);
        if (remote != null) {
            for (RecipeCollection collection : remote) {
                if (collection == null) continue;
                RecipeCollection local = merged.get(collection.id);
                if (!isDirty(collection.id) && (local == null || collection.revision >= local.revision)) {
                    merged.put(collection.id, collection);
                }
            }
        }
        persist(new ArrayList<>(merged.values()));
    }

    public void saveCollection(RecipeCollection collection) {
        Map<String, RecipeCollection> values = new LinkedHashMap<>();
        for (RecipeCollection item : getCollections()) values.put(item.id, item);
        values.put(collection.id, collection);
        persist(new ArrayList<>(values.values()));
    }

    public long lastSyncAt() {
        return preferences.getLong(KEY_LAST_SYNC, 0L);
    }

    public void markSynced(long at) {
        preferences.edit().putLong(KEY_LAST_SYNC, Math.max(0L, at)).apply();
    }

    public boolean isDirty(String collectionId) {
        return preferences.getStringSet(KEY_DIRTY, java.util.Collections.emptySet()).contains(collectionId);
    }

    public void markDirty(String collectionId) {
        Set<String> values = new LinkedHashSet<>(
                preferences.getStringSet(KEY_DIRTY, java.util.Collections.emptySet()));
        values.add(collectionId);
        preferences.edit().putStringSet(KEY_DIRTY, values).apply();
    }

    public void markClean(String collectionId) {
        Set<String> values = new LinkedHashSet<>(
                preferences.getStringSet(KEY_DIRTY, java.util.Collections.emptySet()));
        values.remove(collectionId);
        preferences.edit().putStringSet(KEY_DIRTY, values).apply();
    }

    private void persist(List<RecipeCollection> collections) {
        try {
            JSONArray array = new JSONArray();
            for (RecipeCollection collection : collections) array.put(collection.toJson());
            preferences.edit().putString(KEY_COLLECTIONS, array.toString()).apply();
        } catch (Exception ignored) { }
    }
}
