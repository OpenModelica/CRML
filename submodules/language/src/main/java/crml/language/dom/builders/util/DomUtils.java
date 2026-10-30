package crml.language.dom.builders.util;

import crml.language.grammar.crmlParser.IdContext;

public class DomUtils {
    public static String text(IdContext context){
        if(context!=null){
            return context.getText();
        } else {
            return "<missing>";
        }
    }
}
