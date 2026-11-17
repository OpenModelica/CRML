package org.openmodelica.crml.testutil.constraints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmodelica.crml.testutil.constraints.ConstraintResult.Status;

/**
 * Semantics of the constraint language, on Ecore models used as data (so the
 * operations of Ecore's own classes, such as {@code EClass.getFeatureCount()},
 * can be invoked) spread over three resources:
 *
 * <pre>
 * base.ecore  package base:  Machine (abstract), Sensor { reading: EInt, owner: Machine (no opposite) },
 *                            enum Color { RED, GREEN }, Lamp { color: Color }
 * app.ecore   package app:   Pump extends Machine;  Valve extends Machine, Sensor;
 *                            Lonely (abstract, no supertypes);  Ghost extends Floating
 *             (Floating is an EClass outside every resource)
 * data.xmi    one Lamp instance, color = GREEN
 * </pre>
 */
class ConstraintEngineTest {

    private static ResourceSet resourceSet;
    private static ConstraintEngine engine;

    @BeforeAll
    static void fixture() {
        EcoreFactory f = EcoreFactory.eINSTANCE;

        EPackage base = f.createEPackage();
        base.setName("base");
        base.setNsURI("http://example/base");
        EClass machine = eClass(base, "Machine", true);
        EClass sensor = eClass(base, "Sensor", false);
        EAttribute reading = f.createEAttribute();
        reading.setName("reading");
        reading.setEType(EcorePackage.Literals.EINT);
        sensor.getEStructuralFeatures().add(reading);
        EReference owner = f.createEReference();
        owner.setName("owner");
        owner.setEType(machine);
        sensor.getEStructuralFeatures().add(owner);
        EEnum color = f.createEEnum();
        color.setName("Color");
        EEnumLiteral red = f.createEEnumLiteral();
        red.setName("RED");
        red.setValue(0);
        EEnumLiteral green = f.createEEnumLiteral();
        green.setName("GREEN");
        green.setValue(1);
        color.getELiterals().add(red);
        color.getELiterals().add(green);
        base.getEClassifiers().add(color);
        EClass lamp = eClass(base, "Lamp", false);
        EAttribute colorAttr = f.createEAttribute();
        colorAttr.setName("color");
        colorAttr.setEType(color);
        lamp.getEStructuralFeatures().add(colorAttr);

        EPackage app = f.createEPackage();
        app.setName("app");
        app.setNsURI("http://example/app");
        eClass(app, "Pump", false).getESuperTypes().add(machine);
        EClass valve = eClass(app, "Valve", false);
        valve.getESuperTypes().add(machine);
        valve.getESuperTypes().add(sensor);
        eClass(app, "Lonely", true);
        EClass floating = f.createEClass();
        floating.setName("Floating");
        eClass(app, "Ghost", false).getESuperTypes().add(floating);

        EObject lampInstance = EcoreUtil.create(lamp);
        lampInstance.eSet(colorAttr, green);

        resourceSet = new ResourceSetImpl();
        add(base, "base.ecore");
        add(app, "app.ecore");
        add(lampInstance, "data.xmi");

        engine = new ConstraintEngine(EcorePackage.eINSTANCE, base);
    }

    private static EClass eClass(EPackage pkg, String name, boolean isAbstract) {
        EClass c = EcoreFactory.eINSTANCE.createEClass();
        c.setName(name);
        c.setAbstract(isAbstract);
        pkg.getEClassifiers().add(c);
        return c;
    }

    private static void add(EObject root, String file) {
        Resource resource = new ResourceImpl(URI.createURI(file));
        resourceSet.getResources().add(resource);
        resource.getContents().add(root);
    }

    private static ConstraintResult run(String constraint) {
        List<ConstraintResult> results = engine.evaluate(ConstraintFile.parse(constraint), resourceSet);
        assertEquals(1, results.size(), constraint);
        return results.get(0);
    }

    /** Asserts the status, and that the detail starts with {@code detailPrefix} (if not null). */
    private static ConstraintResult check(String constraint, Status status, String detailPrefix) {
        ConstraintResult result = run(constraint);
        assertEquals(status, result.status, result.toString());
        if (detailPrefix != null) {
            assertTrue(result.detail.startsWith(detailPrefix), result.toString());
        }
        return result;
    }

