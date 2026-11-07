package aethereal.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public interface AssetSource {
    public static final String AA_PATH = "aa/";
    public static final String SHADERS_PATH = "shaders/";
    public static final String TEXTURES_PATH = "textures/";
    public static final String ICONS_PATH = "icons/";
    public static final String LANGUAGES_PATH = "langs/";

    InputStream inputStream();

    default byte[] bytes() {
        try (InputStream inputStream = inputStream()) {
            return inputStream.readAllBytes();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    default String string() {
        return new String(bytes(), StandardCharsets.UTF_8);
    }

    default BufferedImage image() {
        try (InputStream inputStream = inputStream()) {
            return ImageIO.read(inputStream);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    static AssetSource fromAssets(String str) {
        return () -> {
            return AssetSource.class.getResourceAsStream("/assets/expensive/" + str);
        };
    }

    static AssetSource fromLanguages(String str) {
        return fromAssets("langs/" + str);
    }

    static AssetSource fromFiles(String str) {
        return () -> {
            try {
                return new ByteArrayInputStream(Files.readAllBytes(Path.of(str, new String[0])));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    static AssetSource fromShaders(String str) {
        return fromAssets("shaders/" + str);
    }

    static AssetSource fromTextures(String str) {
        return fromAssets("textures/" + str);
    }

    static AssetSource fromIcons(String str) {
        return fromAssets("icons/" + str);
    }

    static AssetSource fromFonts(String str) {
        return fromFiles(str);
    }

    static AssetSource fromAA(String str) {
        return fromAssets("aa/" + str);
    }
}
