package crml.compiler.crmlcv2.util;

import java.util.List;

public class LineMerger {
    public static String merge(List<String> lines){
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
}
