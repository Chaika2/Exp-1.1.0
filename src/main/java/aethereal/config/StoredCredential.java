package aethereal.config;

public class StoredCredential {
    public String password;
    public String token;
    public String username;

    public StoredCredential() {
    }

    public StoredCredential(String str, String str2, String str3) {
        this.password = str;
        this.token = str2;
        this.username = str3;
    }
}
