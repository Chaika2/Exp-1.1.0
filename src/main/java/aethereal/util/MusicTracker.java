package aethereal.util;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import aethereal.type.Mc;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

public final class MusicTracker {
    public static final MusicTracker INSTANCE = new MusicTracker();

    private static final Random RANDOM = new Random();

    private final Thread thread;
    private volatile IMediaSession session;
    private volatile int mediaColor = 0xFFFFFFFF;
    private volatile String lastTrack = "";
    private final Map<Integer, Identifier> textureCache = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> colorCache = new ConcurrentHashMap<>();

    private MusicTracker() {
        this.thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(200L);
                    this.poll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Throwable ignored) {
                }
            }
        }, "aethereal-music-tracker");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    private void poll() {
        try {
            List<IMediaSession> sessions = MediaPlayerInfo.INSTANCE.getMediaSessions();
            this.session = sessions.stream()
                    .filter(s -> {
                        MediaInfo media = s.getMedia();
                        return media != null
                                && media.getTitle() != null && !media.getTitle().isEmpty()
                                && media.getArtist() != null && !media.getArtist().isEmpty();
                    })
                    .findFirst()
                    .orElse(null);
            if (this.session != null) {
                this.lastTrack = this.session.getMedia().getArtist() + " - " + this.session.getMedia().getTitle();
            }
        } catch (Throwable ignored) {
        }
    }

    public boolean haveActiveSession() {
        return this.session != null;
    }

    public IMediaSession getSession() {
        return this.session;
    }

    public int getMediaColor() {
        return this.mediaColor;
    }

    public Identifier getImage() {
        try {
            IMediaSession current = this.session;
            if (current == null || current.getMedia() == null) {
                return null;
            }
            byte[] artwork = current.getMedia().getArtworkPng();
            if (artwork == null || artwork.length == 0) {
                return null;
            }
            int hash = java.util.Arrays.hashCode(artwork);
            Identifier cached = this.textureCache.get(hash);
            if (cached != null) {
                this.mediaColor = this.colorCache.getOrDefault(hash, 0xFFFFFFFF);
                return cached;
            }
            if (this.textureCache.size() > 8) {
                this.textureCache.clear();
                this.colorCache.clear();
            }
            NativeImage image = NativeImage.read(artwork);
            this.mediaColor = this.averageColor(image);
            this.colorCache.put(hash, this.mediaColor);
            Identifier identifier = Identifier.of("aethereal", "music/" + randomString());
            Mc.INSTANCE.getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(image));
            this.textureCache.put(hash, identifier);
            return identifier;
        } catch (Throwable throwable) {
            return null;
        }
    }

    private int averageColor(NativeImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        long totalR = 0L;
        long totalG = 0L;
        long totalB = 0L;
        int sampled = 0;
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                int argb = image.getColorArgb(x, y);
                if (((argb >> 24) & 0xFF) != 0) {
                    totalR += (argb >> 16) & 0xFF;
                    totalG += (argb >> 8) & 0xFF;
                    totalB += argb & 0xFF;
                    sampled++;
                }
            }
        }
        if (sampled == 0) {
            return 0xFFFFFFFF;
        }
        int r = (int) Math.min(255L, (totalR / sampled) + 50L);
        int g = (int) Math.min(255L, (totalG / sampled) + 50L);
        int b = (int) Math.min(255L, (totalB / sampled) + 50L);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static String randomString() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            sb.append((char) (97 + RANDOM.nextInt(26)));
        }
        return sb.toString();
    }
}
