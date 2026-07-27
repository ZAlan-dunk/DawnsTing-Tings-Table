package com.dawns.tingstable.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Ingredient {
    public String name;
    public String amount;
    public boolean staple;

    public Ingredient(String name, String amount, boolean staple) {
        this.name = name.trim();
        this.amount = amount.trim();
        this.staple = staple;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("amount", amount);
        json.put("staple", staple);
        return json;
    }

    public static Ingredient fromJson(JSONObject json) {
        return new Ingredient(
                json.optString("name"),
                json.optString("amount", "适量"),
                json.optBoolean("staple", false)
        );
    }
}
