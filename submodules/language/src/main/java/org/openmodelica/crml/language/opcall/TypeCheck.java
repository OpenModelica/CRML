package org.openmodelica.crml.language.opcall;

import org.openmodelica.crml.model.metamodel.TypeReference;
import org.openmodelica.crml.model.metamodel.Value;

public interface TypeCheck {
    default TypeReference infer(Value v){
        return null;
    }
    default boolean check(Value v, TypeReference expected){
        return true;
    }
}
