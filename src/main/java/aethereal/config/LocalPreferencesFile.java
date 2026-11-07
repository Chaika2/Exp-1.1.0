package aethereal.config;
import aethereal.util.AtomicFileWriter;
import aethereal.Expensive;
import aethereal.type.Language;
import aethereal.model.PreferencesData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

public class LocalPreferencesFile {
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public final Path filePath;
    public PreferencesData data = new PreferencesData();

    public void load() {
        if (Files.exists(this.filePath, new LinkOption[0])) {
            PreferencesData class083Var;
            try {
                class083Var = (PreferencesData) gson.fromJson(Files.readString(this.filePath, StandardCharsets.UTF_8), PreferencesData.class);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
            if (class083Var != null) {
                this.data = class083Var;
                ensureCollections();
                applyLanguage();
                applyDpiScale();
            }
        }
    }

    public void save() throws IOException {
        ensureCollections();
        AtomicFileWriter.writeBytes(this.filePath, gson.toJson(this.data).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    public boolean isModuleFavorite(String str) {
        if (str == null) {
            return false;
        }
        ensureCollections();
        return this.data.favoriteModules.contains(str);
    }

    public void setModuleFavorite(String str, boolean z) {
        if (str == null) {
            return;
        }
        ensureCollections();
        if (z) {
            this.data.favoriteModules.add(str);
        } else {
            this.data.favoriteModules.remove(str);
        }
    }

    public boolean isConfigFavorite(String str) {
        if (str == null) {
            return false;
        }
        ensureCollections();
        return this.data.favoriteConfigs.contains(str);
    }

    public void setConfigFavorite(String str, boolean z) {
        if (str == null) {
            return;
        }
        ensureCollections();
        if (z) {
            this.data.favoriteConfigs.add(str);
        } else {
            this.data.favoriteConfigs.remove(str);
        }
    }

    public void setLanguage(Language class313Var) {
        if (class313Var == null) {
            return;
        }
        this.data.language = class313Var.name();
    }

    public void setDpiScale(boolean z, float f) {
        this.data.autoDpiScale = Boolean.valueOf(z);
        this.data.dpiScaleFactor = Float.valueOf(f);
    }

    public void setLastType(String str) {
        if (str == null || str.isBlank()) {
            return;
        }
        this.data.lastType = str;
    }

    public String loadLastType() {
        if (this.data.lastType == null || this.data.lastType.isBlank()) {
            return "Crack";
        }
        return this.data.lastType;
    }

    public int loadMainMenuWallpaper() {
        if (this.data.mainMenuWallpaper == null) {
            return 0;
        }
        return this.data.mainMenuWallpaper.intValue();
    }

    public void setMainMenuWallpaper(int i) {
        this.data.mainMenuWallpaper = Integer.valueOf(i);
    }

    public void applyLanguage() {
        if (this.data.language == null || this.data.language.isBlank()) {
            return;
        }
        try {
            Expensive.INSTANCE.languages().language(Language.valueOf(this.data.language));
        } catch (IllegalArgumentException e) {
            Expensive.LOGGER.warn("Unknown saved language: {}", this.data.language);
        }
    }

    public void applyDpiScale() {
        if (this.data.autoDpiScale == null && this.data.dpiScaleFactor == null) {
            return;
        }
        if (Boolean.TRUE.equals(this.data.autoDpiScale)) {
            Expensive.INSTANCE.windowController().enableAutoDpiScale();
        } else if (this.data.dpiScaleFactor != null) {
            Expensive.INSTANCE.windowController().setManualDpiScaleFactor(this.data.dpiScaleFactor.floatValue());
        }
    }

    public boolean isAutoSaveDisabled(String str) {
        if (str == null) {
            return false;
        }
        ensureCollections();
        return this.data.autoSaveDisabled.contains(str);
    }

    public void setAutoSaveDisabled(String str, boolean z) {
        if (str == null) {
            return;
        }
        ensureCollections();
        if (z) {
            this.data.autoSaveDisabled.add(str);
        } else {
            this.data.autoSaveDisabled.remove(str);
        }
    }

    public void ensureCollections() {
        if (this.data.favoriteModules == null) {
            this.data.favoriteModules = new HashSet();
        }
        if (this.data.favoriteConfigs == null) {
            this.data.favoriteConfigs = new HashSet();
        }
        if (this.data.autoSaveDisabled == null) {
            this.data.autoSaveDisabled = new HashSet();
        }
    }

    public LocalPreferencesFile(Path path) {
        this.filePath = path;
    }
}