    private static void passes(String constraint) {
        check(constraint, Status.PASS, "");
    }

    private static void isFalse(String constraint) {
        check(constraint, Status.FAIL, "false: ");
    }

    private static void isUnresolved(String constraint) {
        check(constraint, Status.FAIL, "unresolved: ");
    }

    private static void isError(String constraint) {
        check(constraint, Status.ERROR, null);
    }

    // ---------------------------------------------------------------- binder

    @Test
    void unknownTypeAndFeatureAreErrors() {
        assertEquals("unknown type Clas", run("[Clas::name == \"Pump\"] is empty").detail);
        isError("[EClass::name == \"Pump\"].nosuch is empty");
        isError("[EClass::nosuch == \"Pump\"] is empty");
        isError("[EClass::name == \"Pump\"] is a Nope");
    }

    @Test
    void operationIsAStep() {
        passes("[EClass::name == \"Sensor\"].featureCount == 2");      // getFeatureCount()
        passes("[EClass::name == \"Sensor\"].getFeatureCount == 2");   // by its own name
    }

    // ---------------------------------------------------------------- selectors

    @Test
    void subtypesAreSelected() {
        passes("count [ENamedElement::name == \"Pump\"] == 1");
        passes("[ENamedElement::name == \"Pump\"] is a EClass");
    }

    @Test
    void scopeNarrowsToTheContentsOfOneObject() {
        passes("count [EPackage::name == \"app\"] / [EClass] == 4");
        passes("count [EPackage::name == \"base\"] / [EClass] == 3");
        passes("[EPackage::name == \"app\"] / [EClass::name == \"Pump\"].abstract == false");
    }

    @Test
    void filterThatCannotBeFollowedDoesNotSelect() {
        // eOpposite exists only on EReference; on every EClass the path cannot be
        // followed, so no EClass is selected - and that is not an error.
        passes("count [ENamedElement::eOpposite.name == \"x\"] == 0");
        passes("count [EClass::abstract != true] == 5");                 // Sensor, Lamp, Pump, Valve, Ghost
        passes("count [EClass::eSuperTypes.name contains \"Machine\"] == 2");
    }

    // ---------------------------------------------------------------- quantifiers

    @Test
    void plainFormNeedsExactlyOneObject() {
        passes("[EClass::name == \"Pump\"].abstract == false");
        isUnresolved("[EClass::name == \"Nope\"].abstract == false");
        isUnresolved("[EPackage].name == \"app\"");                      // two packages
        isFalse("[EClass::name == \"Pump\"].abstract == true");
    }

    @Test
    void emptySet() {
        ConstraintResult vacuous = check("all [EClass::name == \"Nope\"].abstract == true", Status.PASS, null);
        assertEquals("vacuously true: 0 objects matched", vacuous.detail);
        assertEquals("false: no witness: 0 objects matched",
            run("some [EClass::name == \"Nope\"].abstract == true").detail);
    }

    @Test
    void oneAndTwoMatches() {
        passes("all [EPackage::name == \"app\"] / [EClass::name == \"Pump\"].abstract == false");
        passes("some [EPackage::name == \"app\"] / [EClass::name == \"Pump\"].abstract == false");
        passes("all [EPackage::name == \"base\"] / [EClass::abstract == false].abstract == false");
        isFalse("all [EPackage::name == \"base\"] / [EClass].abstract == false");
        passes("some [EPackage::name == \"base\"] / [EClass].abstract == true");
    }

    // ---------------------------------------------------------------- three-valued logic
    //
    // In app, ".eSuperTypes.@root.name == \"base\"" is
    //   Pump: true, Valve: true (both supertypes in base), Lonely: false (no supertypes),
    //   Ghost: unresolved (Floating is in no resource).

    private static final String APP = "[EPackage::name == \"app\"] / ";
    private static final String TO_BASE = ".eSuperTypes.@root.name == \"base\"";

    @Test
    void someIsDecidedByATrueElement() {
        passes("some " + APP + "[EClass]" + TO_BASE);                                   // T T F U
        passes("some " + APP + "[EClass::abstract == false]" + TO_BASE);                // T T U
    }

