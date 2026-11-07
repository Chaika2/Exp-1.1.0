package aethereal.render;
import aethereal.resource.ByteArrayResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public final class AvatarCache {
    public static final ConcurrentHashMap<String, GlTexture> textureCache = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, CompletableFuture<GlTexture>> pendingLoads = new ConcurrentHashMap<>();
    public static final int connectTimeoutMs = 5000;
    public static final int readTimeoutMs = 10000;
    public static final int maxSizeBytes = 5242880;

    public static CompletableFuture<GlTexture> load(String str, GlTexture class073Var, Executor executor) {
        if (str == null || str.isBlank() || str.endsWith("/null")) {
            return CompletableFuture.completedFuture(class073Var);
        }
        GlTexture class073Var2 = textureCache.get(str);
        return class073Var2 != null ? CompletableFuture.completedFuture(class073Var2) : pendingLoads.computeIfAbsent(str, str2 -> {
            return CompletableFuture.supplyAsync(() -> {
                return loadWithRetry(str2);
            }, executor).thenApply(class073Var3 -> {
                if (class073Var3 != null) {
                    textureCache.put(str2, class073Var3);
                }
                return class073Var3;
            }).handle((class073Var4, th) -> {
                pendingLoads.remove(str2);
                if (th != null) {
                    System.out.println("[AvatarCache] FAIL url=" + str2 + " err=" + String.valueOf(th.getCause()));
                }
                return (th != null || class073Var4 == null) ? class073Var : class073Var4;
            });
        });
    }

    public static GlTexture loadWithRetry(String str) {
        RuntimeException runtimeException = null;
        for (int i = 1; i <= 5; i++) {
            try {
                return download(str);
            } catch (RuntimeException e) {
                runtimeException = e;
                if (!isRetryable(e)) {
                    throw e;
                }
                try {
                    Thread.sleep(500 << (i - 1));
                } catch (InterruptedException e2) {
                }
                System.out.println("[AvatarCache] RETRY " + i + "/5 url=" + str + " cause=" + String.valueOf(e.getCause()));
            }
        }
        throw runtimeException;
    }

    public static boolean isRetryable(Throwable th) {
        Throwable cause = th;
        while (true) {
            Throwable th2 = cause;
            if (th2 == null) {
                return false;
            }
            if ((th2 instanceof SocketTimeoutException) || (th2 instanceof ConnectException) || (th2 instanceof UnknownHostException)) {
                return true;
            }
            cause = th2.getCause();
        }
    }

    public static GlTexture download(String str) {
        HttpURLConnection httpURLConnection = null;
        try {
            try {
                HttpURLConnection httpURLConnection2 = (HttpURLConnection) URI.create(str).toURL().openConnection();
                httpURLConnection2.setConnectTimeout(connectTimeoutMs);
                httpURLConnection2.setReadTimeout(readTimeoutMs);
                httpURLConnection2.setRequestProperty("User-Agent", "Mozilla/5.0");
                httpURLConnection2.setInstanceFollowRedirects(true);
                int responseCode = httpURLConnection2.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("[AvatarCache] HTTP " + responseCode + " url=" + str + " location=" + httpURLConnection2.getHeaderField("Location") + " contentType=" + httpURLConnection2.getContentType());
                    throw new RuntimeException("HTTP " + responseCode);
                }
                InputStream inputStream = httpURLConnection2.getInputStream();
                try {
                    GlTexture class073Var = new GlTexture(new ByteArrayResource(readAllBytes(inputStream)));
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                    return class073Var;
                } catch (Throwable th) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable th2) {
                            th.addSuppressed(th2);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                if (0 != 0) {
                    httpURLConnection.disconnect();
                }
                throw th3;
            }
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(8192);
        byte[] bArr = new byte[8192];
        int i = 0;
        while (true) {
            int i2 = inputStream.read(bArr);
            if (i2 == -1) {
                return byteArrayOutputStream.toByteArray();
            }
            i += i2;
            if (i > maxSizeBytes) {
                throw new RuntimeException("Avatar too large: " + i);
            }
            byteArrayOutputStream.write(bArr, 0, i2);
        }
    }
}
