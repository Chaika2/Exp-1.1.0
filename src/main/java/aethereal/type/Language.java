package aethereal.type;

public enum Language {
    en_US("en_US.json", "English"),
    ru_RU("ru_RU.json", "Русский");

    public final String source;
    public final String canonical;
    public static final Language PRIMARY = en_US;

    Language(String str, String str2) {
        this.source = str;
        this.canonical = str2;
    }

    public String source() {
        return this.source;
    }

    public String canonical() {
        return this.canonical;
    }
}
