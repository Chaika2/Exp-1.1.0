package aethereal.type;
import aethereal.util.ArgumentParser;
import aethereal.util.KeyboardUtil;
import aethereal.Lang;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.util.InputUtil;

public class KeyArgumentType implements ArgumentParser<Integer> {
    public final Map<String, Integer> keyMap = buildKeyMap();

    public Map<String, Integer> buildKeyMap() {
        HashMap map = new HashMap();
        ObjectIterator it = InputUtil.Type.KEYSYM.map.int2ObjectEntrySet().iterator();
        while (it.hasNext()) {
            int intKey = ((Int2ObjectMap.Entry) it.next()).getIntKey();
            map.put(KeyboardUtil.keyToString(intKey).replaceAll("\\s", "").toLowerCase(), Integer.valueOf(intKey));
        }
        return map;
    }

    @Override
    public Integer parse(String str) throws TranslatedException {
        Integer num = this.keyMap.get(str.toLowerCase());
        if (num == null) {
            throw new TranslatedException(Translation.clearText(Lang.TYPE_UNKNOWN_KEY.effective().replace("{input}", str)));
        }
        return num;
    }

    @Override
    public List<String> getSuggestions(String str) {
        String lowerCase = str.toLowerCase();
        return (List) this.keyMap.keySet().stream().filter(str2 -> {
            return str2.startsWith(lowerCase);
        }).sorted().collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "key";
    }
}
