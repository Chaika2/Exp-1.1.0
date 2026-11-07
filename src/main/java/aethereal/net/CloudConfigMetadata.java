package aethereal.net;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;

public class CloudConfigMetadata {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("author")
    public String author;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    @SerializedName("author_avatar_url")
    public String authorAvatarUrl;

    @SerializedName("is_local_override")
    public boolean localOverride;

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

    public String author() {
        return this.author;
    }

    public String createdAt() {
        return this.createdAt;
    }

    public String updatedAt() {
        return this.updatedAt;
    }

    public String authorAvatarUrl() {
        return this.authorAvatarUrl;
    }

    public boolean isLocalOverride() {
        return this.localOverride;
    }
}
