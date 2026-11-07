package aethereal.config;
import aethereal.model.ModuleConfigData;
import aethereal.model.ModuleConfigEntry;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ConfigFile {

    @SerializedName("version")
    public int version;

    @SerializedName("avatarUrl")
    public String avatarUrl;

    @SerializedName("modules")
    public List<ModuleConfigData> modules = new ArrayList();

    @SerializedName("widgets")
    public List<ModuleConfigEntry> widgets = new ArrayList();

    public int version() {
        return this.version;
    }

    public String avatarUrl() {
        return this.avatarUrl;
    }

    public List<ModuleConfigData> modules() {
        return this.modules;
    }

    public List<ModuleConfigEntry> widgets() {
        return this.widgets;
    }

    public ConfigFile version(int i) {
        this.version = i;
        return this;
    }

    public ConfigFile avatarUrl(String str) {
        this.avatarUrl = str;
        return this;
    }

    public ConfigFile modules(List<ModuleConfigData> list) {
        this.modules = list;
        return this;
    }

    public ConfigFile widgets(List<ModuleConfigEntry> list) {
        this.widgets = list;
        return this;
    }
}
