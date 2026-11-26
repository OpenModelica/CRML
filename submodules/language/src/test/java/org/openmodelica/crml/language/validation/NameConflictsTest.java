package org.openmodelica.crml.language.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.language.validation.rules.NameConflicts;
import org.openmodelica.crml.language.workspace.CrmlResource;
import org.openmodelica.crml.language.workspace.CrmlWorkspace;
import org.openmodelica.crml.model.metamodel.Class;
import org.openmodelica.crml.model.metamodel.Library;
import org.openmodelica.crml.model.metamodel.LibraryDependency;
import org.openmodelica.crml.model.metamodel.NamedEntity;

/** The {@link NameConflicts} rule. */
public class NameConflictsTest {

    /** Loads and resolves {@code texts}; the last one is the root that is checked. */
    private static EObject resolved(String... texts) {
        CrmlWorkspace ws = new CrmlWorkspace();
        CrmlResource last = null;
        for (String text : texts) {
            last = ws.load(text);
        }
        ws.resolve();
        return last.getRoot();
    }

    /** The findings of the rule on {@code root}. */
    private static List<Diagnostic> conflicts(EObject root) {
        List<Diagnostic> found = new ArrayList<>();
        for (Diagnostic d : CRMLValidator.validate(root).getChildren()) {
            if (CRMLValidator.DIAGNOSTIC_SOURCE.equals(d.getSource()) && d.getCode() == NameConflicts.CODE) {
                found.add(d);
            }
        }
        return found;
    }

    /** The name of the object each finding is reported on. */
    private static String reportedOn(List<Diagnostic> findings) {
        List<String> names = new ArrayList<>();
        for (Diagnostic d : findings) {
            Object at = d.getData().get(0);
            names.add(at instanceof NamedEntity ? ((NamedEntity) at).getName()
                : at instanceof LibraryDependency ? "dependency " + ((LibraryDependency) at).getName()
                : String.valueOf(at));
        }
        return names.toString();
    }

    // ---------------------------------------------------------------------
    // Own members: one namespace for all kinds
    // ---------------------------------------------------------------------

    @Test
    void twoVariablesOfTheSameName() {
        List<Diagnostic> findings = conflicts(resolved("model M is {\n"
            + "Boolean x is external;\n"
            + "Boolean x is external;\n"
            + "};"));

        assertEquals(1, findings.size(), findings.toString());
        assertEquals(Diagnostic.ERROR, findings.get(0).getSeverity());
        String message = findings.get(0).getMessage();
        assertTrue(message.contains("Variable x") && message.contains("Model M"), message);
    }

    @Test
    void aVariableAndAClassOfTheSameName() {
        assertEquals("[X]", reportedOn(conflicts(resolved("model M is {\n"
            + "class X is { Integer i is external; };\n"
            + "Boolean X is external;\n"
            + "};"))));
    }

    @Test
    void aNamedOperatorAndAVariableOfTheSameName() {
        // Operators are values of type Operator: they share the namespace.
        assertEquals("[f]", reportedOn(conflicts(resolved("model M is {\n"
            + "Operator f is Operator [ Boolean ] (Boolean a) = not a;\n"
            + "Boolean f is external;\n"
            + "};"))));
    }

    @Test
    void twoOperatorsWithTheSameKeywords() {
        List<Diagnostic> findings = conflicts(resolved("model M is {\n"
            + "Template b1 'or' b2 = not (not b1 and not b2);\n"
            + "Template a 'or' b = a or b;\n"
            + "};"));

        assertEquals(1, findings.size(), findings.toString());
        assertTrue(findings.get(0).getMessage().contains("_ 'or' _"), findings.get(0).getMessage());
    }

    @Test
    void anUnnamedOperatorDoesNotConflictWithAVariableNamedLikeItsKeyword() {
        assertTrue(conflicts(resolved("model M is {\n"
            + "Template b1 'implies' b2 = not b1 or b2;\n"
            + "Boolean implies is external;\n"
            + "};")).isEmpty());
    }

    @Test
    void operatorsWithDifferentKeywordsDoNotConflict() {
        assertTrue(conflicts(resolved("model M is {\n"
            + "Template b1 'or' b2 = not (not b1 and not b2);\n"
            + "Template b1 'xor' b2 = (b1 or b2) and not (b1 and b2);\n"
            + "};")).isEmpty());
    }

