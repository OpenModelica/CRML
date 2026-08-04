package crml.language.opcall;

import crml.model.language.Keyword;
import crml.model.language.Template;
import crml.model.language.UserOperator;

public enum Fix {
    CLOSED, // Starts and ends with a keyword
    PREFIX, // Starts with a keyword, ends with expression
    POSTFIX, // Starts with an expression, ends with a keyword
    INFIX; // Starts and ends with an expression
    
    public static Fix get(UserOperator op){
        boolean startkw = op.getHeader().get(0) instanceof Keyword;
        boolean endkw = op.getHeader().get(op.getHeader().size()-1) instanceof Keyword;
        if(startkw && endkw){
            return CLOSED;
        } else if(startkw && !endkw) {
            return PREFIX;
        } else if(!startkw && endkw) {
            return POSTFIX;
        } else if(!startkw && !endkw) {
            return INFIX;
        } else {
            throw new IllegalStateException("Unreachabvel.");
        }
    }

    public static Fix get(Template op){
        boolean startkw = op.getHeader().get(0) instanceof Keyword;
        boolean endkw = op.getHeader().get(op.getHeader().size()-1) instanceof Keyword;
        if(startkw && endkw){
            return CLOSED;
        } else if(startkw && !endkw) {
            return PREFIX;
        } else if(!startkw && endkw) {
            return POSTFIX;
        } else if(!startkw && !endkw) {
            return INFIX;
        } else {
            throw new IllegalStateException("Unreachabvel.");
        }
    }

}
