package org.openmodelica.crml.language.scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmodelica.crml.model.metamodel.NamedEntity;

/** What a name denotes at a point; see {@link Scopes#resolve}. */
public final class Resolution {

    public enum Status {
        /** Exactly one declaration: {@link #target()}. */
        FOUND,
        /** Several declarations at the same level: {@link #candidates()}. */
        AMBIGUOUS,
        /** Only the declaration the name is written in has the name. */
        SELF_REFERENCE,
        /**
         * The name denotes declarations that cannot be used here, e.g. a
         * variable of the model in an operator body: {@link #candidates()},
         * each with its {@link ScopeEntry#restriction() reason}.
         */
        NOT_ACCESSIBLE,
        /** No declaration. */
        NOT_FOUND
    }

    private final String name;
    private final Status status;
    private final List<ScopeEntry> candidates;

    private Resolution(String name, Status status, List<ScopeEntry> candidates) {
        this.name = name;
        this.status = status;
        this.candidates = Collections.unmodifiableList(new ArrayList<>(candidates));
    }

    /** The result for the declarations {@code matches} named {@code name}. */
    static Resolution of(String name, List<ScopeEntry> matches) {
        if (matches.size() == 1) {
            return new Resolution(name, Status.FOUND, matches);
        }
        return new Resolution(name, matches.isEmpty() ? Status.NOT_FOUND : Status.AMBIGUOUS, matches);
    }

    /** The result for declarations named {@code name} that cannot be used here; not empty. */
    static Resolution notAccessible(String name, List<ScopeEntry> restricted) {
        return new Resolution(name, Status.NOT_ACCESSIBLE, restricted);
    }

    static Resolution selfReference(String name) {
        return new Resolution(name, Status.SELF_REFERENCE, Collections.<ScopeEntry>emptyList());
    }

    /** The name that was resolved. */
    public String name() { return name; }

    public Status status() { return status; }

    public boolean found() { return status == Status.FOUND; }

    /** The declaration if {@link Status#FOUND}, null otherwise. */
    public NamedEntity target() {
        return found() ? candidates.get(0).element() : null;
    }

    /**
     * The matching declarations: one if found, several if ambiguous, those
     * that cannot be used if not accessible, none otherwise.
     */
    public List<ScopeEntry> candidates() { return candidates; }

    @Override
    public String toString() {
        return status + " '" + name + "'" + (candidates.isEmpty() ? "" : " " + candidates);
    }
}
