package crml.model;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory;
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
import org.eclipse.emf.codegen.ecore.genmodel.GenJDKLevel;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;

import crml.model.language.LanguagePackage;

public class GenmodelMain {
    public static void main(String[] args) throws IOException {
        //String modeldir = args[1];
        String basepackage = args[0];
        String target = args[1];

        var resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
                "genmodel", new XMIResourceFactoryImpl());
                
        List<EPackage> list = List.of(LanguagePackage.eINSTANCE);

        GenModel genModel = GenModelFactory.eINSTANCE.createGenModel();
        genModel.setComplianceLevel(GenJDKLevel.JDK60_LITERAL);
        //genModel.setModelDirectory(modeldir);
        // genModel.initialize(Collections.singletonList(ePackage));
        System.out.println("Number of metamodels:" + list.size());
        genModel.initialize(list);
        GenPackage genPackage = genModel.getGenPackages().get(0);
        if (basepackage != null) {
            genPackage.setBasePackage(basepackage);
        }
        System.out.println("Target: "+target);
        Resource genModelResource = resourceSet.createResource(URI.createFileURI(target));
        genModelResource.getContents().add(genModel);
        genModelResource.save(Collections.emptyMap());
        System.out.println("GenModel saved to: " + target);
    }
}
