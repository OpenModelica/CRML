package org.openmodelica.crml.language.opcall;

import org.openmodelica.crml.model.metamodel.CustomOperator;

public interface OperatorUtil {
    public static Association getAssoc(CustomOperator operator) {
        return Association.LEFT;
    }

    /**
     * True if calls to the operator can be written in natural-language (mixfix)
     * syntax: its header has at least one keyword. An operator whose header is
     * only parameters gives the mixfix parser nothing to anchor on - it would
     * match juxtaposed values, or nothing at all - so it is called by name
     * instead, which the DOM visitor resolves.
     */
    public static boolean isMixfix(CustomOperator operator) {
        return !operator.getKeywords().isEmpty();
    }

}
