package crml.compiler.crmlcv2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.LanguageFactory;
import crml.model.language.Model;
import crml.model.language.Set;
import crml.model.language.Value;
import crml.model.language.Variable;
import crml.model.trace.TraceLink;
import crml.model.trace.TraceLinkKind;

/**
 * CRML classes and sets.
 *
 * <p>Both are unreachable from the test corpus: every model under testModels
 * that declares a class fails to parse (Contract, TwoTanks,
 * CoolingSystem_flattened_simple, ProbabilityExample1, SetOperatorsExample3 and
 * -7, Reqs_sri_CRML), so these run on hand-built object models.
 */
public class ClassAndSetTest {

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

    private static Model model(String name) {
        Model model = F.createModel();
        model.setName(name);
        return model;
    }

    /** A class is a model, not a record: its members' definitions are equations. */
    @Test
    public void aClassBecomesANestedModelWithAnEquationSection() {
        crml.model.language.Class clazz = F.createClass();
        clazz.setName("Pump");
        clazz.getVariables().add(variable("cav", BuiltinType.BOOLEAN, null));

        crml.model.language.BooleanConstant constant = F.createBooleanConstant();
        constant.setValue(crml.model.language.BooleanLiteral.TRUE);
        clazz.getVariables().add(variable("nostart", BuiltinType.BOOLEAN, constant));

        Model model = model("M");
        model.getClasses().add(clazz);

        String text = new OMCv2().translate(model);

        assertTrue(text.contains("model Pump"), text);
        assertFalse(text.contains("record Pump"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.Boolean4 cav;"), text);
        assertTrue(text.contains("CRMLtoModelica.Types.Boolean4 nostart;"), text);
        assertTrue(text.contains("nostart = CRMLtoModelica.Types.Boolean4.true4;"), text);
        assertTrue(text.contains("end Pump;"), text);
    }

    @Test
    public void aPartialClassIsAPartialModelAndSuperClassesBecomeExtends() {
        crml.model.language.Class system = F.createClass();
        system.setName("System");
        system.setPartial(Boolean.TRUE);

        crml.model.language.Class ecs = F.createClass();
        ecs.setName("ECS");
        ecs.getSuperClasses().add(system);

        Model model = model("M");
        model.getClasses().add(system);
        model.getClasses().add(ecs);

        String text = new OMCv2().translate(model);

        assertTrue(text.contains("partial model System"), text);
        assertTrue(text.contains("model ECS"), text);
        assertTrue(text.contains("extends System;"), text);
    }

    @Test
    public void aClassIsTraced() {
        crml.model.language.Class clazz = F.createClass();
        clazz.setName("Pump");
        Model model = model("M");
        model.getClasses().add(clazz);

        TranslationResult result = new OMCv2().translateModel(model);

        boolean traced = false;
        for (TraceLink link : result.trace().getLinks()) {
            if (link.getCrml() == clazz && link.getKind() == TraceLinkKind.CLASS_TO_MODEL) {
                traced = true;
            }
        }
        assertTrue(traced, "no CLASS_TO_MODEL link");
    }

    /** A class-typed variable declares a component of the nested model. */
    @Test
    public void aClassTypedVariableDeclaresAComponentOfTheNestedModel() {
        crml.model.language.Class clazz = F.createClass();
        clazz.setName("ECS");

        crml.model.language.UserTypereference classType = F.createUserTypereference();
        classType.setDomain(clazz);

        Variable instance = F.createVariable();
        instance.setName("ecs");
        instance.setDomain(classType);

        Model model = model("M");
        model.getClasses().add(clazz);
        model.getVariables().add(instance);

        String text = new OMCv2().translate(model);
        assertTrue(text.contains("ECS ecs;"), text);
    }

    /**
     * A set becomes an array typed by its elements, not by the declared CRML
     * domain: "Periods P3 is { P1, P2, Pn }" is an array of Period.
     */
    @Test
    public void aSetValuedVariableBecomesAnArrayOfItsElementType() {
        Variable p1 = variable("P1", BuiltinType.PERIOD, null);
        Variable p2 = variable("P2", BuiltinType.PERIOD, null);

        Set<Value> set = F.<Value>createSet();
        set.getElements().add(refTo(p1));
        set.getElements().add(refTo(p2));

        Model model = model("M");
        model.getVariables().add(p1);
        model.getVariables().add(p2);
        model.getVariables().add(variable("P3", BuiltinType.PERIODS, set));

        TranslationResult result = new OMCv2().translateModel(model);

        assertTrue(result.text().contains("CRMLtoModelica.Types.CRMLPeriod P3[2] = {P1, P2};"), result.text());
        // The one library consumer of a set has an empty body; say so.
        assertTrue(result.diagnostics().toString().contains("setAnd"), result.diagnostics().toString());
    }

    @Test
    public void aSetDeclaredOnTheModelBecomesAnArrayComponent() {
        Variable p1 = variable("P1", BuiltinType.PERIOD, null);

        Set<Value> set = F.<Value>createSet();
        set.setName("S");
        set.getElements().add(refTo(p1));

        Model model = model("M");
        model.getVariables().add(p1);
        model.getSets().add(set);

        String text = new OMCv2().translate(model);
        assertTrue(text.contains("CRMLtoModelica.Types.CRMLPeriod S[1] = {P1};"), text);
    }

    private static crml.model.language.VariableReference refTo(Variable variable) {
        crml.model.language.VariableReference reference = F.createVariableReference();
        reference.setVariable(variable);
        return reference;
    }
}
