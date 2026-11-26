package org.openmodelica.crml.language.util;

import static j2html.TagCreator.code;
import static j2html.TagCreator.pre;

import org.openmodelica.crml.testutil.CustomHtmlReporter;

/**
 * The pretty-printed parse tree of a model, shown collapsed. The report only
 * collapses a plain string emitted under the key {@code "AST"}; this wrapper
 * does the same under any key, for tests that emit one AST per file.
 */
public class AstWrapper implements CustomHtmlReporter {
    private final String tree;

    public AstWrapper(String tree) {
        this.tree = tree;
    }

    @Override
    public Object report() {
        return pre(code(tree));
    }

    @Override
    public boolean collapsible() {
        return true;
    }

    public static AstWrapper of(Parser.ParserResult parsed) {
        return new AstWrapper(parsed.toPrettyTree());
    }
}
