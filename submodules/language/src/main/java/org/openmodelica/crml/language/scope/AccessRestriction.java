package org.openmodelica.crml.language.scope;

/**
 * Why a declaration that a lookup finds cannot be used where the name is
 * written. Such a declaration still hides farther ones of its name, so the
 * name resolves to {@link Resolution.Status#NOT_ACCESSIBLE} with the reason,
 * not to another declaration. Kept as a reason, so that later rules (for
 * example {@code protected} members) can be added.
 */
public enum AccessRestriction {

    /**
     * A variable, object or set of a namespace around the one the name is
     * written in that is not a constant. By name, a class, an operator or a
     * root sees only its own and its inherited variables, and only the
     * constants of the namespaces around it; the Modelica code generated for
     * a class or an operator cannot see the rest either.
     */
    ENCLOSING_NON_CONSTANT("of the namespaces around this one, only constants can be used");

    private final String explanation;

    AccessRestriction(String explanation) {
        this.explanation = explanation;
    }

    /** Why the declaration cannot be used, as a phrase for a diagnostic. */
    public String explanation() {
        return explanation;
    }
}
