package aethereal.model;

public class AltAccount {
    public String name;
    public String type;
    public long lastUsed;

    public AltAccount() {
    }

    public AltAccount(String name, String type) {
        this.name = name;
        this.type = type;
        this.lastUsed = 0L;
    }
}
