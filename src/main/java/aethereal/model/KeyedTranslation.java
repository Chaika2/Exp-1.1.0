package aethereal.model;
import aethereal.util.StringLookup;
import aethereal.util.StringUtil;

public class KeyedTranslation implements Translation {
    String firstLetterUppercase;
    String translatedText;
    final String original;

    KeyedTranslation(String str) {
        this.original = str;
    }

    @Override
    public void lookupFromDictionary(StringLookup class045Var) {
        try {
            this.translatedText = class045Var.lookup(this.original);
            this.firstLetterUppercase = StringUtil.firstLetterUppercase(this.translatedText.toLowerCase());
        } catch (NullPointerException e) {
            System.out.println(this.original + " not found");
        }
    }

    @Override
    public String original() {
        return this.original;
    }

    @Override
    public String firstLetterUppercase() {
        return this.firstLetterUppercase;
    }

    @Override
    public String effective() {
        return this.translatedText == null ? "null" : this.translatedText;
    }
}
