package crml.language.pretty;

import crml.model.language.Sequence;
import crml.model.language.SequenceKeyword;
import crml.model.language.SequenceValue;

public class Misc {
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
