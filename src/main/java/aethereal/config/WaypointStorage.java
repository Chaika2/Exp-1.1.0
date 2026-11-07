package aethereal.config;
import aethereal.util.AtomicFileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;

public class WaypointStorage {
    public final Path path;
    public final WaysRepository repository;
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void save() throws IOException {
        AtomicFileWriter.writeBytes(this.path, gson.toJson(this.repository.getWaypoints()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }

    public void load() throws IOException {
        if (Files.exists(this.path, new LinkOption[0])) {
            Waypoint[] class674VarArr = (Waypoint[]) gson.fromJson(Files.readString(this.path, StandardCharsets.UTF_8), Waypoint[].class);
            Set<Waypoint> waypoints = this.repository.getWaypoints();
            waypoints.clear();
            waypoints.addAll(List.of(class674VarArr));
        }
    }

    public WaypointStorage(Path path, WaysRepository class675Var) {
        this.path = path;
        this.repository = class675Var;
    }
}
