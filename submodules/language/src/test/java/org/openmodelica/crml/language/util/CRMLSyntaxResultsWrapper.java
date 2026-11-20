package org.openmodelica.crml.language.util;

import static j2html.TagCreator.p;

import org.openmodelica.crml.language.util.ErrorListener.CRMLSyntaxResults;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic.LocationStyle;
import org.openmodelica.crml.testutil.CustomHtmlReporter;

import static j2html.TagCreator.join;

public class CRMLSyntaxResultsWrapper  implements CustomHtmlReporter{
    private final CRMLSyntaxResults data;
    private final LocationStyle style;
    public CRMLSyntaxResultsWrapper(CRMLSyntaxResults data, LocationStyle style){
        this.data = data;
        this.style = style;
    }
    @Override
    public Object report() {
        return join(data.errors().stream().map(e -> e.format(style)).map(e -> p(e)).toArray());
    }
    /** Without a location, as for a single-file test. */
    public static CRMLSyntaxResultsWrapper of(CRMLSyntaxResults data){
        return new CRMLSyntaxResultsWrapper(data, LocationStyle.NONE);
    }
    public static CRMLSyntaxResultsWrapper of(CRMLSyntaxResults data, LocationStyle style){
        return new CRMLSyntaxResultsWrapper(data, style);
    }
}