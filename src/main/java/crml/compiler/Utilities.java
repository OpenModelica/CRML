package crml.compiler;

import java.util.List;

import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

public class Utilities {

    /**
     * Remove the last .* of a name 
     * @return
     */
    public static String stripNameEnding (String name){
        return name.substring(0, name.lastIndexOf('.'));
    }

   public static String addDirToPath (String path, String dir){
        return path + java.io.File.separator + dir;
    }

     /** Platform dependent end-of-line marker */
     public static final String Eol = System.lineSeparator();
     /** The literal indent char(s) used for pretty-printing */
     public static final String Indents = "  ";
     private static int level;
 
 
     /**
      * Pretty print out a whole tree. {@link #getNodeText} is used on the node payloads to get the text
      * for the nodes. (Derived from Trees.toStringTree(....))
      */
     public static String toPrettyTree(final Tree t, final List<String> ruleNames) {
         level = 0;
         return process(t, ruleNames).replaceAll("(?m)^\\s+$", "").replaceAll("\\r?\\n\\r?\\n", Eol);
     }
 
     private static String process(final Tree t, final List<String> ruleNames) {
         if (t.getChildCount() == 0) return Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
         StringBuilder sb = new StringBuilder();
         sb.append(lead(level));
         level++;
         String s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
         sb.append(s + ' ');
         for (int i = 0; i < t.getChildCount(); i++) {
             sb.append(process(t.getChild(i), ruleNames));
         }
         level--;
         sb.append(lead(level));
         return sb.toString();
     }
 
     private static String lead(int level) {
         StringBuilder sb = new StringBuilder();
         if (level > 0) {
             sb.append(Eol);
             for (int cnt = 0; cnt < level; cnt++) {
                 sb.append(Indents);
             }
         }
         return sb.toString();
     }
    
}