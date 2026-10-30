package crml.compiler.crmlcv2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import crml.compiler.crmlcv2.templates.Transformer;
import crml.model.language.BuiltinType;
import crml.model.language.BuiltinTypeReference;
import crml.model.language.LanguageFactory;
import crml.model.language.Model;
import crml.model.language.UserTypereference;
import crml.model.language.Variable;
import crml.model.modelica.ClassDefinition;
import crml.model.modelica.RawReason;
import crml.model.modelica.Reference;
import crml.modelica.build.Modelica;
import crml.modelica.print.ModelicaPrinter;

/**
 * Type and function names are references to generated classes where one exists,
 * and raw text otherwise.
 */
public class ReferenceResolutionTest implements Modelica {

    private static final LanguageFactory F = LanguageFactory.eINSTANCE;

    private static BuiltinTypeReference builtin(BuiltinType type) {
        BuiltinTypeReference reference = F.createBuiltinTypeReference();
        reference.setBuiltinType(type);
        return reference;
    }

    private static Model crmlModel(String name) {
        Model model = F.createModel();
        model.setName(name);
        return model;
    }

    private static Reference<ClassDefinition> resolveIn(Model model, crml.model.language.TypeReference type) {
        // Run a real translation so the class registry is populated, then ask
        // the resolver the same question the transformation would.
        Transformer transformer = new Transformer();
        TransformationContext ctx = transformer.transform(model);
        return transformer.types().resolve(ctx, type);
    }

    @Test
    public void crmlBuiltinTypesAreRawAgainstTheLibrary() {
        Reference<ClassDefinition> reference = resolveIn(crmlModel("M"), builtin(BuiltinType.BOOLEAN));
        assertNull(reference.getTarget());
        assertEquals(RawReason.EXTERNAL_LIBRARY, reference.getReason());
        assertEquals("CRMLtoModelica.Types.Boolean4", reference.getRawText());
    }

    @Test
    public void modelicaPrimitivesAreRawAgainstTheLanguage() {
        Reference<ClassDefinition> reference = resolveIn(crmlModel("M"), builtin(BuiltinType.REAL));
        assertNull(reference.getTarget());
        assertEquals(RawReason.MODELICA_BUILTIN, reference.getReason());
        assertEquals("Real", reference.getRawText());
    }

    @Test
    public void aCrmlClassResolvesToItsGeneratedClass() {
        crml.model.language.Class clazz = F.createClass();
        clazz.setName("Contract");
        Model model = crmlModel("M");
        model.getClasses().add(clazz);

        UserTypereference type = F.createUserTypereference();
        type.setDomain(clazz);

        Reference<ClassDefinition> reference = resolveIn(model, type);
        assertNotNull(reference.getTarget());
        assertEquals("Contract", reference.getTarget().getName());
        assertNull(reference.getRawText());
    }

    /**
     * The defect the whole change exists for. A CRML class named with a Modelica
     * reserved word declares as {@code model 'input'}; before the type was a
     * reference the printer wrote the component's type verbatim, so the same
     * class was referenced as {@code input} and the output was not valid
     * Modelica. Both now go through the same quoting.
     */
    @Test
    public void aClassNeedingQuotingIsQuotedAtEveryUse() {
        crml.model.language.Class clazz = F.createClass();
        clazz.setName("input");

        UserTypereference declared = F.createUserTypereference();
        declared.setDomain(clazz);
        Variable instance = F.createVariable();
        instance.setName("i");
        instance.setDomain(declared);

        Model model = crmlModel("M");
        model.getClasses().add(clazz);
        model.getVariables().add(instance);

        String text = new OMCv2().translate(model);

        assertTrue(text.contains("model 'input'"), text);
        assertTrue(text.contains("end 'input';"), text);
        assertTrue(text.contains("'input' i;"), text);
        assertFalse(text.contains("\n    input i;"), text);
    }

    /** A resolved name is recomputed from the target, so renaming cannot leave it stale. */
    @Test
    public void renamingAGeneratedClassRenamesItsReferences() {
        crml.model.language.Class clazz = F.createClass();
        clazz.setName("Contract");

        UserTypereference declared = F.createUserTypereference();
        declared.setDomain(clazz);
        Variable instance = F.createVariable();
        instance.setName("c");
        instance.setDomain(declared);

        Model model = crmlModel("M");
        model.getClasses().add(clazz);
        model.getVariables().add(instance);

        TranslationResult result = new OMCv2().translateModel(model);
        assertTrue(result.text().contains("Contract c;"), result.text());

        result.modelica().getNestedClasses().get(0).setName("Agreement");
        String renamed = ModelicaPrinter.print(result.modelica());
        assertTrue(renamed.contains("model Agreement"), renamed);
        assertTrue(renamed.contains("Agreement c;"), renamed);
        assertFalse(renamed.contains("Contract"), renamed);
    }

    /** A class declared before the class it uses still resolves - no link phase needed. */
    @Test
    public void aForwardClassReferenceResolves() {
        crml.model.language.Class later = F.createClass();
        later.setName("Later");

        UserTypereference memberType = F.createUserTypereference();
        memberType.setDomain(later);
        Variable member = F.createVariable();
        member.setName("m");
        member.setDomain(memberType);

        crml.model.language.Class earlier = F.createClass();
        earlier.setName("Earlier");
        earlier.getVariables().add(member);

        Model model = crmlModel("M");
        model.getClasses().add(earlier);
        model.getClasses().add(later);

        TranslationResult result = new OMCv2().translateModel(model);
        assertFalse(result.hasErrors(), result.diagnostics().toString());
        assertTrue(result.text().contains("Later m;"), result.text());
    }

    /** A raw name is printed exactly as stored - quoting it would break der, and4 and friends. */
    @Test
    public void rawNamesAreNeverQuoted() {
        ClassDefinition cls = model("M");
        cls.getComponents().add(component(
            libraryRef("CRMLtoModelica.Types.Boolean4"), "b"));
        String text = ModelicaPrinter.print(cls);
        assertTrue(text.contains("CRMLtoModelica.Types.Boolean4 b;"), text);
        assertFalse(text.contains("'"), text);
    }
}
