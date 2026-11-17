package org.openmodelica.crml.language.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.InspectingWorkspace;
import org.openmodelica.crml.language.workspace.InspectingWorkspace.Stage;

/**
 * One CRML file or text taken through a workspace of its own, up to a given
 * stage. The DOM tests use it instead of driving the parser and the
 * {@code DOMVisitor} by hand.
 *
 * <pre>{@code
 * DomRun run = DomRun.of(file, Stage.LINK);
 * run.parsed().syntax();   // the parse
 * run.dom();               // the root, linked
 * run.errors();            // what went wrong while building and linking
 * }</pre>
 */
public final class DomRun {
    private final InspectingWorkspace workspace;
    private final CrmlResource resource;

    private DomRun(InspectingWorkspace workspace, CrmlResource resource) {
        this.workspace = workspace;
        this.resource = resource;
        workspace.resolve();
    }

    /** Loads {@code file} and runs the stages up to and including {@code last}. */
    public static DomRun of(Path file, Stage last) throws IOException {
        InspectingWorkspace workspace = InspectingWorkspace.upTo(last);
        return new DomRun(workspace, workspace.load(file));
    }

    /** Loads CRML text and runs the stages up to and including {@code last}. */
    public static DomRun of(String text, Stage last) {
        InspectingWorkspace workspace = InspectingWorkspace.upTo(last);
        return new DomRun(workspace, workspace.load(text));
    }

    public InspectingWorkspace workspace() { return workspace; }

    public CrmlResource resource() { return resource; }

    /** What each stage did. */
    public InspectingWorkspace.Trace trace() { return workspace.trace(resource); }

    /** The parse result. */
    public Parser.ParserResult parsed() { return resource.getParsed(); }

    /** The root of the DOM, or null if it was not built. */
    public EObject dom() { return resource.getRoot(); }

    /** All errors of the resource: syntax, DOM building and the stages that ran. */
    public List<Resource.Diagnostic> errors() { return new ArrayList<>(resource.getErrors()); }

    /** The errors as text, one per line, for assertion messages. */
    public String errorText() {
        StringBuilder text = new StringBuilder();
        for (Resource.Diagnostic diagnostic : resource.getErrors()) {
            text.append(System.lineSeparator()).append(diagnostic);
        }
        return text.toString();
    }
}
