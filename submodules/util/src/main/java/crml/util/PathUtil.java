package crml.util;

import java.nio.file.Path;
import java.util.Arrays;

public class PathUtil {

    public enum Option {
        ABSOLUTE,
        UNIX,
        ESCAPED,
        NORMALIZE
    }
    public static String toString(Path path, Option... options) {
        Path resolved = has(options, Option.ABSOLUTE) ? path.toAbsolutePath() : path;
        if (has(options, Option.NORMALIZE)) {
            resolved = resolved.normalize();
        }
        return toString(resolved.toString(), options);
    }

    public static String toString(String path, Option... options) {
        String result = path;

        if (has(options, Option.NORMALIZE)) {
            result = Path.of(result).normalize().toString();
        }

        if (has(options, Option.UNIX)) {
            result = result.replace('\\', '/');
        }

        if (has(options, Option.ESCAPED)) {
            result = result.replace("\\", "\\\\");
        }

        return result;
    }

    private static boolean has(Option[] options, Option key) {
        return Arrays.asList(options).contains(key);
    }
}
