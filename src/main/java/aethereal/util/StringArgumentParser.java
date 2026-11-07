package aethereal.util;

import java.util.Collections;
import java.util.List;

public class StringArgumentParser implements ArgumentParser<String> {
    @Override
    public String parse(String str) {
        return str;
    }

    @Override
    public List<String> getSuggestions(String str) {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "string";
    }
}
