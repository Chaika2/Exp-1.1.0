package aethereal.model;


public final class AccountProfile {
    public final String uid;
    public final String login;
    public final String signature;
    public final String hwid;
    public final String avatarUrl;

    public AccountProfile(String str, String str2, String str3, String str4, String str5) {
        this.uid = str;
        this.login = str2;
        this.signature = str3;
        this.hwid = str4;
        this.avatarUrl = str5;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "uid=" + this.uid + ", " + "login=" + this.login + ", " + "signature=" + this.signature + ", " + "hwid=" + this.hwid + ", " + "avatarUrl=" + this.avatarUrl + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.uid, this.login, this.signature, this.hwid, this.avatarUrl);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AccountProfile)) return false;
        AccountProfile o = (AccountProfile) obj;
        return java.util.Objects.equals(this.uid, o.uid) && java.util.Objects.equals(this.login, o.login) && java.util.Objects.equals(this.signature, o.signature) && java.util.Objects.equals(this.hwid, o.hwid) && java.util.Objects.equals(this.avatarUrl, o.avatarUrl);
    }
public String uid() {
        return this.uid;
    }

    public String login() {
        return this.login;
    }

    public String signature() {
        return this.signature;
    }

    public String hwid() {
        return this.hwid;
    }

    public String avatarUrl() {
        return this.avatarUrl;
    }
}
