package org.openmodelica.crml.compiler.util;

import static j2html.TagCreator.pre;

import org.openmodelica.crml.testutil.CustomHtmlReporter;
import org.openmodelica.crml.testutil.FormatUtil;

public class CodeWrapper implements CustomHtmlReporter {
    private final String code;

    public CodeWrapper(String code) {
        this.code = code;
    }

    @Override
    public Object report() {
        return pre(FormatUtil.numcode(code));
    }

    @Override
    public boolean collapsible() {
        return true;
    }

    @Override
    public String summary() {
        return FormatUtil.lineCount(code) + " lines";
    }

    public static CodeWrapper of(String code) {
        return new CodeWrapper(code);
    }
}
