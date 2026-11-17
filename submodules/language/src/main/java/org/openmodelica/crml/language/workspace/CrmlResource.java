package org.openmodelica.crml.language.workspace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.antlr.v4.runtime.CharStreams;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
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
 * <p>A file with syntax errors still gets a resource: its errors are in
 * {@link #getErrors()}, its {@link #getHeader() header} may be readable, but
 * it has no contents ("load as much as possible").
 */
public class CrmlResource extends ResourceImpl {

    /** How far this resource has got in the pipeline. */
    public enum State {
        /** Not loaded yet. */
        NEW,
        /** Parsed with syntax errors: no DOM. */
        SYNTAX_ERRORS,
        /** Parsed, but building the DOM failed: no DOM. */
        BUILD_FAILED,
        /** DOM built, not linked yet. */
        BUILT,
        /** Linked, operator calls resolved, lifted. Done. */
        RESOLVED
    }

    private State state = State.NEW;
    /** What diagnostics name this file by; the URI if not set. */
    private String location;
    private Parser.ParserResult parsed;
    private RootHeader header;

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

        if (parsed.syntax().hasErrors()) {
            getErrors().addAll(parsed.syntax().errors());
            state = State.SYNTAX_ERRORS;
            return;
        }

        visitor = new DOMVisitor();
        EObject root = visitor.buildRoot(parsed.ast());
        getErrors().addAll(visitor.takeErrors());
        if (root == null) {
            visitor = null;
            state = State.BUILD_FAILED;
            return;
        }
        getContents().add(root);
        state = State.BUILT;
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
    }

    /** The root of the DOM, or null if the file did not get that far. */
    public EObject getRoot() {
        return getContents().isEmpty() ? null : getContents().get(0);
    }

    public State getState() { return state; }

    /** The parse result, or null before loading. */
    public Parser.ParserResult getParsed() { return parsed; }

    /** What the file declares, or null if not even the header could be read. */
    public RootHeader getHeader() { return header; }

    /**
     * The visitor holding the pending link tasks. Only for the workspace;
     * null once resolved or if building failed.
     */
    DOMVisitor getVisitor() { return visitor; }

    /** Called by the workspace once linking and operator-call resolution are done. */
    void markResolved() {
        state = State.RESOLVED;
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
