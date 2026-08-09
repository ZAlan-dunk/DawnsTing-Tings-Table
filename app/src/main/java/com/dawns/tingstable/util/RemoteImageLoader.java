package com.dawns.tingstable.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RemoteImageLoader implements AutoCloseable {
    private static final String COVER_HOST = "i2.chuimg.com";
    private static final int MAX_DOWNLOAD_BYTES = 5 * 1024 * 1024;
    private static final long MAX_DISK_CACHE_BYTES = 24L * 1024L * 1024L;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final LruCache<String, Bitmap> memoryCache;
    private final File diskCache;
    private volatile boolean closed;

    public RemoteImageLoader(Context context) {
        int availableKb = (int) Math.min(24L * 1024L,
                Runtime.getRuntime().maxMemory() / 1024L / 10L);
        memoryCache = new LruCache<String, Bitmap>(Math.max(4 * 1024, availableKb)) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return Math.max(1, bitmap.getByteCount() / 1024);
            }
        };
        diskCache = new File(context.getCacheDir(), "yunfeng-covers");
        if (!diskCache.exists()) diskCache.mkdirs();
        executor.execute(this::trimDiskCache);
    }

    public void load(ImageView target, String sourceUrl) {
        if (closed || !isAllowedSource(sourceUrl)) return;
        target.setTag(sourceUrl);
        Bitmap cached = memoryCache.get(sourceUrl);
        if (cached != null) {
            showIfCurrent(target, sourceUrl, cached);
            return;
        }
        executor.execute(() -> {
            Bitmap bitmap = loadBitmap(sourceUrl);
            if (closed || bitmap == null) return;
            memoryCache.put(sourceUrl, bitmap);
            mainHandler.post(() -> showIfCurrent(target, sourceUrl, bitmap));
        });
    }

    public void clear(ImageView target) {
        target.setTag(null);
    }

    public static boolean isAllowedSource(String value) {
        try {
            URI uri = new URI(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && COVER_HOST.equalsIgnoreCase(uri.getHost())
                    && uri.getUserInfo() == null
                    && uri.getPort() == -1;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Bitmap loadBitmap(String sourceUrl) {
        File cachedFile = new File(diskCache, cacheKey(sourceUrl) + ".img");
        Bitmap cached = decodeFile(cachedFile);
        if (cached != null) return cached;

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(sourceUrl).openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(12000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "image/*");
            connection.setRequestProperty("User-Agent", "DawnsTingTingsTable Android");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
            String contentType = connection.getContentType();
            if (contentType == null || !contentType.toLowerCase(java.util.Locale.ROOT).startsWith("image/")) {
                return null;
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int total = 0;
            try (java.io.InputStream input = connection.getInputStream()) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_DOWNLOAD_BYTES) return null;
                    output.write(buffer, 0, read);
                }
            }
            byte[] bytes = output.toByteArray();
            Bitmap bitmap = decodeBytes(bytes);
            if (bitmap == null) return null;
            try (FileOutputStream file = new FileOutputStream(cachedFile)) {
                file.write(bytes);
            }
            trimDiskCache();
            return bitmap;
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private Bitmap decodeFile(File file) {
        if (!file.isFile() || file.length() <= 0 || file.length() > MAX_DOWNLOAD_BYTES) return null;
        try (FileInputStream input = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int offset = 0;
            while (offset < bytes.length) {
                int read = input.read(bytes, offset, bytes.length - offset);
                if (read == -1) break;
                offset += read;
            }
            if (offset != bytes.length) return null;
            return decodeBytes(bytes);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Bitmap decodeBytes(byte[] bytes) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;
        int sampleSize = 1;
        while (bounds.outWidth / sampleSize > 720 || bounds.outHeight / sampleSize > 720) {
            sampleSize *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    private void showIfCurrent(ImageView target, String sourceUrl, Bitmap bitmap) {
        if (closed || !sourceUrl.equals(target.getTag())) return;
        target.setPadding(0, 0, 0, 0);
        target.setScaleType(ImageView.ScaleType.CENTER_CROP);
        target.setImageTintList(null);
        target.setImageBitmap(bitmap);
    }

    private String cacheKey(String sourceUrl) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(sourceUrl.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : bytes) result.append(String.format("%02x", value));
            return result.toString();
        } catch (Exception ignored) {
            return Integer.toHexString(sourceUrl.hashCode());
        }
    }

    private synchronized void trimDiskCache() {
        File[] files = diskCache.listFiles(File::isFile);
        if (files == null) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        long total = 0;
        for (File file : files) total += file.length();
        for (File file : files) {
            if (total <= MAX_DISK_CACHE_BYTES) break;
            long size = file.length();
            if (file.delete()) total -= size;
        }
    }

    @Override
    public void close() {
        closed = true;
        mainHandler.removeCallbacksAndMessages(null);
        executor.shutdownNow();
        memoryCache.evictAll();
    }
}
