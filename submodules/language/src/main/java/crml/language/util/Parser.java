package crml.language.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;
import org.antlr.v4.runtime.misc.Utils;

import crml.language.grammar.crmlLexer;
import crml.language.grammar.crmlParser;
import crml.language.util.ErrorListener.CRMLSyntaxResults;

public class Parser {
    public ParserResult parse(File model) throws IOException{
        return parse(CharStreams.fromPath(model.toPath()));
    }
    
    public ParserResult parse(Path model) throws IOException{
        return parse(CharStreams.fromPath(model));
    }

    public ParserResult parse(String model){
        return parse(CharStreams.fromString(model));
    }

    public ParserResult parse(CharStream model){
        ErrorListener errors = new ErrorListener();

        crmlLexer lexer = new crmlLexer(model);
        lexer.removeErrorListeners(); // Remove default listener that prints the output
        lexer.addErrorListener(errors);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        crmlParser parser = new crmlParser(tokens);
        parser.removeErrorListeners(); // Remove default listener that prints the output
        parser.addErrorListener(errors);

        List<String> ruleNames = Arrays.asList(parser.getRuleNames());
        ParseTree tree = parser.definition();

        return new ParserResult(parser, tree, ruleNames, errors.getErrors());
    }

    public static record ParserResult(crmlParser parser, ParseTree ast, List<String> ruleNames, CRMLSyntaxResults syntax) {
        private static final String EOL = System.getProperty("line.separator");
        private static final String INDENTS = "  ";
        /**
         * Pretty print out a whole tree. {@link #getNodeText} is used on the node payloads to get the text
         * for the nodes. (Derived from Trees.toStringTree(....))
         */
        public String toPrettyTree() {
            return process(0, ast, ruleNames).replaceAll("(?m)^\\s+$", "").replaceAll("\\r?\\n\\r?\\n", EOL);
        }
        
        private static String process(int level, final Tree t, final List<String> ruleNames) {
            if (t.getChildCount() == 0) return Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
            StringBuilder sb = new StringBuilder();
            sb.append(lead(level));
            level++;
            String s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
            sb.append(s + ' ');
            for (int i = 0; i < t.getChildCount(); i++) {
                sb.append(process(level+1, t.getChild(i), ruleNames));
            }
            level--;
            sb.append(lead(level));
            return sb.toString();
        }

        private static String lead(int level) {
         StringBuilder sb = new StringBuilder();
         if (level > 0) {
             sb.append(EOL);
             for (int cnt = 0; cnt < level; cnt++) {
                 sb.append(INDENTS);
             }
         }
         return sb.toString();
     }
    }
}
