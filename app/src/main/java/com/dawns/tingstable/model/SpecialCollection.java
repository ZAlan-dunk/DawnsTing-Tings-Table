package com.dawns.tingstable.model;

import java.util.ArrayList;
import java.util.List;

public class SpecialCollection {
    public final String id;
    public final String title;
    public final String subtitle;
    public final String quote;
    public final List<SpecialRecipe> recipes;

    public SpecialCollection(String id, String title, String subtitle, String quote, List<SpecialRecipe> recipes) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.quote = quote;
        this.recipes = recipes == null ? new ArrayList<>() : new ArrayList<>(recipes);
    }
}
