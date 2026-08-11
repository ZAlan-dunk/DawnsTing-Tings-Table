package com.dawns.tingstable.data;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.dawns.tingstable.model.RecipeCollection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RecipeCollectionApiClient {
    private static final int MAX_RESPONSE_CHARS = 1_500_000;

    private final String baseUrl;
    private final String profileId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean closed;

    public RecipeCollectionApiClient(String baseUrl, String profileId) {
        this.baseUrl = trimSlash(baseUrl);
        this.profileId = clean(profileId);
    }

    public boolean isConfigured() {
        return baseUrl.startsWith("https://") && !profileId.isEmpty();
    }

    public void list(Callback<List<RecipeCollection>> callback) {
        run(callback, () -> {
            JSONObject response = request("GET", "/v1/collections", null, null);
            List<RecipeCollection> collections = new ArrayList<>();
            JSONArray array = response.optJSONArray("collections");
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.optJSONObject(i);
                    if (item != null) collections.add(RecipeCollection.fromJson(item));
                }
            }
            return collections;
        });
    }

    public void get(String collectionId, String token, Callback<RecipeCollection> callback) {
        run(callback, () -> RecipeCollection.fromJson(request(
                "GET", "/v1/collections/" + Uri.encode(collectionId), token, null
        ).getJSONObject("collection")));
    }

    public void create(String name, List<com.dawns.tingstable.model.Recipe> recipes,
                       Callback<AccessResult> callback) {
        run(callback, () -> {
            JSONObject body = new JSONObject();
            body.put("name", clean(name));
            body.put("profileId", profileId);
            JSONArray items = new JSONArray();
            if (recipes != null) {
                for (com.dawns.tingstable.model.Recipe recipe : recipes) items.put(recipe.toJson());
            }
            body.put("recipes", items);
            return AccessResult.fromJson(request("POST", "/v1/collections", null, body));
        });
    }

    public void unlockSpecial(String key, Callback<AccessResult> callback) {
        run(callback, () -> {
            JSONObject body = new JSONObject();
            body.put("key", key == null ? "" : key);
            body.put("profileId", profileId);
            return AccessResult.fromJson(request("POST", "/v1/special/unlock", null, body));
        });
    }

    public void recover(String collectionId, String recoveryCode, Callback<AccessResult> callback) {
        run(callback, () -> {
            JSONObject body = new JSONObject();
            body.put("collectionId", clean(collectionId));
            body.put("recoveryCode", recoveryCode == null ? "" : recoveryCode.trim());
            body.put("profileId", profileId);
            return AccessResult.fromJson(request("POST", "/v1/collections/recover", null, body));
        });
    }

    public void save(RecipeCollection collection, String token, Callback<RecipeCollection> callback) {
        save(collection, token, false, callback);
    }

    public void forceSave(RecipeCollection collection, String token, Callback<RecipeCollection> callback) {
        save(collection, token, true, callback);
    }

    private void save(RecipeCollection collection, String token, boolean force,
                      Callback<RecipeCollection> callback) {
        run(callback, () -> RecipeCollection.fromJson(request(
                "PUT", "/v1/collections/" + Uri.encode(collection.id) + (force ? "?force=true" : ""),
                token, collection.toJson()
        ).getJSONObject("collection")));
    }

    public void shutdown() {
        closed = true;
        executor.shutdownNow();
    }

    private JSONObject request(String method, String path, String token, JSONObject body) throws Exception {
        if (!isConfigured()) throw new IllegalStateException("菜谱集服务尚未配置");
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(15_000);
            connection.setReadTimeout(20_000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "LazySheepChef-RecipeCollections");
            if (token != null && !token.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + token.trim());
            }
            if (body != null) {
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream output = connection.getOutputStream()) { output.write(bytes); }
            }
            int status = connection.getResponseCode();
            InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String raw = read(input);
            JSONObject response = raw.isEmpty() ? new JSONObject() : new JSONObject(raw);
            if (status < 200 || status >= 300) {
                String message = response.optString("message", "菜谱集服务请求失败（" + status + "）");
                throw new IllegalStateException(message);
            }
            return response;
        } finally {
            connection.disconnect();
        }
    }

    private String read(InputStream input) throws Exception {
        if (input == null) return "";
        StringBuilder value = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                value.append(line);
                if (value.length() > MAX_RESPONSE_CHARS) throw new IllegalStateException("云端菜谱集数据过大");
            }
        }
        return value.toString();
    }

    private <T> void run(Callback<T> callback, Task<T> task) {
        executor.execute(() -> {
            try {
                T value = task.run();
                if (!closed) main.post(() -> callback.onSuccess(value));
            } catch (Exception error) {
                String message = error.getMessage();
                if (!closed) main.post(() -> callback.onError(
                        message == null || message.trim().isEmpty() ? "操作失败，请稍后重试" : message));
            }
        });
    }

    private static String trimSlash(String value) {
        String result = clean(value);
        while (result.endsWith("/")) result = result.substring(0, result.length() - 1);
        return result;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public interface Callback<T> {
        void onSuccess(T value);
        void onError(String message);
    }

    private interface Task<T> { T run() throws Exception; }

    public static final class AccessResult {
        public final RecipeCollection collection;
        public final String accessToken;
        public final String recoveryCode;

        AccessResult(RecipeCollection collection, String accessToken, String recoveryCode) {
            this.collection = collection;
            this.accessToken = clean(accessToken);
            this.recoveryCode = clean(recoveryCode);
        }

        static AccessResult fromJson(JSONObject json) {
            return new AccessResult(
                    RecipeCollection.fromJson(json.optJSONObject("collection")),
                    json.optString("accessToken"), json.optString("recoveryCode")
            );
        }
    }
}
