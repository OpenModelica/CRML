package crml.language.util;

import org.eclipse.emf.ecore.EObject;

import crml.language.pretty.PrettyPrint;
import crml.test.CustomHtmlReporter;
import crml.test.FormatUtil;

import static j2html.TagCreator.code;
import static j2html.TagCreator.pre;

public class DOMWrapper implements CustomHtmlReporter {
    private final EObject dom;

    public DOMWrapper(EObject dom) {
        this.dom = dom;
    }

    @Override
    public Object report() {
        //return pre(FormatUtil.numcode(PrettyPrint.prettyPrint(dom)));
        return pre(code(PrettyPrint.prettyPrint(dom)));
    }

    public static DOMWrapper of(EObject dom) {
        return new DOMWrapper(dom);
    }
}
