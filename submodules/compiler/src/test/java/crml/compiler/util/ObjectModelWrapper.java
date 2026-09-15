package crml.compiler.util;

import org.eclipse.emf.ecore.EObject;

import crml.language.pretty.PrettyPrint;
import crml.test.CustomHtmlReporter;

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

    public static ObjectModelWrapper of(EObject model) {
        return new ObjectModelWrapper(model);
    }
}
