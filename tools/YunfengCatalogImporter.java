import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class YunfengCatalogImporter {
    private static final String HOST = "https://m.xiachufang.com";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124.0 Mobile Safari/537.36";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: <mobile-list-url> <output-json>");
        }
        String listUrl = args[0];
        if (!listUrl.startsWith(HOST + "/recipe_list/")) {
            throw new IllegalArgumentException("Only m.xiachufang.com recipe lists are supported");
        }

        List<Map<String, String>> recipes = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (int page = 1; page <= 100; page++) {
            String pageUrl = page == 1 ? listUrl : listUrl + "?page=" + page;
            Document document = Jsoup.connect(pageUrl)
                    .userAgent(USER_AGENT)
                    .referrer(HOST + "/")
                    .timeout(30_000)
                    .get();
            Elements articles = document.select("article.recipe-332-horizon");
            if (articles.isEmpty()) break;
            for (Element article : articles) {
                Element link = article.selectFirst("a.click-expand[href^=/recipe/]");
                Element title = article.selectFirst(".recipe-name");
                Element cover = article.selectFirst(".cover img[src]");
                if (link == null || title == null || cover == null) continue;
                String href = link.attr("href").split("\\?")[0];
                String id = href.replaceAll("\\D", "");
                if (id.isEmpty() || !seen.add(id)) continue;
                Map<String, String> recipe = new LinkedHashMap<>();
                recipe.put("id", id);
                recipe.put("title", title.text().trim());
                recipe.put("sourceUrl", HOST + (href.endsWith("/") ? href : href + "/"));
                recipe.put("coverUrl", cover.absUrl("src"));
                recipes.add(recipe);
            }
            String next = "/recipe_list/" + listId(listUrl) + "/?page=" + (page + 1);
            if (document.selectFirst("a[href='" + next + "']") == null) break;
        }
        if (recipes.isEmpty()) throw new IllegalStateException("No recipes were found");

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("source", listUrl);
        root.put("retrievedAt", LocalDate.now().toString());
        root.put("count", recipes.size());
        root.put("recipes", recipes);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Path output = Path.of(args[1]).toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        Files.writeString(output, gson.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
        System.out.println("WROTE=" + output);
        System.out.println("COUNT=" + recipes.size());
    }

    private static String listId(String listUrl) {
        String[] parts = listUrl.replaceAll("/+$", "").split("/");
        return parts[parts.length - 1];
    }
}
