package aethereal.model;

import com.google.gson.annotations.SerializedName;

public final class FriendEntry {

    @SerializedName("name")
    public final String name;

    public FriendEntry(String str) {
        this.name = str;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "name=" + this.name + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.name);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FriendEntry)) return false;
        FriendEntry o = (FriendEntry) obj;
        return java.util.Objects.equals(this.name, o.name);
    }
@SerializedName("name")
    public String name() {
        return this.name;
    }
}
