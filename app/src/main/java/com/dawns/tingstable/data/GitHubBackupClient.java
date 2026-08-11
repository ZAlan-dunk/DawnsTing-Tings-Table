package com.dawns.tingstable.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small GitHub Contents API client for one versioned backup file per profile. */
public final class GitHubBackupClient {
    private static final int MAX_BACKUP_BYTES = 900_000;
    private static final String API_VERSION = "2022-11-28";

    public interface Callback<T> {
        void onSuccess(T value);
        void onError(String message);
    }

    private final String owner;
    private final String repository;
    private final String profileId;
    private final String token;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean closed;

    public GitHubBackupClient(String owner, String repository, String profileId, String token) {
        this.owner = owner;
        this.repository = repository;
        this.profileId = profileId;
        this.token = token == null ? "" : token.trim();
    }

    public boolean isConfigured() {
        return !owner.isEmpty() && !repository.isEmpty() && !profileId.isEmpty() && !token.isEmpty();
    }

    public String contentPath() {
        return "backups/" + profileId + "/latest.json";
    }

    public void upload(String raw, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                ensureConfigured();
                byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
                if (bytes.length > MAX_BACKUP_BYTES) throw new IllegalStateException("备份文件过大");
                String sha = currentSha();
                JSONObject request = new JSONObject();
                request.put("message", "backup: update " + profileId);
                request.put("branch", "main");
                request.put("content", Base64.encodeToString(bytes, Base64.NO_WRAP));
                if (!sha.isEmpty()) request.put("sha", sha);
                Response response = request("PUT", apiUrl(), request.toString());
                if (response.code != 200 && response.code != 201) throw apiError(response);
                long completedAt = System.currentTimeMillis();
                postSuccess(callback, completedAt);
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void download(Callback<String> callback) {
        executor.execute(() -> {
            try {
                ensureConfigured();
                Response response = request("GET", apiUrl() + "?ref=main", null);
                if (response.code == 404) throw new IllegalStateException("云端还没有备份");
                if (response.code != 200) throw apiError(response);
                JSONObject json = new JSONObject(response.body);
                String encoded = json.optString("content").replace("\n", "");
                byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
                if (decoded.length > MAX_BACKUP_BYTES) throw new IllegalStateException("云端备份文件过大");
                String raw = new String(decoded, StandardCharsets.UTF_8);
                postSuccess(callback, raw);
            } catch (Exception error) {
                postError(callback, error);
            }
        });
    }

    public void shutdown() {
        closed = true;
        executor.shutdownNow();
    }

    private String currentSha() throws Exception {
        Response response = request("GET", apiUrl() + "?ref=main", null);
        if (response.code == 404) return "";
        if (response.code != 200) throw apiError(response);
        return new JSONObject(response.body).optString("sha");
    }

    private String apiUrl() {
        return "https://api.github.com/repos/" + owner + "/" + repository + "/contents/" + contentPath();
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
            connection.setRequestProperty("User-Agent", "LazySheepChef-CloudBackup");
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
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private void ensureConfigured() {
        if (!isConfigured()) throw new IllegalStateException("云备份尚未配置");
    }

    private Exception apiError(Response response) {
        if (response.code == 401 || response.code == 403) {
            return new IllegalStateException("云备份授权无效");
        }
        if (response.code == 409 || response.code == 422) {
            return new IllegalStateException("云端备份冲突，请稍后重试");
        }
        try {
            String message = new JSONObject(response.body).optString("message");
            if (!message.isEmpty()) return new IllegalStateException("云端返回 " + response.code + "：" + message);
        } catch (Exception ignored) { }
        return new IllegalStateException("云端返回错误 " + response.code);
    }

    private String userMessage(Exception error) {
        if (error instanceof UnknownHostException) return "网络连接失败，请检查网络后重试";
        if (error instanceof SocketTimeoutException) return "网络连接超时，请稍后重试";
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? "网络请求失败" : message;
    }

    private <T> void postSuccess(Callback<T> callback, T value) {
        if (closed) return;
        main.post(() -> {
            if (!closed) callback.onSuccess(value);
        });
    }

    private void postError(Callback<?> callback, Exception error) {
        if (closed) return;
        String message = userMessage(error);
        main.post(() -> {
            if (!closed) callback.onError(message);
        });
    }

    private static final class Response {
        final int code;
        final String body;

        Response(int code, String body) {
            this.code = code;
            this.body = body;
        }
    }
}
