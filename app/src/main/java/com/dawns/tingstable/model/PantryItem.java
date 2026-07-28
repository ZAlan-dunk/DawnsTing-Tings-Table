package com.dawns.tingstable.model;

import org.json.JSONException;
import org.json.JSONObject;

public class PantryItem {
    public static final String STATUS_FULL = "充足";
    public static final String STATUS_LOW = "不多";
    public static final String STATUS_EMPTY = "用完";

    public String id;
    public String name;
    public String category;
    public String quantity;
    public String unit;
    public String status;
    public long purchasedAt;
    public String note;

    public PantryItem(String id, String name, String category, String quantity, String unit,
                      String status, long purchasedAt, String note) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name.trim();
        this.category = category == null || category.trim().isEmpty() ? "其他" : category.trim();
        this.quantity = quantity == null ? "" : quantity.trim();
        this.unit = unit == null ? "" : unit.trim();
        this.status = status == null || status.trim().isEmpty() ? STATUS_FULL : status.trim();
        this.purchasedAt = purchasedAt;
        this.note = note == null ? "" : note.trim();
    }

    public PantryItem copy() {
        return new PantryItem(id, name, category, quantity, unit, status, purchasedAt, note);
    }

    public boolean available() {
        return !STATUS_EMPTY.equals(status);
    }

    public String amountLabel() {
        String amount = (quantity + unit).trim();
        return amount.isEmpty() ? "未记录数量" : amount;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("category", category);
        json.put("quantity", quantity);
        json.put("unit", unit);
        json.put("status", status);
        json.put("purchasedAt", purchasedAt);
        json.put("note", note);
        return json;
    }

    public static PantryItem fromJson(JSONObject json) {
        return new PantryItem(
                json.optString("id"), json.optString("name"), json.optString("category", "其他"),
                json.optString("quantity"), json.optString("unit"),
                json.optString("status", STATUS_FULL), json.optLong("purchasedAt", 0L),
                json.optString("note")
        );
    }
}