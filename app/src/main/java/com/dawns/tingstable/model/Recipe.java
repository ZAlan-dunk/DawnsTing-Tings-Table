package com.dawns.tingstable.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    public String id;
    public String name;
    public String category;
    public String flavor;
    public String difficulty;
    public int minutes;
    public int servings;
    public List<Ingredient> ingredients;
    public List<String> steps;
    public String tips;
    public boolean custom;

    public Recipe(String id, String name, String category, String flavor, String difficulty,
                  int minutes, int servings, List<Ingredient> ingredients,
                  List<String> steps, String tips, boolean custom) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.flavor = flavor;
        this.difficulty = difficulty;
        this.minutes = minutes;
        this.servings = servings;
        this.ingredients = ingredients;
        this.steps = steps;
        this.tips = tips;
        this.custom = custom;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("category", category);
        json.put("flavor", flavor);
        json.put("difficulty", difficulty);
        json.put("minutes", minutes);
        json.put("servings", servings);
        json.put("tips", tips);
        json.put("custom", custom);
        JSONArray ingredientArray = new JSONArray();
        for (Ingredient ingredient : ingredients) ingredientArray.put(ingredient.toJson());
        json.put("ingredients", ingredientArray);
        JSONArray stepArray = new JSONArray();
        for (String step : steps) stepArray.put(step);
        json.put("steps", stepArray);
        return json;
    }

    public static Recipe fromJson(JSONObject json) {
        List<Ingredient> ingredients = new ArrayList<>();
        JSONArray ingredientArray = json.optJSONArray("ingredients");
        if (ingredientArray != null) {
            for (int i = 0; i < ingredientArray.length(); i++) {
                JSONObject item = ingredientArray.optJSONObject(i);
                if (item != null) ingredients.add(Ingredient.fromJson(item));
            }
        }
        List<String> steps = new ArrayList<>();
        JSONArray stepArray = json.optJSONArray("steps");
        if (stepArray != null) {
            for (int i = 0; i < stepArray.length(); i++) {
                String step = stepArray.optString(i);
                if (!step.trim().isEmpty()) steps.add(step.trim());
            }
        }
        return new Recipe(
                json.optString("id"), json.optString("name"),
                json.optString("category", "家常菜"), json.optString("flavor", "家常"),
                json.optString("difficulty", "简单"), json.optInt("minutes", 20),
                json.optInt("servings", 2), ingredients, steps,
                json.optString("tips"), json.optBoolean("custom", true)
        );
    }
}
