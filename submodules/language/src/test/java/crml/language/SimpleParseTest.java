package crml.language;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import crml.language.util.Parser;

public class SimpleParseTest {
    String model = """
model TestModel is {
    partial type Quantity is (Real q is rate*u + offset)  {  
        String SIUnit; // SI unit for quantity q 
        String userUnit; // User unit for quantity q 
        Real u; // Quantity q expressed in user units 
        Real rate; // Conversion rate between user units and SI units 
        Real offset; // offset between user units and SI units 
    }; 
    //Quantity x = Quantity (SIUnit = "Pa", userUnit = "bar", rate = 1.e5, offset = 0, u = 3); 
}
""";


String model2 = """
model Example is {
	// Redefine <= for integers from < and ==
	Operator [ Boolean ] Integer n1 '<=' Integer n2 = (n1 < n2) or (n1 == n2);

	// Absolute value of a Real
	Operator [ Real ] 'abs' Real x = if x < 0.0 then -x else x;

	// Concatenate two Strings with a separator
	Operator [ String ] String s1 'joinWith' String s2 = s1 + ", " + s2;

	// Usage examples
	Boolean lessOrEq is 3 <= 5;
	Real magnitude is 'abs' -2.7;
	String result is "hello" 'joinWith' "world";
};
""";

    @Test
    public void test(){
        var parsed = new Parser().parse(model);
        parsed.syntax().errors().forEach(System.out::println);
        assertFalse(parsed.syntax().hasErrors());
    }    
}
