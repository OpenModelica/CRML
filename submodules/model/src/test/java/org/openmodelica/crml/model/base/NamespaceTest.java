package org.openmodelica.crml.model.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.model.metamodel.CRMLFactory;
import org.openmodelica.crml.model.metamodel.CRMLPackage;
import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.model.metamodel.Keyword;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.LibraryDependency;
import org.openmodelica.crml.model.metamodel.Model;
import org.openmodelica.crml.model.metamodel.NamedEntity;
import org.openmodelica.crml.model.metamodel.UserOperator;
import org.openmodelica.crml.model.metamodel.Variable;
import org.openmodelica.crml.model.metamodel.VariableReference;

/** The namespace structure of the metamodel: {@link CrmlObject#getNamespace()} and {@link Namespace}. */
public class NamespaceTest {

    private static final CRMLFactory F = CRMLFactory.eINSTANCE;

    // ---------------------------------------------------------------------
    // Root and getNamespace
    // ---------------------------------------------------------------------

    @Test
    void generatedObjectsAreStillEObjects() {
        Variable variable = F.createVariable();
        assertTrue(variable instanceof EObject);
        assertTrue(variable instanceof CrmlObject);
        assertTrue(CRMLPackage.eINSTANCE.getNamespace().isSuperTypeOf(CRMLPackage.eINSTANCE.getModel()),
            "Namespace is an EClass and a supertype of Model");
    }

    @Test
    void namespaceIsTheNearestStrictlyContainingNamespace() {
        Model model = model("M");
        Class pump = cls("Pump", model);
        Variable flow = variable("flow", pump);
        VariableReference reference = F.createVariableReference();
        flow.setDefinition(reference);

        assertSame(pump, reference.getNamespace(), "a value is in the class it is written in");
        assertSame(pump, flow.getNamespace());
        assertSame(model, pump.getNamespace(), "a class is in its model, not in itself");
        assertNull(model.getNamespace(), "a root has none");
        assertNull(F.createVariableReference().getNamespace(), "nor has an element that is not contained");
    }

    @Test
    void anOperatorIsANamespaceOfItsOwn() {
        Model model = model("M");
        UserOperator neg = F.createUserOperator();
        Keyword keyword = F.createKeyword();
        Variable a = F.createVariable();
        a.setName("a");
        neg.getHeader().addAll(Arrays.asList(keyword, a));
        VariableReference body = F.createVariableReference();
        neg.setDefinition(body);
        model.getOperators().add(neg);

        assertSame(neg, body.getNamespace());
        assertSame(model, neg.getNamespace());
        assertEquals(Arrays.asList(a), neg.getOwnedMembers());
        assertTrue(neg.getBaseNamespaces().isEmpty());
    }

    // ---------------------------------------------------------------------
    // Owned members
    // ---------------------------------------------------------------------

    @Test
    void ownedMembersOfAModelInDeclarationOrder() {
        Model model = model("M");
        Class pump = cls("Pump", model);
        UserOperator op = F.createUserOperator();
        op.setName("op");
        model.getOperators().add(op);
        Variable x = variable("x", model);

        assertEquals(Arrays.asList(pump, op, x), model.getOwnedMembers());
        assertThrows(UnsupportedOperationException.class, () -> model.getOwnedMembers().clear());
    }

    // ---------------------------------------------------------------------
    // Class inheritance
    // ---------------------------------------------------------------------

    @Test
    void classInheritsFromSuperclassesTransitively() {
        Model model = model("M");
        Class base = cls("Base", model);
        Variable id = variable("id", base);
        Class machine = cls("Machine", model);
        machine.getSuperClasses().add(base);
        Variable power = variable("power", machine);
        Class pump = cls("Pump", model);
        pump.getSuperClasses().add(machine);
        Variable flow = variable("flow", pump);

        assertEquals(Arrays.asList(machine), pump.getBaseNamespaces());
        assertEquals(Arrays.asList(power, id), pump.getInheritedMembers());
        assertEquals(Arrays.asList(flow, power, id), pump.getAllVariables());
        assertSame(id, pump.getVariable("id"));
        assertNull(pump.getVariable("missing"));
    }

