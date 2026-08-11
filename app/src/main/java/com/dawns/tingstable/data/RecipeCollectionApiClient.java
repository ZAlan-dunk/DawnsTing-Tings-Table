package com.dawns.tingstable.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.RecipeCollection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** GitHub Contents API client for the shared recipe-collection state file. */
public final class RecipeCollectionApiClient {
    private static final String API_VERSION = "2022-11-28";
    private static final String STATE_PATH = "collections/state.json";
    private static final int MAX_STATE_BYTES = 900_000;
    private static final int MAX_RESPONSE_CHARS = 1_500_000;
    private static final int MAX_WRITE_ATTEMPTS = 5;

    private final String owner;
    private final String repository;
    private final String profileId;
    private final String token;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean closed;

    public RecipeCollectionApiClient(String owner, String repository, String profileId, String token) {
        this.owner = clean(owner);
        this.repository = clean(repository);
        this.profileId = clean(profileId);
        this.token = clean(token);
    }

    public boolean isConfigured() {
        return !owner.isEmpty() && !repository.isEmpty() && profileId.matches("[a-z0-9-]{1,24}")
                && !token.isEmpty();
    }

    public void list(Callback<List<RecipeCollection>> callback) {
        run(callback, () -> readState().state.collections());
    }

    public void get(String collectionId, Callback<RecipeCollection> callback) {
        run(callback, () -> {
            RecipeCollection collection = readState().state.find(collectionId);
            if (collection == null) throw new IllegalStateException("请求的菜谱集不存在");
            return collection;
        });
    }

    public void create(String name, List<Recipe> recipes, Callback<RecipeCollection> callback) {
        run(callback, () -> {
            String cleanName = requireName(name);
            for (int attempt = 0; attempt < MAX_WRITE_ATTEMPTS; attempt++) {
                StateSnapshot snapshot = readState();
                List<Recipe> initial = new ArrayList<>();
                if (!snapshot.state.isLegacyMigrated(profileId)) {
                    initial.addAll(readLegacyRecipes());
                    snapshot.state.markLegacyMigrated(profileId);
                }
                if (recipes != null) initial.addAll(recipes);
                RecipeCollection created = snapshot.state.createStandard(
                        cleanName, RecipeCollectionCloudState.mergeRecipes(initial),
                        System.currentTimeMillis());
                try {
                    writeState(snapshot.state, snapshot.sha, "collections: create " + created.id);
                    return created;
                } catch (StateConflictException ignored) {
                    // Re-read before allocating another number.
                }
            }
            throw new IllegalStateException("编号分配繁忙，请重试");
        });
    }

    public void ensureSpecial(String collectionId, String name, Callback<RecipeCollection> callback) {
        run(callback, () -> {
            String cleanName = requireName(name);
            for (int attempt = 0; attempt < MAX_WRITE_ATTEMPTS; attempt++) {
                StateSnapshot snapshot = readState();
                RecipeCollection existing = snapshot.state.find(collectionId);
                if (existing != null) {
                    if (!RecipeCollection.TYPE_SPECIAL.equals(existing.type)) {
                        throw new IllegalStateException("特典编号已被其他菜谱集占用");
                    }
                    return existing;
                }
                RecipeCollection created = snapshot.state.ensureSpecial(
                        collectionId, cleanName, System.currentTimeMillis());
                try {
                    writeState(snapshot.state, snapshot.sha, "collections: create special " + collectionId);
                    return created;
                } catch (StateConflictException ignored) {
                    // A concurrent create is resolved by reading the latest state.
                }
            }
            throw new IllegalStateException("特典创建繁忙，请重试");
        });
    }

    public void save(RecipeCollection collection, Callback<RecipeCollection> callback) {
        save(collection, false, callback);
    }

    public void forceSave(RecipeCollection collection, Callback<RecipeCollection> callback) {
        save(collection, true, callback);
    }

    private void save(RecipeCollection collection, boolean force, Callback<RecipeCollection> callback) {
        run(callback, () -> {
            for (int attempt = 0; attempt < MAX_WRITE_ATTEMPTS; attempt++) {
                StateSnapshot snapshot = readState();
                RecipeCollection updated = snapshot.state.applyUpdate(
                        collection, force, System.currentTimeMillis());
                try {
                    writeState(snapshot.state, snapshot.sha, "collections: update " + collection.id);
                    return updated;
                } catch (StateConflictException ignored) {
                    // Re-read so unrelated writes can be merged and same-record writes become conflicts.
                }
            }
            throw new IllegalStateException("VERSION_CONFLICT");
        });
    }

    public void shutdown() {
        closed = true;
        executor.shutdownNow();
    }

