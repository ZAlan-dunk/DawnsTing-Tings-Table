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
    private static final String KEY_UNLOCKED_SPECIALS = "unlocked_special_ids";

    private final SharedPreferences preferences;

    public RecipeCollectionRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Returns only collections the current device is allowed to show. */
    public List<RecipeCollection> getCollections() {
        Set<String> unlocked = unlockedSpecialIds();
        List<RecipeCollection> result = new ArrayList<>();
        for (RecipeCollection collection : getStoredCollections()) {
            if (RecipeCollectionVisibility.isVisible(collection, unlocked)) result.add(collection);
        }
        return result;
    }

    /** Includes hidden special collections cached from the cloud. */
    public List<RecipeCollection> getStoredCollections() {
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

    public RecipeCollection findStoredById(String id) {
        for (RecipeCollection collection : getStoredCollections()) {
            if (collection.id.equals(id)) return collection;
        }
        return null;
    }

    public void mergeCatalog(List<RecipeCollection> remote) {
        Map<String, RecipeCollection> merged = new LinkedHashMap<>();
        for (RecipeCollection local : getStoredCollections()) merged.put(local.id, local);
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
        for (RecipeCollection item : getStoredCollections()) values.put(item.id, item);
        values.put(collection.id, collection);
        persist(new ArrayList<>(values.values()));
    }

    public void unlockSpecial(String collectionId) {
        String id = collectionId == null ? "" : collectionId.trim();
        if (id.isEmpty()) return;
        Set<String> values = unlockedSpecialIds();
        values.add(id);
        preferences.edit().putStringSet(KEY_UNLOCKED_SPECIALS, values).apply();
    }

    public boolean isSpecialUnlocked(String collectionId) {
        return unlockedSpecialIds().contains(collectionId);
    }

    public boolean canEdit(RecipeCollection collection) {
        return RecipeCollectionVisibility.canEdit(collection, unlockedSpecialIds());
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

    private Set<String> unlockedSpecialIds() {
        return new LinkedHashSet<>(preferences.getStringSet(
                KEY_UNLOCKED_SPECIALS, java.util.Collections.emptySet()));
    }

    private void persist(List<RecipeCollection> collections) {
        try {
            JSONArray array = new JSONArray();
            for (RecipeCollection collection : collections) array.put(collection.toJson());
            preferences.edit().putString(KEY_COLLECTIONS, array.toString()).apply();
        } catch (Exception ignored) { }
    }
}
