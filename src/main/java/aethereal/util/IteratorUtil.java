package aethereal.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorUtil {
    public static <T> List<T> toList(Iterator<? extends T> it) {
        if (it == null) {
            throw new NullPointerException("Iterator must not be null");
        }
        ArrayList arrayList = new ArrayList();
        while (it.hasNext()) {
            arrayList.add(it.next());
        }
        return arrayList;
    }
}
