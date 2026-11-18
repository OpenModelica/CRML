package org.openmodelica.crml.language.scope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.workspace.CrmlDiagnostic;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.CrmlWorkspace;
import org.openmodelica.crml.model.base.MemberKind;
import org.openmodelica.crml.model.metamodel.Binding;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.model.metamodel.ConstructorValue;
import org.openmodelica.crml.model.metamodel.CustomOperator;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.Sequence;
import org.openmodelica.crml.model.metamodel.UserTypereference;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;

/** The rules of {@link Scopes}; one test per rule of section 3 of {@code SCOPING_SPEC.md}. */
public class ScopesTest {

    private static final CRMLPackage P = CRMLPackage.eINSTANCE;
    private final Scopes scopes = new Scopes();

    // ---------------------------------------------------------------------
    // list
    // ---------------------------------------------------------------------

    @Test
    void listsTheNearestNamespaceFirst() {
        CrmlResource r = resolved("model M is {\n"
            + "Integer m is external;\n"
            + "class C is { Integer c is external; Integer d is c; };\n"
            + "};");
        EObject context = variable(r, "d").getDefinition();

        assertEquals("[c, m]", names(scopes.list(context, P.getVariable(), ScopeMode.RESOLUTION)),
            "the class's own variables, then the model's; d itself is left out");
    }

    @Test
    void aNearerDeclarationHidesAFartherOneOfTheSameKind() {
        CrmlResource r = resolved("model M is {\n"
            + "Integer x is external;\n"
            + "class C is { Integer x is external; Integer y is x; };\n"
            + "};");
        Variable classX = (Variable) ((Class) find(r, Class.class, "C")).getVariables().get(0);

        assertEquals("[x]", names(scopes.list(variable(r, "y"), P.getVariable(), ScopeMode.RESOLUTION)),
            "the class's x; the model's x is hidden, y itself left out");
        assertSame(classX, ((VariableReference) variable(r, "y").getDefinition()).getVariable());
    }

    @Test
    void hidingIsPerKind() {
        // The class's variable Pump does not hide the model's class Pump.
        CrmlResource r = resolved("model M is {\n"
            + "class Pump is { Integer id is external; };\n"
            + "class C is { Integer Pump is external; Pump p; };\n"
            + "};");

        assertTrue(r.getErrors().isEmpty(), () -> "errors: " + r.getErrors());
        UserTypereference type = (UserTypereference) variable(r, "p").getDomain();
        assertSame(find(r, Class.class, "Pump"), type.getDomain());
    }

    @Test
    void filtersByKind() {
        CrmlResource r = resolved("model M is {\n"
            + "class Pump is { Integer id is external; };\n"
            + "Integer x is external;\n"
            + "Integer y is x;\n"
            + "};");
        EObject context = variable(r, "y").getDefinition();

        assertEquals("[Pump]", names(scopes.list(context, P.getClass_(), ScopeMode.RESOLUTION)));
        assertEquals("[x]", names(scopes.list(context, P.getVariable(), ScopeMode.RESOLUTION)));
    }

    @Test
    void builtInsOnlyInContentAssist() {
        CrmlResource r = resolved("model M is { Integer x is external; Integer y is x; };");
        EObject context = variable(r, "y").getDefinition();

        assertTrue(scopes.list(context, P.getDomain(), ScopeMode.RESOLUTION).stream().noneMatch(ScopeEntry::builtIn));
        List<ScopeEntry> types = scopes.list(context, P.getDomain(), ScopeMode.CONTENT_ASSIST);
        assertTrue(types.stream().anyMatch(e -> e.builtIn() && e.name().equals("Boolean") && e.kind() == MemberKind.TYPE),
            names(types));
        List<ScopeEntry> operators = scopes.list(context, P.getOperator(), ScopeMode.CONTENT_ASSIST);
        assertTrue(operators.stream().anyMatch(e -> e.builtIn() && e.name().equals("and")), names(operators));
        assertTrue(scopes.list(context, P.getVariable(), ScopeMode.CONTENT_ASSIST).stream().noneMatch(ScopeEntry::builtIn),
            "no built-ins where a variable is expected");
    }

    // ---------------------------------------------------------------------
    // resolve
    // ---------------------------------------------------------------------

