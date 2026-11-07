package aethereal.model;

import com.google.gson.annotations.SerializedName;

public final class MacroEntry {

    @SerializedName("name")
    public final String name;

    @SerializedName("content")
    public final String content;

    @SerializedName("key")
    public final int key;

    public MacroEntry(String str, String str2, int i) {
        this.name = str;
        this.content = str2;
        this.key = i;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "name=" + this.name + ", " + "content=" + this.content + ", " + "key=" + this.key + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.name, this.content, this.key);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MacroEntry)) return false;
        MacroEntry o = (MacroEntry) obj;
        return java.util.Objects.equals(this.name, o.name) && java.util.Objects.equals(this.content, o.content) && java.util.Objects.equals(this.key, o.key);
    }
@SerializedName("name")
    public String name() {
        return this.name;
    }

    @SerializedName("content")
    public String content() {
        return this.content;
    }

    @SerializedName("key")
    public int key() {
        return this.key;
    }
}
