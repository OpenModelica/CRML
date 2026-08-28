package crml.compiler.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import crml.test.CustomHtmlReporter;

import static j2html.TagCreator.code;
import static j2html.TagCreator.pre;

public class ThrowableWrapper implements CustomHtmlReporter {
    private final Throwable throwable;

    public ThrowableWrapper(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public Object report() {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return pre(code(sw.toString()));
    }

    public static ThrowableWrapper of(Throwable throwable) {
        return new ThrowableWrapper(throwable);
    }
}