    @Test
    void twoDependenciesDeclaringTheNameAreAmbiguous() {
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("library A is { class X is { Integer i is external; }; };");
        ws.load("library B is { class X is { Integer i is external; }; };");
        CrmlResource m = ws.load("model M is A union B union {\nX x;\n};");
        ws.resolve();

        Resolution resolution = scopes.resolve(variable(m, "x").getDomain(), P.getDomain(), "X");
        assertEquals(Resolution.Status.AMBIGUOUS, resolution.status());
        assertEquals(2, resolution.candidates().size());
        assertEquals("[AMBIGUOUS_REFERENCE]", kinds(m));
        String message = m.getErrors().get(0).getMessage();
        assertTrue(message.contains("Class X of A") && message.contains("Class X of B"), message);
    }

    @Test
    void anOwnDeclarationHidesTheDependencies() {
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("library A is { class X is { Integer i is external; }; };");
        CrmlResource m = ws.load("model M is A union {\nclass X is { Integer j is external; };\nX x;\n};");
        ws.resolve();

        assertTrue(m.getErrors().isEmpty(), () -> "errors: " + m.getErrors());
        assertSame(find(m, Class.class, "X"), ((UserTypereference) variable(m, "x").getDomain()).getDomain());
    }

    @Test
    void aVariableCannotReferToItself() {
        CrmlResource r = resolved("model M is {\nBoolean x is x;\n};");

        assertEquals("[SELF_REFERENCE]", kinds(r));
        assertEquals(2, r.getErrors().get(0).getLine());
    }

    @Test
    void aSelfReferenceIsNotResolvedToAnOuterDeclaration() {
        CrmlResource r = resolved("model M is {\n"
            + "Boolean x is external;\n"
            + "class C is { Boolean x is x; };\n"
            + "};");

        assertEquals("[SELF_REFERENCE]", kinds(r));
        Variable classX = ((Class) find(r, Class.class, "C")).getVariables().get(0);
        assertEquals(Resolution.Status.SELF_REFERENCE,
            scopes.resolve(classX.getDefinition(), P.getVariable(), "x").status());
    }

    @Test
    void anOperatorBodySeesTheModelsVariables() {
        CrmlResource r = resolved("model M is {\n"
            + "Boolean x is external;\n"
            + "Operator [ Boolean ] 'f' Boolean a = a and x;\n"
            + "Boolean y is 'f' x;\n"
            + "};");

        assertTrue(r.getErrors().isEmpty(), () -> "errors: " + r.getErrors());
    }

    @Test
    void anOperatorCannotCallItselfInItsBody() {
        CrmlResource r = resolved("model M is {\nTemplate b1 'or' b2 = b1 'or' b2;\n};");

        CustomOperator or = (CustomOperator) ((Library) r.getRoot()).getOperators().get(0);
        assertTrue(or.getDefinition() instanceof Sequence, "the call to itself stays an unresolved Sequence");
    }

    @Test
    void constructorBindingsNameMembersOfTheClassIncludingInheritedOnes() {
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("library Equipment is {\n"
            + "partial class Machine is { String id; };\n"
            + "};");
        CrmlResource m = ws.load("model M is Equipment union {\n"
            + "class Pump is { Boolean cav is external; } extends Machine;\n"
            + "Pump pump1 is new Pump (id = \"P1\");\n"
            + "};");
        ws.resolve();

        assertTrue(m.getErrors().isEmpty(), () -> "errors: " + m.getErrors());
        Binding binding = ((ConstructorValue) variable(m, "pump1").getDefinition()).getBindings().get(0);
        assertEquals("Machine", ((Class) binding.getElement().eContainer()).getName());
        assertEquals("[cav, id]", names(scopes.list(binding, P.getElement(), ScopeMode.RESOLUTION,
            new ConstructorBindingScopeStrategy())), "the class's own member, then the inherited one; no upward walk");
    }

    @Test
    void notFound() {
        assertNull(scopes.resolve(variable(resolved("model M is { Integer y is external; };"), "y"),
            P.getVariable(), "nothing").target());
    }

    // ---------------------------------------------------------------------
    // operators for the mixfix parser
    // ---------------------------------------------------------------------

