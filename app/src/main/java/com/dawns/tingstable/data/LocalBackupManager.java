package com.dawns.tingstable.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.dawns.tingstable.model.ThemeMode;

import org.json.JSONException;

/** Captures and restores the app's personal data without changing built-in recipes. */
public final class LocalBackupManager {
    private static final String UI_PREFS = "ui_preferences";
    private static final String THEME_KEY = "theme_mode";
    private static final String CLOUD_PREFS = "cloud_backup_state";
    private static final String LAST_UPLOAD_KEY = "last_upload_at";
    private static final String LAST_RESTORE_KEY = "last_restore_at";

    private final Context context;
    private final RecipeRepository recipes;
    private final PantryRepository pantry;

    public LocalBackupManager(Context context, RecipeRepository recipes, PantryRepository pantry) {
        this.context = context.getApplicationContext();
        this.recipes = recipes;
        this.pantry = pantry;
    }

    public BackupPayload capture(String profileId, String appVersion, long now) {
        String theme = context.getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE)
                .getString(THEME_KEY, ThemeMode.LIGHT.id());
        return new BackupPayload(
                profileId,
                appVersion,
                now,
                recipes.getCustomRecipes(),
                recipes.getFavorites(),
                pantry.getItems(),
                recipes.getShoppingItems(),
                recipes.getSelectedIngredients(),
                theme
        );
    }

    public BackupPayload parse(String raw, String expectedProfileId) throws JSONException {
        return BackupPayload.parse(raw, expectedProfileId);
    }

    public boolean restore(BackupPayload payload) {
        BackupPayload previous = capture(payload.profileId, payload.appVersion, System.currentTimeMillis());
        if (restoreInternal(payload)) return true;
        restoreInternal(previous);
        return false;
    }

    private boolean restoreInternal(BackupPayload payload) {
        if (!recipes.replacePersonalData(payload.customRecipes, payload.favorites,
                payload.selectedIngredients, payload.shoppingList)) return false;
        if (!pantry.replaceItems(payload.pantryItems)) return false;
        return context.getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(THEME_KEY, ThemeMode.fromId(payload.themeMode).id())
                .commit();
    }

    public void markUploaded(long time) {
        state().edit().putLong(LAST_UPLOAD_KEY, time).apply();
    }

    public void markRestored(long time) {
        state().edit().putLong(LAST_RESTORE_KEY, time).apply();
    }

    public long lastUploadAt() {
        return state().getLong(LAST_UPLOAD_KEY, 0L);
    }

    public long lastRestoreAt() {
        return state().getLong(LAST_RESTORE_KEY, 0L);
    }

    private SharedPreferences state() {
        return context.getSharedPreferences(CLOUD_PREFS, Context.MODE_PRIVATE);
    }
}
