package org.openmodelica.crml.modelica.genmodel;

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

import org.openmodelica.crml.modelica.metamodel.ModelicaPackage;
import org.openmodelica.crml.modelica.trace.metamodel.TracePackage;

/**
 * Writes a .genmodel for the Modelica and trace metamodels. Diagnostic only:
 * the Java actually compiled into this module is produced by the Xtext/Xcore
 * builder (see build.gradle.kts), exactly as in :model.
 */
public class GenmodelMain {
    public static void main(String[] args) throws IOException {
        String target = args[0];

        ResourceSetImpl resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
                "genmodel", new XMIResourceFactoryImpl());

        // Both metamodels are named "metamodel", so the base package and the
        // prefix differ per package; take them from the generated interfaces.
        List<Class<?>> packageInterfaces = Arrays.asList(
                ModelicaPackage.class,
                TracePackage.class);
        List<EPackage> list = Arrays.asList(
                ModelicaPackage.eINSTANCE,
                TracePackage.eINSTANCE);

        GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
        genModel.setComplianceLevel(GenJDKLevel.JDK80_LITERAL);
        genModel.initialize(list);
        for (GenPackage genPackage : genModel.getGenPackages()) {
            Class<?> packageInterface = packageInterfaces.get(list.indexOf(genPackage.getEcorePackage()));
            String javaPackage = packageInterface.getPackage().getName();
            genPackage.setBasePackage(javaPackage.substring(0, javaPackage.lastIndexOf('.')));
            String simpleName = packageInterface.getSimpleName();
            genPackage.setPrefix(simpleName.substring(0, simpleName.length() - "Package".length()));
        }
        Resource genModelResource = resourceSet.createResource(URI.createFileURI(target));
        genModelResource.getContents().add(genModel);
        genModelResource.save(Collections.emptyMap());
    }
}