    @Test
    void operatorsOwnFirstThenDependencies() {
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("library Logic is {\nTemplate b1 'or' b2 = not (not b1 and not b2);\n};");
        CrmlResource m = ws.load("model M is Logic union {\n"
            + "Template b1 'implies' b2 = not b1 'or' b2;\n"
            + "Operator [ Boolean ] 'negation of' Boolean b = not b;\n"
            + "};");
        ws.resolve();

        List<String> signatures = new ArrayList<>();
        for (CustomOperator operator : scopes.operators((Library) m.getRoot())) {
            signatures.add(org.openmodelica.crml.model.base.NamespaceSupport.hidingKey(operator));
        }
        assertEquals("[_ 'implies' _, 'negation of' _, _ 'or' _]", signatures.toString());
    }

    @Test
    void anOwnOperatorHidesALibraryOperatorWithTheSameSignature() {
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("library Logic is {\nTemplate b1 'or' b2 = not (not b1 and not b2);\n};");
        CrmlResource m = ws.load("model M is Logic union {\n"
            + "Template a 'or' b = a or b;\n"
            + "Boolean x is external;\n"
            + "Boolean y is x 'or' x;\n"
            + "};");
        ws.resolve();

        List<CustomOperator> operators = scopes.operators((Library) m.getRoot());
        assertEquals(1, operators.size());
        assertSame(m, operators.get(0).eResource());
        assertTrue(m.getErrors().isEmpty(), () -> "errors: " + m.getErrors());
        assertFalse(variable(m, "y").getDefinition() instanceof Sequence);
    }

    @Test
    void operatorsWithoutKeywordsAreNotGivenToTheMixfixParser() {
        // Keywordless operators, own and inherited: the mixfix parser has no
        // keyword to anchor on, so they must stay out of its list (they would
        // match juxtaposed values, or loop on left recursion).
        CrmlWorkspace ws = new CrmlWorkspace();
        ws.load("library Lib is {\n"
            + "Template a b c = a and b and c;\n"
            + "Operator f is Operator [ Boolean ] (Boolean a) = not a;\n"
            + "};");
        CrmlResource m = ws.load("model M is Lib union {\n"
            + "Operator [ Boolean ] Boolean a Boolean b = a or b;\n"
            + "Template b1 'or' b2 = not (not b1 and not b2);\n"
            + "Boolean x is external;\n"
            + "Boolean y is x 'or' x;\n"
            + "};");

        assertTimeoutPreemptively(java.time.Duration.ofSeconds(10), ws::resolve);

        assertEquals(4, ((Library) m.getRoot()).getAllOperators().size(), "all four are in scope");
        List<CustomOperator> mixfix = scopes.operators((Library) m.getRoot());
        assertEquals(1, mixfix.size(), "only the operator with a keyword");
        assertEquals("_ 'or' _", org.openmodelica.crml.model.base.NamespaceSupport.hidingKey(mixfix.get(0)));
        assertTrue(m.getErrors().isEmpty(), () -> "errors: " + m.getErrors());
        assertFalse(variable(m, "y").getDefinition() instanceof Sequence);
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private static CrmlResource resolved(String text) {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource r = ws.load(text);
        ws.resolve();
        return r;
    }

    private static Variable variable(CrmlResource r, String name) {
        return (Variable) find(r, Variable.class, name);
    }

    private static EObject find(CrmlResource r, java.lang.Class<?> type, String name) {
        TreeIterator<EObject> all = r.getAllContents();
        while (all.hasNext()) {
            EObject object = all.next();
            if (type.isInstance(object) && name.equals(object.eGet(P.getNamedEntity_Name()))) {
                return object;
            }
        }
        throw new AssertionError("no " + type.getSimpleName() + " " + name);
    }

    private static String names(List<ScopeEntry> entries) {
        return entries.stream().map(ScopeEntry::name).collect(Collectors.toList()).toString();
    }

    private static String kinds(CrmlResource r) {
        List<String> kinds = new ArrayList<>();
        for (Resource.Diagnostic d : r.getErrors()) {
            kinds.add(((CrmlDiagnostic) d).getKind().name());
        }
        return kinds.toString();
    }
}
