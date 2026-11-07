package aethereal.config;

import aethereal.model.AltAccount;
import aethereal.util.AtomicFileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class AltManagerStore {
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public final Path path;

    public List<AltAccount> load() throws IOException {
        if (!Files.exists(this.path, new LinkOption[0])) {
            return new ArrayList();
        }
        String string = Files.readString(this.path, StandardCharsets.UTF_8);
        if (string.isBlank()) {
            return new ArrayList();
        }
        List<AltAccount> list = (List) gson.fromJson(string, new AltListTypeToken().getType());
        return list == null ? new ArrayList() : list;
    }

    public void save(List<AltAccount> list) throws IOException {
        AtomicFileWriter.writeBytes(this.path, gson.toJson(list).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    public AltManagerStore(Path path) {
        this.path = path;
    }
}
