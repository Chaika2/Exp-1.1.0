package aethereal.config;
import aethereal.util.AtomicFileWriter;
import aethereal.util.StaffDetector;

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

public class StaffNamesStore {
    public final Path filePath;
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void save() throws IOException {
        AtomicFileWriter.writeBytes(this.filePath, gson.toJson(StaffDetector.getCustomStaffNames()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    public void load() throws IOException {
        Set set;
        if (Files.exists(this.filePath, new LinkOption[0])) {
            String strTrim = Files.readString(this.filePath, StandardCharsets.UTF_8).trim();
            if (strTrim.isBlank() || (set = (Set) gson.fromJson(strTrim, new StaffNamesTypeToken(this).getType())) == null) {
                return;
            }
            Iterator it = set.iterator();
            while (it.hasNext()) {
                StaffDetector.addCustomStaffName((String) it.next());
            }
        }
    }

    public StaffNamesStore(Path path) {
        this.filePath = path;
    }
}
