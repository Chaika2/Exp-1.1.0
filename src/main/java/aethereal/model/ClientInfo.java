package aethereal.model;


public final class ClientInfo {
    public final String version;
    public final String branch;
    public final String updated;

    public ClientInfo(String str, String str2, String str3) {
        this.version = str;
        this.branch = str2;
        this.updated = str3;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "version=" + this.version + ", " + "branch=" + this.branch + ", " + "updated=" + this.updated + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.version, this.branch, this.updated);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ClientInfo)) return false;
        ClientInfo o = (ClientInfo) obj;
        return java.util.Objects.equals(this.version, o.version) && java.util.Objects.equals(this.branch, o.branch) && java.util.Objects.equals(this.updated, o.updated);
    }
public String version() {
        return this.version;
    }

    public String branch() {
        return this.branch;
    }

    public String updated() {
        return this.updated;
    }
}
