package crml.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import crml.util.SafeResource;


public class TestResourcesRoot {
    public static final Path RESOURCES = SafeResource.get("testResourcesRoot").getParent();

    public static List<Path> fileNameSourceHelper(Path source) {
        try (Stream<Path> list = Files.list(source)) {
            return list
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".crml"))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Path> listFiles(Path source, Predicate<Path> filter) {
        try (Stream<Path> list = Files.list(source)) {
            return list
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".crml"))
                .filter(filter)
                .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
