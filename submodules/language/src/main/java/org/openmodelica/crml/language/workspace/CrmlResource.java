package org.openmodelica.crml.language.workspace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.openmodelica.crml.language.dom.DOMVisitor;
import org.openmodelica.crml.language.util.Parser;

/**
 * One CRML file: its parse result, its header and the root of its DOM
 * (a {@code Model} or {@code Library}).
 *
 * <p>Loading parses and builds only. The root is not linked: references into
 * other resources cannot be resolved before every file of the run is loaded.
 * The {@link CrmlWorkspace} links it later, see {@link #getState()}.
 *
 * <p>A file with syntax or build errors still gets a resource ("load as
 * much as possible"): its errors are in {@link #getErrors()}, its
 * {@link #getHeader() header} may be readable, and it has a partial DOM if
 * the parser could recover enough of it: what could not be built is left
 * out (see {@link #isComplete()}). Its {@link #getSourceMap() source map}
 * tells where each object of the DOM is in the text.
 */
public class CrmlResource extends ResourceImpl {

    /** How far this resource has got in the pipeline. */
    public enum State {
        /** Not loaded yet. */
        NEW,
        /**
         * Parsed with syntax errors. The DOM, if any, is partial: built from
         * what the parser recovered. It is resolved like a complete one, but
         * the state stays.
         */
        SYNTAX_ERRORS,
        /**
         * Parsed, but building the DOM reported errors. The DOM, if any, is
         * partial: it lacks what could not be built. It is resolved like a
         * complete one, but the state stays.
         */
        BUILD_FAILED,
        /** Complete DOM built, not linked yet. */
        BUILT,
        /** Complete DOM linked, operator calls resolved, lifted. Done. */
        RESOLVED
    }

    private State state = State.NEW;
    /** What diagnostics name this file by; the URI if not set. */
    private String location;
    private Parser.ParserResult parsed;
    private RootHeader header;
    private SourceMap sourceMap;
    /** True once the workspace resolved the DOM, complete or partial. */
    private boolean resolved;

    /**
     * The visitor that built the DOM. It holds the queued link tasks until
     * the workspace links this resource; dropped afterwards.
     */
    private DOMVisitor visitor;

    public CrmlResource(URI uri) {
        super(uri);
    }

    @Override
    protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
        parsed = new Parser().parse(CharStreams.fromString(read(inputStream), location()));
        header = RootHeader.read(parsed.ast());
        boolean syntaxErrors = parsed.syntax().hasErrors();
        getErrors().addAll(parsed.syntax().errors());

        // Built from what the parser recovered if there are syntax errors.
        sourceMap = new SourceMap((BufferedTokenStream) parsed.parser().getTokenStream());
        visitor = new DOMVisitor(sourceMap);
        EObject root = visitor.buildRoot(parsed.ast());
        List<CrmlDiagnostic> buildErrors = visitor.takeErrors();
        getErrors().addAll(buildErrors);
        sourceMap.collectHoles(parsed.ast());
        eAdapters().add(sourceMap);

        if(syntaxErrors){
            state = State.SYNTAX_ERRORS;
        } else {
            if(buildErrors.isEmpty()){
                state = State.BUILT;
            } else {
                state = State.BUILD_FAILED;
            }
        }
        if (root == null) {
            visitor = null;
            return;
        }
        getContents().add(root);
    }

    /**
     * The whole stream as UTF-8 text. Read by hand rather than with
     * {@code CharStreams.fromStream}, which needs {@code java.nio.channels}:
     * the web build (TeaVM) does not have it.
     */
    private static String read(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) {
            bytes.write(buffer, 0, n);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    protected void doUnload() {
        super.doUnload();//Note: It changes some objects to Proxies, so that might be an issue with reloading content.
        state = State.NEW;
        parsed = null;
        header = null;
        visitor = null;
        resolved = false;
        if (sourceMap != null) {
            eAdapters().remove(sourceMap);
            sourceMap = null;
        }
    }

    /** The root of the DOM, or null if the file did not get that far. */
    public EObject getRoot() {
        return getContents().isEmpty() ? null : getContents().get(0);
    }

    public State getState() { return state; }

    /**
     * True if the file was loaded without syntax or build errors, so its DOM
     * (if any) has all it declares. A file that is not complete is
     * {@link RootIndex.Lookup.Status#BROKEN broken} for its dependents, which
     * still see what its partial DOM has.
     */
    public boolean isComplete() {
        return state == State.BUILT || state == State.RESOLVED;
    }

    /** True if there is a DOM the workspace has not resolved yet. */
    public boolean needsResolving() {
        return getRoot() != null && !resolved;
    }

    /** Where each object of the DOM is in the text; null before loading. */
    public SourceMap getSourceMap() { return sourceMap; }

    /** The parse result, or null before loading. */
    public Parser.ParserResult getParsed() { return parsed; }

    /** What the file declares, or null if not even the header could be read. */
    public RootHeader getHeader() { return header; }

    /**
     * Records an error found by a stage after loading, such as the
     * translation to Modelica, at the position {@code at} was built from, or
     * that of its closest container that has one. The state of the resource
     * does not change. An equal error recorded before is not added again, so
     * running the stage twice reports it once.
     *
     * @return the recorded error
     */
    public CrmlDiagnostic reportError(EObject at, CrmlDiagnostic.Kind kind, String message) {
        Token start = sourceMap == null ? null : sourceMap.position(at);
        CrmlDiagnostic diagnostic = start == null
            ? new CrmlDiagnostic(kind, message, location(), 0, 0)
            : new CrmlDiagnostic(kind, message, location(), start.getLine(), start.getCharPositionInLine());
        for (Resource.Diagnostic existing : getErrors()) {
            if (existing instanceof CrmlDiagnostic && sameAs((CrmlDiagnostic) existing, diagnostic)) {
                return (CrmlDiagnostic) existing;
            }
        }
        getErrors().add(diagnostic);
        return diagnostic;
    }

    private static boolean sameAs(CrmlDiagnostic a, CrmlDiagnostic b) {
        return a.getKind() == b.getKind() && a.getLine() == b.getLine() && a.getColumn() == b.getColumn()
            && Objects.equals(a.getMessage(), b.getMessage())
            && Objects.equals(a.getLocation(), b.getLocation());
    }

    /**
     * The visitor holding the pending link tasks. Only for the workspace;
     * null once resolved or if building failed.
     */
    DOMVisitor getVisitor() { return visitor; }

    /** Called by the workspace once linking and operator-call resolution are done. */
    void markResolved() {
        if (state == State.BUILT) {
            state = State.RESOLVED;
        }
        resolved = true;
        visitor = null;
    }

    /**
     * The location the diagnostics of this resource report: the one set with
     * {@link #setLocation}, else its URI.
     */
    public String location() {
        if (location != null) {
            return location;
        }
        return getURI() == null ? null : getURI().toString();
    }

    /**
     * Sets the location diagnostics report, e.g. the path as the user gave it
     * rather than the absolute URI. Set it before loading: the parser and the
     * DOM builder take it when the file is loaded.
     */
    void setLocation(String location) {
        this.location = location;
    }
}
