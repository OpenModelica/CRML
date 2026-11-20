package org.openmodelica.crml.language.pretty;

import org.openmodelica.crml.model.metamodel.Sequence;
import org.openmodelica.crml.model.metamodel.SequenceKeyword;
import org.openmodelica.crml.model.metamodel.SequenceValue;

public class Misc {
    /**
     * This is a static utility class.
     */
    private Misc() {}
    public static String pretty(Sequence s){
        if(s==null){
            return "";
        }
        if(s instanceof SequenceKeyword){
            return "'"+((SequenceKeyword) s).getKeyword()+"'" + " ; " + pretty(s.getNext());
        }
        if(s instanceof SequenceValue){
            return " value" + " ; " + pretty(s.getNext());
        }
        return "";
    }
}
