package aethereal.config;
import aethereal.util.AtomicFileWriter;
import aethereal.Expensive;
import aethereal.type.Mc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

public class SessionNicknameStore {
    public final Path filePath;
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void saveCurrentSessionNickname() {
        String username;
        if (Mc.INSTANCE.getSession() == null || (username = Mc.INSTANCE.getSession().getUsername()) == null || username.isBlank()) {
            return;
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("nickname", username);
        try {
            if (this.filePath.getParent() != null) {
                Files.createDirectories(this.filePath.getParent(), new FileAttribute[0]);
            }
            AtomicFileWriter.writeBytes(this.filePath, gson.toJson(jsonObject).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            Expensive.LOGGER.error("Failed to save session nickname", e);
        }
    }

    public String loadSessionNickname() {
        JsonObject jsonObject;
        if (!Files.exists(this.filePath, new LinkOption[0])) {
            return null;
        }
        try {
            String strTrim = Files.readString(this.filePath, StandardCharsets.UTF_8).trim();
            if (strTrim.isBlank() || (jsonObject = (JsonObject) gson.fromJson(strTrim, JsonObject.class)) == null || !jsonObject.has("nickname")) {
                return null;
            }
            String asString = jsonObject.get("nickname").getAsString();
            if (asString == null || asString.isBlank()) {
                return null;
            }
            return asString;
        } catch (IOException | JsonSyntaxException e) {
            Expensive.LOGGER.error("Failed to load session nickname", e);
            return null;
        }
    }

    public SessionNicknameStore(Path path) {
        this.filePath = path;
    }
}
