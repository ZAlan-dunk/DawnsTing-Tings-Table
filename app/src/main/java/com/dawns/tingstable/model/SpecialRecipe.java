package com.dawns.tingstable.model;

import java.net.URI;

public final class SpecialRecipe {
    private static final String RECIPE_HOST = "m.xiachufang.com";

    public final String id;
    public final String title;
    public final String sourceUrl;
    public final String coverUrl;

    public SpecialRecipe(String id, String title, String sourceUrl, String coverUrl) {
        this.id = id == null ? "" : id.trim();
        this.title = title == null ? "" : title.trim();
        this.sourceUrl = sourceUrl == null ? "" : sourceUrl.trim();
        this.coverUrl = coverUrl == null ? "" : coverUrl.trim();
    }

    public boolean isValid() {
        return !id.isEmpty() && !title.isEmpty() && isAllowedRecipeUrl(sourceUrl);
    }

    public static boolean isAllowedRecipeUrl(String value) {
        try {
            URI uri = new URI(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && RECIPE_HOST.equalsIgnoreCase(uri.getHost())
                    && uri.getUserInfo() == null
                    && uri.getPort() == -1
                    && uri.getQuery() == null
                    && uri.getPath() != null
                    && uri.getPath().matches("/recipe/\\d+/?");
        } catch (Exception ignored) {
            return false;
        }
    }
}
