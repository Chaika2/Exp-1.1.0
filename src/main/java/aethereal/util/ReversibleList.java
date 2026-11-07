package aethereal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ReversibleList<T> implements Iterable<T> {
    public final List<T> items;
    public boolean reversed;

    public ReversibleList(int i) {
        this.items = new ArrayList(i);
    }

    public void add(T t) {
        this.items.add(t);
    }

    public void clear() {
        this.items.clear();
        this.reversed = false;
    }

    public void reverse() {
        if (this.reversed) {
            return;
        }
        Collections.reverse(this.items);
        this.reversed = true;
    }

    @Override
    @NotNull
    public Iterator<T> iterator() {
        reverse();
        return this.items.iterator();
    }
}