    @Test
    void aRedeclaredVariableHidesTheInheritedOne() {
        Model model = model("M");
        Class machine = cls("Machine", model);
        variable("power", machine);
        Class pump = cls("Pump", model);
        pump.getSuperClasses().add(machine);
        Variable power = variable("power", pump);

        assertTrue(pump.getInheritedMembers().isEmpty());
        assertEquals(Arrays.asList(power), pump.getAllVariables());
        assertSame(power, pump.getVariable("power"));
    }

    @Test
    void cyclicSuperclassesDoNotHang() {
        Model model = model("M");
        Class a = cls("A", model);
        Variable x = variable("x", a);
        Class b = cls("B", model);
        Variable y = variable("y", b);
        a.getSuperClasses().add(b);
        b.getSuperClasses().add(a);

        assertEquals(Arrays.asList(y), a.getInheritedMembers());
        assertEquals(Arrays.asList(x), b.getInheritedMembers());
    }

    // ---------------------------------------------------------------------
    // Dependencies
    // ---------------------------------------------------------------------

    @Test
    void aRootInheritsTheMembersOfItsBoundDependencies() {
        Library logic = library("Logic");
        Class gate = cls("Gate", logic);
        Model model = model("M");
        dependsOn(model, logic);
        unbound(model, "Missing");

        assertEquals(Arrays.asList(logic), model.getBaseNamespaces(), "the unbound dependency is left out");
        assertEquals(Arrays.asList(gate), model.getInheritedMembers());
        assertEquals(Arrays.asList(gate), model.getAllClasses());
    }

    @Test
    void ownMembersHideDependencyMembersOfTheSameKindOnly() {
        Library lib = library("Lib");
        Class xClass = cls("X", lib);
        Model dependency = model("D");
        variable("y", dependency);
        dependsOn(lib, dependency);
        Model model = model("M");
        Variable ownX = variable("X", model);
        Variable ownY = variable("y", model);
        dependsOn(model, lib);

        assertEquals(Arrays.asList(xClass), model.getInheritedMembers(),
            "the variable X does not hide the class X; the variable y hides D's y");
        assertEquals(Arrays.asList(ownX, ownY), model.getAllVariables());
    }

    @Test
    void transitiveDependenciesAndHidingAlongThePath() {
        // B depends on A; both declare X. M depends on B: it sees B's X only.
        Library a = library("A");
        cls("X", a);
        Class onlyInA = cls("OnlyInA", a);
        Library b = library("B");
        Class bx = cls("X", b);
        dependsOn(b, a);
        Model m = model("M");
        dependsOn(m, b);

        assertEquals(Arrays.asList(bx, onlyInA), m.getInheritedMembers());
    }

    @Test
    void twoDependenciesDeclaringTheSameNameAreBothListed() {
        Library a = library("A");
        Class ax = cls("X", a);
        Library b = library("B");
        Class bx = cls("X", b);
        Model m = model("M");
        dependsOn(m, a);
        dependsOn(m, b);

        assertEquals(Arrays.asList(ax, bx), m.getInheritedMembers(), "for the resolver, an ambiguity");
    }

    @Test
    void aDiamondListsAMemberOnce() {
        Library logic = library("Logic");
        Class gate = cls("Gate", logic);
        Library left = library("Left");
        dependsOn(left, logic);
        Library right = library("Right");
        dependsOn(right, logic);
        Model m = model("M");
        dependsOn(m, left);
        dependsOn(m, right);

        assertEquals(Arrays.asList(gate), m.getInheritedMembers());
    }

