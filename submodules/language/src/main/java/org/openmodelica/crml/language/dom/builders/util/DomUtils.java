package org.openmodelica.crml.language.dom.builders.util;

import org.openmodelica.crml.language.dom.Recovery;

import crml.language.grammar.crmlParser.IdContext;

public class DomUtils {
    /** The name written at {@code context}; null if the parser recovered it from a syntax error. */
    public static String name(IdContext context){
        return Recovery.isMissing(context) ? null : context.getText();
    }
}
