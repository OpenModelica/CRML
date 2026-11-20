package org.openmodelica.crml.compiler.util;

import org.eclipse.emf.ecore.EObject;
import org.openmodelica.crml.language.pretty.PrettyPrint;
import org.openmodelica.crml.testutil.CustomHtmlReporter;

import static j2html.TagCreator.code;
import static j2html.TagCreator.pre;

public class ObjectModelWrapper implements CustomHtmlReporter {
    private final EObject model;

    public ObjectModelWrapper(EObject model) {
        this.model = model;
    }

    @Override
    public Object report() {
        return pre(code(PrettyPrint.prettyPrint(model)));
    }

    @Override
    public boolean collapsible() {
        return true;
    }

    public static ObjectModelWrapper of(EObject model) {
        return new ObjectModelWrapper(model);
    }
}
