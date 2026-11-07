package aethereal.util;

import java.util.List;

public interface ArgumentParser<T> {
    T parse(String str);

    List<String> getSuggestions(String str);

    String getName();
}