    @Test
    void aDirectDependencyIsNotHiddenByAnIndirectPath() {
        // M depends on B and A; B depends on A and redeclares X. A's X still
        // reaches M directly, so M sees both.
        Library a = library("A");
        Class ax = cls("X", a);
        Library b = library("B");
        Class bx = cls("X", b);
        dependsOn(b, a);
        Model m = model("M");
        dependsOn(m, b);
        dependsOn(m, a);

        assertEquals(Arrays.asList(bx, ax), m.getInheritedMembers());
    }

    @Test
    void cyclicDependenciesDoNotHang() {
        Library a = library("A");
        Class ac = cls("AC", a);
        Library b = library("B");
        Class bc = cls("BC", b);
        dependsOn(a, b);
        dependsOn(b, a);

        assertEquals(Arrays.asList(bc), a.getInheritedMembers());
        assertEquals(Arrays.asList(ac, bc), members(a));
    }

    // ---------------------------------------------------------------------
    // Hiding keys of operators
    // ---------------------------------------------------------------------

    @Test
    void unnamedOperatorsAreKeyedByTheirSignature() {
        UserOperator and = natural("a", "'and'", "b");
        assertEquals("_ 'and' _", NamespaceSupport.hidingKey(and));
        UserOperator named = natural("a", "'and'", "b");
        named.setName("conjunction");
        assertEquals("conjunction", NamespaceSupport.hidingKey(named));
    }

    @Test
    void unnamedOperatorsWithDifferentKeywordsDoNotHideEachOther() {
        Library logic = library("Logic");
        UserOperator or = natural("a", "'or'", "b");
        logic.getOperators().add(or);
        Model m = model("M");
        UserOperator and = natural("a", "'and'", "b");
        m.getOperators().add(and);
        dependsOn(m, logic);

        assertEquals(Arrays.asList(or), m.getInheritedMembers());
        assertEquals(Arrays.asList(and, or), m.getAllOperators());
    }

    @Test
    void anOwnOperatorHidesAnInheritedOneWithTheSameSignature() {
        Library logic = library("Logic");
        logic.getOperators().add(natural("a", "'neg'"));
        Model m = model("M");
        UserOperator neg = natural("x", "'neg'");
        m.getOperators().add(neg);
        dependsOn(m, logic);

        assertTrue(m.getInheritedMembers().isEmpty());
        assertEquals(Arrays.asList(neg), m.getAllOperators());
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** An unnamed operator; arguments in quotes are keywords, the others parameters. */
    private static UserOperator natural(String... header) {
        UserOperator operator = F.createUserOperator();
        for (String element : header) {
            if (element.startsWith("'")) {
                Keyword keyword = F.createKeyword();
                keyword.setKeyword(element);
                operator.getHeader().add(keyword);
            } else {
                Variable parameter = F.createVariable();
                parameter.setName(element);
                operator.getHeader().add(parameter);
            }
        }
        return operator;
    }

    private static List<NamedEntity> members(Library library) {
        return new ArrayList<>(library.getMembers());
    }

    private static Model model(String name) {
        Model model = F.createModel();
        model.setName(name);
        return model;
    }

    private static Library library(String name) {
        Library library = F.createLibrary();
        library.setName(name);
        return library;
    }

    private static Class cls(String name, Library owner) {
        Class cls = F.createClass();
        cls.setName(name);
        owner.getClasses().add(cls);
        return cls;
    }

    private static Variable variable(String name, Class owner) {
        Variable variable = F.createVariable();
        variable.setName(name);
        owner.getVariables().add(variable);
        return variable;
    }

    private static Variable variable(String name, Model owner) {
        Variable variable = F.createVariable();
        variable.setName(name);
        owner.getVariables().add(variable);
        return variable;
    }

    private static void dependsOn(Library dependent, Library dependency) {
        LibraryDependency link = F.createLibraryDependency();
        link.setName(dependency.getName());
        link.setLibrary(dependency);
        dependent.getSuperlibs().add(link);
    }

    private static void unbound(Library dependent, String name) {
        LibraryDependency link = F.createLibraryDependency();
        link.setName(name);
        dependent.getSuperlibs().add(link);
    }
}
