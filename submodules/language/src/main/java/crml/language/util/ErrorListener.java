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

    public static class CRMLSyntaxError {
        private final int line;
        private final int charPosition;
        private final String message;

        public CRMLSyntaxError(int line, int charPosition, String message) {
            this.line = line;
            this.charPosition = charPosition;
            this.message = message;
        }

        public int line() { return line; }
        public int charPosition() { return charPosition; }
        public String message() { return message; }

        @Override
        public String toString() {
            return "ERROR (line " + line + ":" + charPosition + "): " + message;
        }
    }

    public static class CRMLSyntaxResults {
        private final List<CRMLSyntaxError> errors;

        public CRMLSyntaxResults(List<CRMLSyntaxError> errors) {
            this.errors = errors;
        }

        public List<CRMLSyntaxError> errors() { return errors; }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}