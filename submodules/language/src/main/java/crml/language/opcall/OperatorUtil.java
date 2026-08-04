package crml.language.opcall.ai;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

import crml.language.opcall.Association;
import crml.model.language.CustomOperator;
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
}

