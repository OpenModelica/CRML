package crml.modelica;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.codegen.ecore.genmodel.GenJDKLevel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import crml.model.modelica.ModelicaPackage;
import crml.model.trace.TracePackage;

/**
 * Writes a .genmodel for the Modelica and trace metamodels. Diagnostic only:
 * the Java actually compiled into this module is produced by the Xtext/Xcore
 * builder (see build.gradle.kts), exactly as in :model.
 */
public class GenmodelMain {
    public static void main(String[] args) throws IOException {
        String basepackage = args[0];
        String target = args[1];

        ResourceSetImpl resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
                "genmodel", new XMIResourceFactoryImpl());

        List<EPackage> list = Arrays.asList(
                (EPackage) ModelicaPackage.eINSTANCE,
                (EPackage) TracePackage.eINSTANCE);

        GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
        genModel.setComplianceLevel(GenJDKLevel.JDK80_LITERAL);
        System.out.println("Number of metamodels:" + list.size());
        genModel.initialize(list);
        for (GenPackage genPackage : genModel.getGenPackages()) {
            if (basepackage != null) {
                genPackage.setBasePackage(basepackage);
            }
        }
        System.out.println("Target: " + target);
        Resource genModelResource = resourceSet.createResource(URI.createFileURI(target));
        genModelResource.getContents().add(genModel);
        genModelResource.save(Collections.emptyMap());
        System.out.println("GenModel saved to: " + target);
    }
}
