package crml.language.util;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;

public class ErrorListener extends BaseErrorListener {
    private final List<CRMLSyntaxError> errors = new ArrayList<>();
    
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {

        errors.add(new CRMLSyntaxError(line, charPositionInLine, msg));
    }

    public CRMLSyntaxResults getErrors() {
        return new CRMLSyntaxResults(errors);
    }

    public static record CRMLSyntaxError(int line, int charPosition, String message) {
        @Override
        public String toString() {
            return "ERROR (line " + line + ":" + charPosition + "): " + message;
        }
    }

    public static record CRMLSyntaxResults(List<CRMLSyntaxError> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
    
}