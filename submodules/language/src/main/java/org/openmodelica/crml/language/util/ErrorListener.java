package org.openmodelica.crml.language.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;

/**
 * Collects the errors of the lexer and the parser as {@link CrmlDiagnostic}s
 * of kind {@link CrmlDiagnostic.Kind#SYNTAX}, located at the source name of
 * the parsed stream ({@code CharStreams.fromString(text, sourceName)}).
 */
public class ErrorListener extends BaseErrorListener {
    private final List<CrmlDiagnostic> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        errors.add(new CrmlDiagnostic(CrmlDiagnostic.Kind.SYNTAX, msg, sourceName(recognizer), line, charPositionInLine));
    }

    /** The source name of the stream, or null if it has none. */
    private static String sourceName(Recognizer<?, ?> recognizer) {
        IntStream input = recognizer == null ? null : recognizer.getInputStream();
        String name = input == null ? null : input.getSourceName();
        return name == null || IntStream.UNKNOWN_SOURCE_NAME.equals(name) ? null : name;
    }

    public CRMLSyntaxResults getErrors() {
        return new CRMLSyntaxResults(errors);
    }

    /** The syntax errors of one parse, in the order they were found. */
    public static class CRMLSyntaxResults {
        private final List<CrmlDiagnostic> errors;

        public CRMLSyntaxResults(List<CrmlDiagnostic> errors) {
            this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        }

        public List<CrmlDiagnostic> errors() { return errors; }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}
