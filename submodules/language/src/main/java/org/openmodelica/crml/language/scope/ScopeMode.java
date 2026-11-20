package org.openmodelica.crml.language.scope;

/** What {@link Scopes#list} returns besides the declarations of the DOM. */
public enum ScopeMode {
    /** Declarations only: what a name can resolve to. */
    RESOLUTION,
    /** Declarations, then the built-in types and operators: what to propose. */
    CONTENT_ASSIST
}