    @Test
    void twoParametersOfAnOperator() {
        assertEquals("[a]", reportedOn(conflicts(resolved("model M is {\n"
            + "Operator [ Boolean ] Boolean a 'with' Boolean a = a;\n"
            + "};"))));
    }

    @Test
    void aParameterMayHaveTheNameOfAVariableAroundTheOperator() {
        assertTrue(conflicts(resolved("model M is {\n"
            + "Boolean a is external;\n"
            + "Operator [ Boolean ] 'f' Boolean a = not a;\n"
            + "};")).isEmpty());
    }

    @Test
    void fragmentsOfASyntaxErrorAreNotReported() {
        // After the missing ';', "from e;" is read as a variable e of type from.
        assertTrue(conflicts(resolved("model M is {\n"
            + "Event e is external;\n"
            + "Real d is time from e;\n"
            + "};")).isEmpty());
    }

    @Test
    void twoVariablesOfAClass() {
        assertEquals("[i]", reportedOn(conflicts(resolved("model M is {\n"
            + "class C is { Integer i is external; Integer i is external; };\n"
            + "};"))));
    }

    // ---------------------------------------------------------------------
    // A root and its union dependencies
    // ---------------------------------------------------------------------

    @Test
    void anOwnMemberAndAMemberOfADependency() {
        List<Diagnostic> findings = conflicts(resolved(
            "library A is { class X is { Integer i is external; }; };",
            "model M is A union {\nclass X is { Integer j is external; };\n};"));

        assertEquals("[X]", reportedOn(findings));
        String message = findings.get(0).getMessage();
        assertTrue(message.contains("Library A") && message.contains("unique"), message);
    }

    @Test
    void anOwnVariableAndAVariableOfAModelItDependsOn() {
        assertEquals("[x]", reportedOn(conflicts(resolved(
            "model Base is { Boolean x is external; };",
            "model M is Base union {\nBoolean x is external;\n};"))));
    }

    @Test
    void twoDependenciesDeclaringTheSameNameEvenIfItIsNotUsed() {
        List<Diagnostic> findings = conflicts(resolved(
            "library A is { class X is { Integer i is external; }; };",
            "library B is { class X is { Integer i is external; }; };",
            "model M is A union B union {\nBoolean y is external;\n};"));

        assertEquals("[dependency B]", reportedOn(findings));
        String message = findings.get(0).getMessage();
        assertTrue(message.contains("Class X of Library B") && message.contains("Class X of Library A"), message);
    }

    @Test
    void oneDeclarationReachedThroughTwoDependenciesIsNotAConflict() {
        assertTrue(conflicts(resolved(
            "library L is { class X is { Integer i is external; }; };",
            "library A is L union { };",
            "library B is L union { };",
            "model M is A union B union {\nX x;\n};")).isEmpty());
    }

    @Test
    void aDependencyListedTwiceIsNotAConflict() {
        assertTrue(conflicts(resolved(
            "library L is { class X is { Integer i is external; }; };",
            "model M is L union L union {\nX x;\n};")).isEmpty());
    }

    // ---------------------------------------------------------------------
    // Classes and their superclasses
    // ---------------------------------------------------------------------

    @Test
    void aRedeclarationInASubclassIsNotChecked() {
        assertTrue(conflicts(resolved("model M is {\n"
            + "class A is { Integer i is external; };\n"
            + "class B is { Integer i is external; } extends A;\n"
            + "};")).isEmpty());
    }

    @Test
    void twoSuperclassesWithMembersOfTheSameName() {
        // The grammar has one superclass per class; the metamodel allows several.
        EObject root = resolved("model M is {\n"
            + "class A is { Integer i is external; };\n"
            + "class B is { Integer i is external; };\n"
            + "class C is { Integer j is external; } extends A;\n"
            + "};");
        List<Class> classes = ((Library) root).getClasses();
        classes.get(2).getSuperClasses().add(classes.get(1));

        List<Diagnostic> findings = conflicts(root);

        assertEquals("[C]", reportedOn(findings));
        String message = findings.get(0).getMessage();
        assertTrue(message.contains("from B") && message.contains("from A"), message);
    }
}
