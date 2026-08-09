package com.dawns.tingstable.data;

import android.content.Context;

import com.dawns.tingstable.model.SpecialRecipe;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpecialRecipeCatalog {
    private SpecialRecipeCatalog() { }

    public static List<SpecialRecipe> load(Context context, int resourceId) {
        try (InputStream input = context.getResources().openRawResource(resourceId)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
            return parse(new String(output.toByteArray(), StandardCharsets.UTF_8));
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    static List<SpecialRecipe> parse(String raw) {
        List<SpecialRecipe> result = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(raw);
            JSONArray recipes = root.optJSONArray("recipes");
            if (recipes == null) return result;
            for (int i = 0; i < recipes.length(); i++) {
                JSONObject item = recipes.optJSONObject(i);
                if (item == null) continue;
                SpecialRecipe recipe = new SpecialRecipe(
                        item.optString("id"),
                        item.optString("title"),
                        item.optString("sourceUrl"),
                        item.optString("coverUrl")
                );
                if (recipe.isValid()) result.add(recipe);
            }
        } catch (Exception ignored) { }
        return result;
    }
}
