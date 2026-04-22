package crml.language.xtext;


/**
 * Initialization support for running Xtext languages without Equinox extension registry.
 */
public class CrmlXtextStandaloneSetup extends CrmlXtextStandaloneSetupGenerated {

	public static void doSetup() {
		new CrmlXtextStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}
