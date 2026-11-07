package aethereal.util;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class StringMappingLookup implements StringLookup {
    @SerializedName("mappings")
    public Map<String, String> mappings;

    @Override
    public String lookup(String str) {
        if (this.mappings == null) {
            return null;
        }
        return this.mappings.get(str);
    }
}