    private StateSnapshot readState() throws Exception {
        ensureConfigured();
        Response response = request("GET", contentsUrl(STATE_PATH) + "?ref=main", null);
        if (response.code == 404) return new StateSnapshot(RecipeCollectionCloudState.empty(), "");
        if (response.code != 200) throw apiError(response);
        JSONObject item = new JSONObject(response.body);
        String raw = decodeContent(item.optString("content"));
        return new StateSnapshot(RecipeCollectionCloudState.fromJson(new JSONObject(raw)),
                item.optString("sha"));
    }

    private List<Recipe> readLegacyRecipes() throws Exception {
        Response response = request("GET", contentsUrl("backups/" + profileId + "/latest.json")
                + "?ref=main", null);
        if (response.code == 404) return new ArrayList<>();
        if (response.code != 200) throw apiError(response);
        JSONObject backup = new JSONObject(decodeContent(
                new JSONObject(response.body).optString("content")));
        List<Recipe> result = new ArrayList<>();
        JSONArray array = backup.optJSONArray("customRecipes");
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) result.add(Recipe.fromJson(item));
            }
        }
        return RecipeCollectionCloudState.mergeRecipes(result);
    }

    private void writeState(RecipeCollectionCloudState state, String sha, String message)
            throws Exception {
        String raw = state.toJson().toString(2) + "\n";
        byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_STATE_BYTES) {
            throw new IllegalStateException("云端菜谱集数据已达到容量上限");
        }
        JSONObject body = new JSONObject();
        body.put("message", message);
        body.put("branch", "main");
        body.put("content", Base64.encodeToString(bytes, Base64.NO_WRAP));
        if (sha != null && !sha.isEmpty()) body.put("sha", sha);
        Response response = request("PUT", contentsUrl(STATE_PATH), body.toString());
        if (response.code == 409 || response.code == 422) throw new StateConflictException();
        if (response.code != 200 && response.code != 201) throw apiError(response);
    }

    private String decodeContent(String encoded) {
        byte[] decoded = Base64.decode(encoded == null ? "" : encoded.replace("\n", ""), Base64.DEFAULT);
        if (decoded.length > MAX_STATE_BYTES) {
            throw new IllegalStateException("云端菜谱集数据已达到容量上限");
        }
        return new String(decoded, StandardCharsets.UTF_8);
    }

    private String contentsUrl(String path) {
        return "https://api.github.com/repos/" + owner + "/" + repository + "/contents/" + path;
    }

    private Response request(String method, String endpoint, String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        try {
            connection.setRequestMethod(method);
            connection.setConnectTimeout(15_000);
            connection.setReadTimeout(20_000);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setRequestProperty("X-GitHub-Api-Version", API_VERSION);
            connection.setRequestProperty("User-Agent", "LazySheepChef-RecipeCollections");
            if (body != null) {
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }
            }
            int code = connection.getResponseCode();
            InputStream input = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
            return new Response(code, read(input));
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
                if (value.length() > MAX_RESPONSE_CHARS) {
                    throw new IllegalStateException("云端菜谱集数据过大");
                }
            }
        }
        return value.toString();
    }

    private void ensureConfigured() {
        if (!isConfigured()) throw new IllegalStateException("菜谱集云端尚未配置");
    }

    private Exception apiError(Response response) {
        if (response.code == 401 || response.code == 403) {
            return new IllegalStateException("菜谱集云端授权无效");
        }
        try {
            String message = new JSONObject(response.body).optString("message");
            if (!message.isEmpty()) {
                return new IllegalStateException("云端返回 " + response.code + "：" + message);
            }
        } catch (Exception ignored) { }
        return new IllegalStateException("云端返回错误 " + response.code);
    }

    private String userMessage(Exception error) {
        if (error instanceof UnknownHostException) return "网络连接失败，请检查网络后重试";
        if (error instanceof SocketTimeoutException) return "网络连接超时，请稍后重试";
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? "云端操作失败，请稍后重试" : message;
    }

    private <T> void run(Callback<T> callback, Task<T> task) {
        executor.execute(() -> {
            try {
                T value = task.run();
                if (!closed) main.post(() -> callback.onSuccess(value));
            } catch (Exception error) {
                String message = userMessage(error);
                if (!closed) main.post(() -> callback.onError(message));
            }
        });
    }

    private static String requireName(String value) {
        String result = clean(value);
        if (result.isEmpty()) throw new IllegalArgumentException("请输入菜谱集名称");
        if (result.length() > 40) throw new IllegalArgumentException("菜谱集名称不能超过 40 个字");
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

    private static final class StateSnapshot {
        final RecipeCollectionCloudState state;
        final String sha;

        StateSnapshot(RecipeCollectionCloudState state, String sha) {
            this.state = state;
            this.sha = sha == null ? "" : sha;
        }
    }

    private static final class StateConflictException extends Exception { }

    private static final class Response {
        final int code;
        final String body;

        Response(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }
}
