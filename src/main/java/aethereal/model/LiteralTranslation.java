package aethereal.model;
import aethereal.util.StringLookup;
import aethereal.util.StringUtil;

public class LiteralTranslation implements Translation {
    String firstLetterUppercase;
    final String original;

    LiteralTranslation(String str) {
        this.original = str;
    }

    @Override
    public void lookupFromDictionary(StringLookup class045Var) {
        this.firstLetterUppercase = StringUtil.firstLetterUppercase(this.original);
    }

    @Override
    public String original() {
        return this.original;
    }

    @Override
    public String effective() {
        return this.original;
    }

    @Override
    public String firstLetterUppercase() {
        return this.firstLetterUppercase;
    }
}
