package aethereal.model;


public final class CredentialKey {
    public final String serverAddress;
    public final String username;

    public CredentialKey(String str, String str2) {
        this.serverAddress = str;
        this.username = str2;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "serverAddress=" + this.serverAddress + ", " + "username=" + this.username + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.serverAddress, this.username);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CredentialKey)) return false;
        CredentialKey o = (CredentialKey) obj;
        return java.util.Objects.equals(this.serverAddress, o.serverAddress) && java.util.Objects.equals(this.username, o.username);
    }
public String serverAddress() {
        return this.serverAddress;
    }

    public String username() {
        return this.username;
    }
}