    @Test
    void someWithoutTrue() {
        isUnresolved("some " + APP + "[EClass::name == \"Ghost\"]" + TO_BASE);          // U
        isUnresolved("some " + APP + "[EClass::name != \"Pump\", name != \"Valve\"]" + TO_BASE); // F U
        isFalse("some " + APP + "[EClass::name == \"Lonely\"]" + TO_BASE);              // F
    }

    @Test
    void allIsDecidedByAFalseElement() {
        ConstraintResult r = check("all " + APP + "[EClass]" + TO_BASE, Status.FAIL, "false: counterexamples: ");
        assertTrue(r.detail.contains("EClass Lonely (app.ecore) gives {}"), r.detail);
        isFalse("all " + APP + "[EClass::name != \"Pump\", name != \"Valve\"]" + TO_BASE); // F U
    }

    @Test
    void allWithoutFalse() {
        ConstraintResult r = check("all " + APP + "[EClass::abstract == false]" + TO_BASE,
            Status.FAIL, "unresolved: ");                                              // T T U
        assertTrue(r.detail.contains("step .@root on EClass Floating: not in a resource"), r.detail);
        passes("all " + APP + "[EClass::eSuperTypes.name contains \"Machine\"]" + TO_BASE); // T T
    }

    @Test
    void identities() {
        // some P == not all not P, on a total predicate: Lonely is the only abstract class.
        passes("some " + APP + "[EClass].abstract == true");
        isFalse("all " + APP + "[EClass].abstract != true");
    }

    // ---------------------------------------------------------------- definite descriptions

    @Test
    void scopeSelectorMustDenoteOneObject() {
        assertEquals("unresolved: [EPackage::name == \"nope\"] matched 0 objects, expected exactly one",
            run("all [EPackage::name == \"nope\"] / [EClass].abstract == true").detail);
        isUnresolved("all [EPackage] / [EClass].abstract == true");
    }

    @Test
    void rightOperandMustDenoteOneValue() {
        passes("[EClass::name == \"Pump\"].eSuperTypes contains [EClass::name == \"Machine\"]");
        passes("[EClass::name == \"Pump\"].eSuperTypes == [EClass::name == \"Machine\"]");
        isUnresolved("[EClass::name == \"Pump\"].eSuperTypes contains [EClass::name == \"Nope\"]");
        isUnresolved("[EClass::name == \"Pump\"].eSuperTypes contains [EClass]");
        passes("[EClass::name == \"Pump\"].name == [EClass::name == \"Pump\"].name");
        isUnresolved("[EClass::name == \"Valve\"].name == [EClass::name == \"Valve\"].eSuperTypes");
    }

    @Test
    void unresolvedRightOperandOverEmptySetIsStillVacuous() {
        passes("all [EClass::name == \"Nope\"].eSuperTypes contains [EClass::name == \"Nope\"]");
    }

    // ---------------------------------------------------------------- unresolved chains

    @Test
    void stepFailures() {
        assertEquals("unresolved: step .eOpposite on EClass Pump (app.ecore): no such feature",
            run("[EClass::name == \"Pump\"].eOpposite is empty").detail);
        assertEquals("unresolved: step .eOpposite on EReference owner (base.ecore): unset",
            run("[EReference::name == \"owner\"].eOpposite is empty").detail);
        isUnresolved("[EClass::name == \"Pump\"].name.name == \"x\"");                   // not an object
        passes("[EClass::name == \"Lonely\"].eSuperTypes is empty");                     // empty many-valued
    }

    // ---------------------------------------------------------------- pseudo steps

    @Test
    void pseudoStepsAcrossResources() {
        passes("[EClass::name == \"Pump\"].eSuperTypes.@root.name == \"base\"");
        passes("[EClass::name == \"Pump\"].eSuperTypes.@file == \"base.ecore\"");
        passes("[EClass::name == \"Pump\"].@container.name == \"app\"");
        passes("[EClass::name == \"Pump\"].@root == [EPackage::name == \"app\"]");
        isUnresolved("[EPackage::name == \"app\"].@container is empty");                 // a root
        isUnresolved("[EClass::name == \"Ghost\"].eSuperTypes.@file == \"x\"");          // outside a resource
        isUnresolved("[EClass::name == \"Ghost\"].eSuperTypes.@container is empty");
    }

