package crml.compiler.util;

import crml.test.CustomHtmlReporter;
import crml.test.FormatUtil;

import static j2html.TagCreator.pre;

public class CodeWrapper implements CustomHtmlReporter {
    private final String code;

    public CodeWrapper(String code) {
        this.code = code;
    }

    @Override
    public Object report() {
        return pre(FormatUtil.numcode(code));
    }

    public static CodeWrapper of(String code) {
        return new CodeWrapper(code);
    }
}
