package aethereal.config;
import aethereal.util.AtomicFileWriter;
import aethereal.model.MacroEntry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;

public class MacroStorage {
    public final Path filePath;
    public final MacroRepository repository;
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static final Type macroSetType = new MacroSetTypeToken().getType();

    public void save() throws IOException {
        AtomicFileWriter.writeBytes(this.filePath, gson.toJson(Set.copyOf(this.repository.getMacroList()), macroSetType).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    public void load() throws IOException {
        if (Files.exists(this.filePath, new LinkOption[0])) {
            Set set = (Set) gson.fromJson(Files.readString(this.filePath, StandardCharsets.UTF_8), macroSetType);
            if (set == null) {
                return;
            }
            List<MacroEntry> macroList = this.repository.getMacroList();
            macroList.clear();
            macroList.addAll(set);
        }
    }

    public MacroStorage(Path path, MacroRepository class725Var) {
        this.filePath = path;
        this.repository = class725Var;
    }
}
