package org.openmodelica.crml.model.base.typing;

/**
 * Whether a value is a single element of its {@link BaseType} or a set of them.
 */
public enum Multiplicity {
    /** One element. */
    SINGLE,
    /** A set of elements, such as a set literal or the result of a lifted application. */
    SET
}
