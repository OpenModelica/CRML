package org.openmodelica.crml.language.workspace;

import org.antlr.v4.runtime.Token;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Where an object of the DOM is in the CRML text: the location of its file
 * and the line and column it starts at. Looked up on demand through the
 * {@link SourceMap} of the object's {@link CrmlResource}, so it is never
 * stored with the object or with anything that refers to it.
 */
public final class SourcePosition {

    private final String location;
    private final int line;
    private final int column;

    private SourcePosition(String location, int line, int column) {
        this.location = location;
        this.line = line;
        this.column = column;
    }

    /**
     * The position {@code object} was built from or, if it has none, that of
     * its closest container that has one; null if there is none, e.g. the
     * object was not loaded from a CRML file.
     */
    public static SourcePosition of(EObject object) {
        Resource resource = object == null ? null : object.eResource();
        if (!(resource instanceof CrmlResource)) {
            return null;
        }
        CrmlResource crml = (CrmlResource) resource;
        SourceMap sourceMap = crml.getSourceMap();
        Token start = sourceMap == null ? null : sourceMap.position(object);
        return start == null ? null : new SourcePosition(crml.location(), start.getLine(), start.getCharPositionInLine());
    }

    /** The file, as it was loaded (see {@link CrmlResource#location()}). */
    public String getLocation() {
        return location;
    }

    /** The line, from 1. */
    public int getLine() {
        return line;
    }

    /** The column, from 0. */
    public int getColumn() {
        return column;
    }

    /** {@code location:line:column}, as diagnostics print it. */
    @Override
    public String toString() {
        return location + ":" + line + ":" + column;
    }
}
