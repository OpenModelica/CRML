package org.openmodelica.crml.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.codegen.ecore.genmodel.GenJDKLevel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;

import org.openmodelica.crml.model.metamodel.CRMLPackage;

public class GenmodelMain {
    public static void main(String[] args) throws IOException {
        String target = args[0];

        ResourceSetImpl resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
                "genmodel", new XMIResourceFactoryImpl());

        List<EPackage> list = Arrays.asList(CRMLPackage.eINSTANCE);

        GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
        genModel.setComplianceLevel(GenJDKLevel.JDK80_LITERAL);
        //genModel.setModelDirectory(modeldir);
        // genModel.initialize(Collections.singletonList(ePackage));
        System.out.println("Number of metamodels:" + list.size());
        genModel.initialize(list);
        GenPackage genPackage = genModel.getGenPackages().get(0);
        // The EPackage is named "metamodel"; take the base package and the
        // prefix from the generated interface so the genmodel matches it.
        String javaPackage = CRMLPackage.class.getPackage().getName();
        genPackage.setBasePackage(javaPackage.substring(0, javaPackage.lastIndexOf('.')));
        String simpleName = CRMLPackage.class.getSimpleName();
        genPackage.setPrefix(simpleName.substring(0, simpleName.length() - "Package".length()));
        System.out.println("Target: "+target);
        Resource genModelResource = resourceSet.createResource(URI.createFileURI(target));
        genModelResource.getContents().add(genModel);
        genModelResource.save(Collections.emptyMap());
        System.out.println("GenModel saved to: " + target);
    }
}
