package aethereal.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileResource implements ResourceSource {
    public final String path;

    @Override
    public InputStream stream() {
        try {
            return new ByteArrayInputStream(Files.readAllBytes(Path.of(this.path, new String[0])));
        } catch (Exception e) {
            throw new RuntimeException("Can't read file from path: " + this.path, e);
        }
    }

    public FileResource(String str) {
        this.path = str;
    }
}