    // ---------------------------------------------------------------- conditions and literals

    @Test
    void everyCondition() {
        String pump = "[EClass::name == \"Pump\"]";
        passes(pump + ".name == \"Pump\"");
        passes(pump + ".name != \"Valve\"");
        passes(pump + ".eSuperTypes.name contains \"Machine\"");
        passes(pump + ".eSuperTypes.name not contains \"Sensor\"");
        passes(pump + ".eSuperTypes is not empty");
        passes(pump + ".eSuperTypes is a EClass");
        isFalse(pump + ".ePackage is a EClass");
        passes("[EClass::name == \"Lonely\"].eSuperTypes is a EAttribute");            // ∀ over empty set
        isFalse("[EClass::name == \"Valve\"].eSuperTypes.name == \"Machine\"");         // two values, not {x}
        passes("[EClass::name == \"Valve\"].eSuperTypes.name != \"Machine\"");
    }

    @Test
    void literalConversion() {
        passes("[EAttribute::name == \"reading\"].lowerBound == 0");                    // int
        passes("[EAttribute::name == \"reading\"].upperBound == 1");
        passes("[EClass::name == \"Machine\"].abstract == true");                       // boolean
        passes("[Lamp].color == \"GREEN\"");                                            // enum literal
        isFalse("[Lamp].color == \"RED\"");
        assertTrue(run("[EClass::name == \"Pump\"].eSuperTypes == \"Machine\"").detail
            .startsWith("cannot compare the literal \"Machine\" with the object EClass Machine"));
        isError("[EClass::name == \"Pump\"].eSuperTypes == \"Machine\"");
        isError("[EClass::name == \"Pump\"].abstract == 1");
    }

    @Test
    void countDeduplicates() {
        // Pump {Machine}, Valve {Machine, Sensor}, Lonely {}: the union has 2 objects.
        passes("count " + APP + "[EClass::name != \"Ghost\"].eSuperTypes == 2");
        passes("count " + APP + "[EClass::name != \"Ghost\"].eSuperTypes.@root == 1");
        ConstraintResult r = check("count " + APP + "[EClass::name != \"Ghost\"].eSuperTypes > 2",
            Status.FAIL, "false: found 2: ");
        assertTrue(r.detail.contains("EClass Machine (base.ecore)"), r.detail);
        isUnresolved("count " + APP + "[EClass].eSuperTypes.@root == 1");               // Ghost
        passes("count [EClass::name == \"Nope\"] == 0");
    }

    // ---------------------------------------------------------------- results

    @Test
    void oneBadLineDoesNotStopTheOthers() {
        List<ConstraintResult> results = engine.evaluate(ConstraintFile.parse(
            "[EClass::name == \"Pump\"] is a EClass\n"
                + "[EClass::name == ] is empty\n"
                + "[Clas] is empty\n"
                + "\n"
                + "# comment\n"
                + "[EClass::name == \"Pump\"].abstract == 1\n"
                + "[EClass::name == \"Pump\"].abstract == false\n"), resourceSet);
        assertEquals(5, results.size());
        assertEquals(Status.PASS, results.get(0).status);
        assertEquals(Status.ERROR, results.get(1).status);
        assertTrue(results.get(1).detail.startsWith("column 18: "), results.get(1).detail);
        assertEquals(Status.ERROR, results.get(2).status);
        assertEquals(Status.ERROR, results.get(3).status);
        assertEquals(6, results.get(3).line);
        assertEquals(Status.PASS, results.get(4).status);
    }

    @Test
    void tableRendersOneRowPerLine() {
        List<ConstraintResult> results = engine.evaluate(ConstraintFile.parse(
            "[EClass::name == \"Pump\"] is a EClass\n[Clas] is empty"), resourceSet);
        String html = ConstraintTable.of(results).report().toString();
        assertTrue(html.contains("<th>Constraint</th>"), html);
        assertEquals(3, html.split("<tr>", -1).length - 1, html);                       // header + 2 rows
        assertTrue(html.contains("unknown type Clas"), html);
    }
}
