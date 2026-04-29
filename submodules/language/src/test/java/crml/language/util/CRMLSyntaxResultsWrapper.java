package crml.language.util;

import crml.language.util.ErrorListener.CRMLSyntaxResults;
import crml.test.CustomHtmlReporter;

import static j2html.TagCreator.p;
import static j2html.TagCreator.join;

public class CRMLSyntaxResultsWrapper  implements CustomHtmlReporter{
    private final CRMLSyntaxResults data;
    public CRMLSyntaxResultsWrapper(CRMLSyntaxResults data){
        this.data = data;
    }
    @Override
    public Object report() {
        return join(data.errors().stream().map(Object::toString).map(e -> p(e)).toArray());
    }
    public static CRMLSyntaxResultsWrapper of(CRMLSyntaxResults data){
        return new CRMLSyntaxResultsWrapper(data);
    }
}