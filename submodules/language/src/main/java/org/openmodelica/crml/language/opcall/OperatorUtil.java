package org.openmodelica.crml.language.opcall;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.Operator;

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

    /** The operators of {@code model} that the mixfix parser can resolve calls to. */
    public static List<CustomOperator> load(Model model) {
        return mixfix(model.getOperators());
    }

    /** The operators of {@code library} that the mixfix parser can resolve calls to. */
    public static List<CustomOperator> load(Library library) {
        return mixfix(library.getOperators());
    }

    static List<CustomOperator> mixfix(Iterable<? extends Operator> declared) {
        List<CustomOperator> operators = new ArrayList<>();
        for (CustomOperator op : Iterables.filter(declared, CustomOperator.class)) {
            if (isMixfix(op)) {
                operators.add(op);
            }
        }
        return operators;
    }
}
