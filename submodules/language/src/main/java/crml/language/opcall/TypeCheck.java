package crml.language.opcall;

import crml.model.language.TypeReference;
import crml.model.language.Value;

public interface TypeCheck {
    default TypeReference infer(Value v){
        return null;
    }
    default boolean check(Value v, TypeReference expected){
        return true;
    }
}
