package aethereal.net;
import aethereal.render.GlTexture;


public final class UserSession {
    public final String uid;
    public final String username;
    public final String hwid;
    public final String role;
    public final String expire;
    public final String avatarUrl;
    public final GlTexture texture;

    public UserSession(String str, String str2, String str3, String str4, String str5, String str6, GlTexture class073Var) {
        this.uid = str;
        this.username = str2;
        this.hwid = str3;
        this.role = str4;
        this.expire = str5;
        this.avatarUrl = str6;
        this.texture = class073Var;
    }

    @Override
    public String toString() {
        return String.format("Username %s, Uid %s, Role %s, Hwid %s", this.username, this.uid, this.role, this.hwid);
    }

    public UserSession withExpire(String str) {
        return this.expire == str ? this : new UserSession(this.uid, this.username, this.hwid, this.role, str, this.avatarUrl, this.texture);
    }

    public UserSession withTexture(GlTexture class073Var) {
        return this.texture == class073Var ? this : new UserSession(this.uid, this.username, this.hwid, this.role, this.expire, this.avatarUrl, class073Var);
    }

        @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.uid, this.username, this.hwid, this.role, this.expire, this.avatarUrl, this.texture);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserSession)) return false;
        UserSession o = (UserSession) obj;
        return java.util.Objects.equals(this.uid, o.uid) && java.util.Objects.equals(this.username, o.username) && java.util.Objects.equals(this.hwid, o.hwid) && java.util.Objects.equals(this.role, o.role) && java.util.Objects.equals(this.expire, o.expire) && java.util.Objects.equals(this.avatarUrl, o.avatarUrl) && java.util.Objects.equals(this.texture, o.texture);
    }
public String uid() {
        return this.uid;
    }

    public String username() {
        return this.username;
    }

    public String hwid() {
        return this.hwid;
    }

    public String role() {
        return this.role;
    }

    public String expire() {
        return this.expire;
    }

    public String avatarUrl() {
        return this.avatarUrl;
    }

    public GlTexture texture() {
        return this.texture;
    }
}
