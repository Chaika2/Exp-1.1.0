package aethereal.config;
import aethereal.util.AtomicFileWriter;
import aethereal.util.FriendManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Set;

public class FriendStore {
    public final Path filePath;
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void save() throws IOException {
        AtomicFileWriter.writeBytes(this.filePath, gson.toJson(Set.copyOf(FriendManager.getFriends())).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    public void load() throws IOException {
        if (Files.exists(this.filePath, new LinkOption[0])) {
            Set set = (Set) gson.fromJson(Files.readString(this.filePath, StandardCharsets.UTF_8), new FriendSetTypeToken(this).getType());
            FriendManager.clear();
            Iterator it = set.iterator();
            while (it.hasNext()) {
                FriendManager.add((String) it.next());
            }
        }
    }

    public FriendStore(Path path) {
        this.filePath = path;
    }
}
