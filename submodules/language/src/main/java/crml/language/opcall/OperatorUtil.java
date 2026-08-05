package crml.language.opcall;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

import crml.model.language.CustomOperator;
import crml.model.language.Library;
import crml.model.language.Model;

public interface OperatorUtil {
    public static Association getAssoc(CustomOperator operator) {
        return Association.LEFT;
    }
    
    public static List<CustomOperator> load(Model model) {
        List<CustomOperator> operators = new ArrayList<>();
        Iterables.filter(model.getOperators(), CustomOperator.class).forEach(operators::add);
        return operators;
    }

    public static List<CustomOperator> load(Library library) {
        List<CustomOperator> operators = new ArrayList<>();
        Iterables.filter(library.getOperators(), CustomOperator.class).forEach(operators::add);
        return operators;
    }
}

