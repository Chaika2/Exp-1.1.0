package aethereal.util;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TranslationRegistry {
    public static final List<Translation> translations = new ArrayList();

    public TranslationRegistry() {
    }

    public static void register(Translation class254Var) {
        translations.add(class254Var);
    }

    public static List<Translation> getAll() {
        return Collections.unmodifiableList(translations);
    }
}
