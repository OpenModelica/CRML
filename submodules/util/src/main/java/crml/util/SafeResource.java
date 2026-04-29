package crml.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

public class SafeResource {

    public static Path get(String name) {
        return get(name, Thread.currentThread().getContextClassLoader());
    }

    public static Path get(String name, ClassLoader classLoader) {
        URL url = classLoader.getResource(name);
        if (url == null) {
            throw new IllegalArgumentException("Resource not found: " + name);
        }
        try {
            URI uri = url.toURI();
            if ("jar".equals(uri.getScheme())) {
                String[] parts = uri.toString().split("!");
                URI jarUri = URI.create(parts[0]);
                FileSystem fs;
                try {
                    fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                } catch (FileSystemAlreadyExistsException e) {
                    fs = FileSystems.getFileSystem(jarUri);
                }
                return fs.getPath(parts[1]);
            }
            return Paths.get(uri);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Failed to load resource: " + name, e);
        }
    }
}
