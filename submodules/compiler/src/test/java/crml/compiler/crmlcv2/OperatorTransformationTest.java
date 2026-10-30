package crml.compiler.crmlcv2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import crml.model.language.Binding;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.BuiltinUnaryOperatorKind;
import crml.model.language.ComputedValue;
import crml.model.language.ConstructorValue;
import crml.model.language.CustomOperator;
import crml.model.language.Keyword;
import crml.model.language.LanguageFactory;
import crml.model.language.Model;
import crml.model.language.UnaryOperator;
import crml.model.language.UserOperator;
import crml.model.language.Value;
import crml.model.language.Variable;
import crml.model.language.VariableReference;
import crml.model.trace.TraceLink;
import crml.model.trace.TraceLinkKind;

/**
 * Templates and user operators become nested Modelica classes, and their calls
 * become function calls or block instantiations depending on whether the
 * operator's body hoists anything.
 */
public class OperatorTransformationTest {

    private static final LanguageFactory F = LanguageFactory.eINSTANCE;

    private static BuiltinTypeReference type(BuiltinType builtinType) {
        BuiltinTypeReference reference = F.createBuiltinTypeReference();
        reference.setBuiltinType(builtinType);
        return reference;
    }

    private static Variable variable(String name, BuiltinType domain, Value definition) {
        Variable variable = F.createVariable();
        variable.setName(name);
        variable.setDomain(type(domain));
        variable.setDefinition(definition);
        return variable;
    }

    private static VariableReference refTo(Variable variable) {
        VariableReference reference = F.createVariableReference();
        reference.setVariable(variable);
        return reference;
    }

    private static Keyword keyword(String text) {
        Keyword kw = F.createKeyword();
        kw.setKeyword(text);
        return kw;
    }

    private static UnaryOperator not(Value operand) {
        UnaryOperator op = F.createUnaryOperator();
        op.setOptype(BuiltinUnaryOperatorKind.NOT);
        op.setValue(operand);
        return op;
    }

    private static ComputedValue call(CustomOperator operator, Value... arguments) {
        ComputedValue computed = F.createComputedValue();
        computed.setOperator(operator);
        for (int i = 0; i < arguments.length; i++) {
            Binding binding = F.createBinding();
            binding.setElement(operator.getVariables().get(i));
            binding.setValue(arguments[i]);
            computed.getBindings().add(binding);
        }
        return computed;
    }

    /** "Operator [ Boolean ] Boolean b 'negated' = not b;" - a pure body. */
    private static UserOperator negatedOperator() {
        UserOperator operator = F.createUserOperator();
        operator.setDomain(type(BuiltinType.BOOLEAN));
        Variable parameter = variable("b", BuiltinType.BOOLEAN, null);
        operator.getHeader().add(parameter);
        operator.getHeader().add(keyword("'negated'"));
        operator.setDefinition(not(refTo(parameter)));
        return operator;
    }

    /** "Operator [ Clock ] Boolean b 'becomes false' = new Clock (not b);" - hoists. */
    private static UserOperator becomesFalseOperator() {
        UserOperator operator = F.createUserOperator();
        operator.setDomain(type(BuiltinType.CLOCK));
        Variable parameter = variable("b", BuiltinType.BOOLEAN, null);
        operator.getHeader().add(parameter);
        operator.getHeader().add(keyword("'becomes false'"));

        ConstructorValue constructor = F.createConstructorValue();
        constructor.setDomain(type(BuiltinType.CLOCK));
        constructor.setValue(not(refTo(parameter)));
        operator.setDefinition(constructor);
        return operator;
    }

