package crml.compiler;

import java.io.File;
import java.nio.file.Path;

public class Utilities {

    /**
     * Remove the last .* of a name and the directory path
     * /path/to/some/FILE.ext -> FILE
     * @return
     */
    public static String stripNameEndingAndPath(Path name) {
        String fileName = name.getFileName().toString();
        // check if it even has an extension and return the name if not!
        int dotPos = fileName.lastIndexOf('.');
        if (dotPos == -1) {
            return fileName;
        }
        return fileName.substring(0, dotPos);
    }

   public static String addDirToPath (String path, String dir){
        return path + java.io.File.separator + dir;
    }

     public static String removeWindowsDriveLetter(String path) {
        // check if is a windows path and remove the /C: part
        int colonIndex = path.indexOf(':');
        if (colonIndex != -1)
            return path.substring(colonIndex + 1, path.length());
        return path;
     }

     public static File getFileInPath(String fileName) {
        String path = System.getenv("PATH");
        
        if (path != null) {
            String[] paths = path.split(File.pathSeparator);

            for (String p : paths) {
                File file = new File(p, fileName);
                if (file.exists()) {
                    return file;
                }
            }
        }
        
        return null;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
