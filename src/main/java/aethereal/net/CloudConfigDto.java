package aethereal.net;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.util.Base64;

public class CloudConfigDto {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("base64")
    public String base64;

    @SerializedName("author")
    public String author;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    @SerializedName("is_local_override")
    public boolean localOverride;

    public static CloudConfigDto of(String str, String str2, String str3) {
        CloudConfigDto class376Var = new CloudConfigDto();
        class376Var.id = str;
        class376Var.name = str2;
        class376Var.author = str3;
        return class376Var;
    }

    public byte[] decodeData() {
        if (this.base64 == null) {
            throw new IllegalStateException("Config not found: payload is missing");
        }
        return Base64.getDecoder().decode(this.base64);
    }

    public long createdTimestamp() {
        return parseTimestamp(this.createdAt);
    }

    public long updatedTimestamp() {
        return parseTimestamp(this.updatedAt);
    }

    public static long parseTimestamp(String str) {
        try {
            return Instant.parse(str).toEpochMilli();
        } catch (Exception e) {
            return 0L;
        }
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String base64() {
        return this.base64;
    }

    public String author() {
        return this.author;
    }

    public String createdAt() {
        return this.createdAt;
    }

    public String updatedAt() {
        return this.updatedAt;
    }

    public boolean isLocalOverride() {
        return this.localOverride;
    }
}