    @Test
    public void aPureOperatorBecomesAFunctionAndItsCallAFunctionCall() {
        UserOperator operator = negatedOperator();
        Variable b1 = variable("b1", BuiltinType.BOOLEAN, null);

        Model model = F.createModel();
        model.setName("M");
        model.getOperators().add(operator);
        model.getVariables().add(b1);
        model.getVariables().add(variable("b2", BuiltinType.BOOLEAN, call(operator, refTo(b1))));

        String text = new OMCv2().translate(model);

        assertTrue(text.contains("function op_negated"), text);
        assertTrue(text.contains("input CRMLtoModelica.Types.Boolean4 b;"), text);
        assertTrue(text.contains("output CRMLtoModelica.Types.Boolean4 out;"), text);
        assertTrue(text.contains("out := CRMLtoModelica.Functions.not4(b);"), text);
        assertTrue(text.contains("end op_negated;"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.Boolean4 b2 = op_negated(b1);"), text);
    }

    @Test
    public void anOperatorWhoseBodyHoistsBecomesABlockAndItsCallAnInstantiation() {
        UserOperator operator = becomesFalseOperator();
        Variable b1 = variable("b1", BuiltinType.BOOLEAN, null);

        Model model = F.createModel();
        model.setName("BecomesFalse");
        model.getOperators().add(operator);
        model.getVariables().add(b1);
        model.getVariables().add(variable("c", BuiltinType.CLOCK, call(operator, refTo(b1))));

        String text = new OMCv2().translate(model);

        assertTrue(text.contains("block op_becomes_false"), text);
        assertFalse(text.contains("function op_becomes_false"), text);
        assertTrue(text.contains("input CRMLtoModelica.Types.Boolean4 b;"), text);
        assertTrue(text.contains("output CRMLtoModelica.Types.CRMLClock out;"), text);
        // The constructor's two declarations hoisted into the block's body.
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock c1(b = CRMLtoModelica.Functions.not4(b));"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock_build c1_init(clock = c1);"), text);
        assertTrue(text.contains("out = c1;"), text);
        // The call site instantiates the block and wires its input.
        assertTrue(text.contains("op_becomes_false op_becomes_false_1;"), text);
        assertTrue(text.contains("op_becomes_false_1.b = b1;"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLClock c = op_becomes_false_1.out;"), text);
    }

    @Test
    public void anOperatorIsEmittedOnceHoweverManyCallSites() {
        UserOperator operator = negatedOperator();
        Variable b1 = variable("b1", BuiltinType.BOOLEAN, null);

        Model model = F.createModel();
        model.setName("M");
        model.getOperators().add(operator);
        model.getVariables().add(b1);
        model.getVariables().add(variable("x", BuiltinType.BOOLEAN, call(operator, refTo(b1))));
        model.getVariables().add(variable("y", BuiltinType.BOOLEAN, call(operator, refTo(b1))));

        String text = new OMCv2().translate(model);
        assertEquals(1, countOccurrences(text, "function op_negated"), text);
    }

    /** An operator the model declares is emitted even when nothing calls it. */
    @Test
    public void anUncalledOperatorIsStillEmitted() {
        Model model = F.createModel();
        model.setName("M");
        model.getOperators().add(negatedOperator());

        String text = new OMCv2().translate(model);
        assertTrue(text.contains("function op_negated"), text);
    }

    @Test
    public void aCyclicOperatorIsDiagnosedRatherThanLoopingForever() {
        UserOperator operator = F.createUserOperator();
        operator.setDomain(type(BuiltinType.BOOLEAN));
        Variable parameter = variable("b", BuiltinType.BOOLEAN, null);
        operator.getHeader().add(parameter);
        operator.getHeader().add(keyword("'loops'"));
        operator.setDefinition(call(operator, refTo(parameter)));

        Model model = F.createModel();
        model.setName("M");
        model.getOperators().add(operator);

        TranslationResult result = new OMCv2().translateModel(model);
        assertEquals(1, result.diagnostics().size(), result.diagnostics().toString());
        assertTrue(result.diagnostics().get(0).message().contains("defined in terms of itself"),
            result.diagnostics().toString());
    }

    @Test
    public void anOperatorIsTraced() {
        UserOperator operator = negatedOperator();
        Model model = F.createModel();
        model.setName("M");
        model.getOperators().add(operator);

        TranslationResult result = new OMCv2().translateModel(model);

        boolean traced = false;
        for (TraceLink link : result.trace().getLinks()) {
            if (link.getCrml() == operator && link.getKind() == TraceLinkKind.OPERATOR_TO_FUNCTION) {
                traced = true;
            }
        }
        assertTrue(traced, "no OPERATOR_TO_FUNCTION link");
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        for (int i = text.indexOf(needle); i >= 0; i = text.indexOf(needle, i + needle.length())) {
            count++;
        }
        return count;
    }
}
