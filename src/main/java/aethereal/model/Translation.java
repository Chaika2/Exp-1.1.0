package aethereal.model;
import aethereal.util.StringLookup;

public interface Translation {
    void lookupFromDictionary(StringLookup class045Var);

    String original();

    String effective();

    String firstLetterUppercase();

    static Translation clearText(String str) {
        return new LiteralTranslation(str);
    }

    static Translation unformatted(String str) {
        return new KeyedTranslation(str);
    }
}
