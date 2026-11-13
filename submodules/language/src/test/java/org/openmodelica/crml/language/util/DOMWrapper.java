package org.openmodelica.crml.language.util;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.language.pretty.PrettyPrint;
import org.openmodelica.crml.testutil.CustomHtmlReporter;

import static j2html.TagCreator.code;
import static j2html.TagCreator.pre;

public class DOMWrapper implements CustomHtmlReporter {
    private final EObject dom;

    public DOMWrapper(EObject dom) {
        this.dom = dom;
    }

    @Override
    public Object report() {
        return pre(code(PrettyPrint.prettyPrint(dom)));
    }

    @Override
    public boolean collapsible() {
        return true;
    }

    public static DOMWrapper of(EObject dom) {
        return new DOMWrapper(dom);
    }
}
