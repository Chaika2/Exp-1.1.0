package aethereal.model;


public final class ConfigReference {
    public final String id;
    public final String name;

    public ConfigReference(String str, String str2) {
        this.id = str;
        this.name = str2;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "id=" + this.id + ", " + "name=" + this.name + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.id, this.name);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConfigReference)) return false;
        ConfigReference o = (ConfigReference) obj;
        return java.util.Objects.equals(this.id, o.id) && java.util.Objects.equals(this.name, o.name);
    }
public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }
}
