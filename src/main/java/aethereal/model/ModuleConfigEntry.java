package aethereal.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ModuleConfigEntry {

    @SerializedName("name")
    public String name;

    @SerializedName("x")
    public float x;

    @SerializedName("y")
    public float y;

    @SerializedName("settings")
    public List<LayoutNode> settings = new ArrayList();

    public String name() {
        return this.name;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    public List<LayoutNode> settings() {
        return this.settings;
    }

    public ModuleConfigEntry name(String str) {
        this.name = str;
        return this;
    }

    public ModuleConfigEntry x(float f) {
        this.x = f;
        return this;
    }

    public ModuleConfigEntry y(float f) {
        this.y = f;
        return this;
    }

    public ModuleConfigEntry settings(List<LayoutNode> list) {
        this.settings = list;
        return this;
    }
}
