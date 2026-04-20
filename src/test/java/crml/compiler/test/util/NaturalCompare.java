package crml.compiler.test.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalCompare implements Comparator<String> {
    private static final Pattern TOKENIZER = Pattern.compile("(\\D+|\\d+)");

    public static class PathCompare implements Comparator<Path> {
        @Override
        public int compare(Path s1, Path s2) {
            return compareStrings(s1.toString(), s2.toString());
        }
    }

    @Override
    public int compare(String s1, String s2){
        return NaturalCompare.compareStrings(s1, s2);
    }

    public static int compareStrings(String s1, String s2) {
        List<Object> t1 = tokenize(s1);
        List<Object> t2 = tokenize(s2);

        for (int i=0; i < Math.min(t1.size(), t2.size()); i++){
            Object o1 = t1.get(i);
            Object o2 = t2.get(i);

            int result = compareTokens(o1, o2);
            if (result != 0) {
                return result;
            }
        }

        return Integer.compare(t1.size(), t2.size());
    }

    private static int compareTokens(Object o1, Object o2) {
        if (o1 instanceof Integer && o2 instanceof Integer) {
            return Integer.compare((Integer) o1, (Integer) o2);
        }
        return o1.toString().compareTo(o2.toString());
    }


    public static List<Object> tokenize(String string){
        List<Object> tokens = new ArrayList<Object>();
        Matcher matcher = TOKENIZER.matcher(string);

        while (matcher.find()) {
            String segment = matcher.group();
            try{
                Integer num = Integer.parseInt(segment);
                System.out.println(num);
                tokens.add(num);
            } catch (NumberFormatException e){
                tokens.add(segment);
            }
        }

        return tokens;
    }
}
