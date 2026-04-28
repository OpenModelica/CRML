package crml.language.xtext;

import com.google.inject.Injector;
import crml.model.language.LanguagePackage;
import org.eclipse.emf.ecore.EPackage;

/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
public class CrmlXtextStandaloneSetup extends CrmlXtextStandaloneSetupGenerated {

	public static void doSetup() {
		new CrmlXtextStandaloneSetup().createInjectorAndDoEMFRegistration();
	}

	@Override
	public Injector createInjectorAndDoEMFRegistration() {
		// The grammar imports crml.model.language types, so the EPackage must be
		// registered before Xtext tries to resolve grammar-binary proxies.
		if (!EPackage.Registry.INSTANCE.containsKey(LanguagePackage.eNS_URI))
			EPackage.Registry.INSTANCE.put(LanguagePackage.eNS_URI, LanguagePackage.eINSTANCE);
		return super.createInjectorAndDoEMFRegistration();
	}
}
