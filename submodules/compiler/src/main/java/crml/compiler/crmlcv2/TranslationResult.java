package crml.compiler.crmlcv2;

import java.util.Collections;
import java.util.List;

import crml.model.modelica.ClassDefinition;
import crml.model.trace.TraceModel;
import crml.modelica.print.ModelicaPrinter;

/**
 * Everything one CRML model's translation produced: the Modelica object model,
 * the CRML-to-Modelica trace, and the diagnostics for whatever could not be
 * mapped.
 */
public final class TranslationResult {

    private final ClassDefinition modelica;
    private final TraceModel trace;
    private final List<Diagnostic> diagnostics;

    public TranslationResult(ClassDefinition modelica, TraceModel trace, List<Diagnostic> diagnostics) {
        this.modelica = modelica;
        this.trace = trace;
        this.diagnostics = Collections.unmodifiableList(diagnostics);
    }

    public ClassDefinition modelica() {
        return modelica;
    }

    public TraceModel trace() {
        return trace;
    }

    public List<Diagnostic> diagnostics() {
        return diagnostics;
    }

    /** The generated .mo text for this model. */
    public String text() {
        return ModelicaPrinter.print(modelica);
    }

    public boolean hasErrors() {
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.severity() == Diagnostic.Severity.ERROR) {
                return true;
            }
        }
        return false;
    }
}
