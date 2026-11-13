package org.openmodelica.crml.util;

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

    /**
     * Hide constructor.
     * This is a static utility class.
     */
    private SafeResource() {}

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
                FileSystem fs = getFS(jarUri);
                return fs.getPath(parts[1]);
            }
            return Paths.get(uri);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException("Failed to load resource: " + name, e);
        }
    }
    
    private static FileSystem getFS(URI uri) throws IOException{
        try {
            return FileSystems.newFileSystem(uri, Collections.emptyMap());
        } catch (FileSystemAlreadyExistsException e) {
            return FileSystems.getFileSystem(uri);
        }
    }
}
