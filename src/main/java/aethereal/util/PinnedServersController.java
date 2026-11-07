package aethereal.util;
import aethereal.model.PinnedServerEntry;

import java.util.ArrayList;
import java.util.List;

public class PinnedServersController {
    public final List<PinnedServerEntry> favorites = new ArrayList();

    public void init() {
        addFavorite(new PinnedServerEntry("MetaHvH - Лучший читерский сервер", "exp.metahvh.space:25565", 52945));
    }

    public boolean isPinned(String str) {
        String strMethod002 = normalizeAddress(str);
        return this.favorites.stream().anyMatch(class029Var -> {
            return normalizeAddress(class029Var.address()).equals(strMethod002);
        });
    }

    public void addFavorite(PinnedServerEntry class029Var) {
        this.favorites.add(class029Var);
    }

    public static String normalizeAddress(String str) {
        if (str == null) {
            return "";
        }
        String strTrim = str.trim();
        return !strTrim.contains(":") ? strTrim + ":25565" : strTrim;
    }

    public List<PinnedServerEntry> favorites() {
        return List.copyOf(this.favorites);
    }
}
